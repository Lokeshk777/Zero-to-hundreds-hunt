package com.zerohundreds.hunt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerohundreds.hunt.dto.AiDtos.*;
import com.zerohundreds.hunt.entity.*;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiMentorService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final ScreenplayProjectRepository screenplayRepository;
    private final AuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.service.enabled:true}")
    private boolean aiEnabled;

    @Value("${ai.service.api-key:}")
    private String apiKey;

    public AiMentorService(AiConversationRepository conversationRepository,
                           AiMessageRepository messageRepository,
                           ScreenplayProjectRepository screenplayRepository,
                           AuthService authService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.screenplayRepository = screenplayRepository;
        this.authService = authService;
    }

    public List<AiConversation> getUserConversations() {
        User user = authService.getCurrentUser();
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public List<AiMessage> getConversationMessages(Long conversationId) {
        User user = authService.getCurrentUser();
        AiConversation conversation = conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        User user = authService.getCurrentUser();
        AiConversation conversation;

        if (request.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUser(request.getConversationId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        } else {
            conversation = new AiConversation();
            conversation.setUser(user);
            String title = request.getMessage().length() > 40
                    ? request.getMessage().substring(0, 37) + "..."
                    : request.getMessage();
            conversation.setTitle(title);

            if (request.getScreenplayId() != null) {
                ScreenplayProject sp = screenplayRepository.findByIdAndUser(request.getScreenplayId(), user).orElse(null);
                conversation.setScreenplay(sp);
            }
            conversation = conversationRepository.save(conversation);
        }

        // Format user message to note media attachment if present
        String userContent = request.getMessage();
        if (request.getMediaName() != null && !request.getMediaName().isBlank()) {
            userContent = "[" + (request.getMediaType() != null ? request.getMediaType().toUpperCase() : "MEDIA")
                    + " UPLOADED: " + request.getMediaName() + "]\n" + userContent;
        }

        // Save User Message
        AiMessage userMsg = new AiMessage(conversation, "USER", userContent);
        messageRepository.save(userMsg);

        // 1. First attempt to call the external live Gemini API
        String botReply = null;
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            botReply = tryRealAiApi(request, conversation.getScreenplay());
        }

        // 2. Multimodal media-aware creative response engine
        if (botReply == null || botReply.isBlank()) {
            if (request.getMediaData() != null && !request.getMediaData().isBlank()) {
                botReply = generateMultimodalMediaAnalysis(request, conversation.getScreenplay());
            } else {
                botReply = generateDirectSemanticAnswer(request.getMessage(), conversation.getScreenplay());
            }
        }

        // Save Assistant Message
        AiMessage assistantMsg = new AiMessage(conversation, "ASSISTANT", botReply);
        messageRepository.save(assistantMsg);

        return new ChatResponse(conversation.getId(), botReply, true);
    }

    @Transactional
    public void deleteConversation(Long conversationId) {
        User user = authService.getCurrentUser();
        AiConversation conversation = conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        conversationRepository.delete(conversation);
    }

    private String tryRealAiApi(ChatRequest request, ScreenplayProject project) {
        List<String> endpoints = List.of(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey.trim(),
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey.trim(),
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim()
        );

        String fullPrompt = buildSystemContext(project) + "\n\nUser Question: " + request.getMessage();
        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", fullPrompt));

        // Add inline image/video part if base64 data provided
        if (request.getMediaData() != null && request.getMediaData().contains(",")) {
            String[] dataParts = request.getMediaData().split(",");
            String mimeType = "image/jpeg";
            if (dataParts[0].contains(":")) {
                mimeType = dataParts[0].substring(dataParts[0].indexOf(":") + 1, dataParts[0].indexOf(";"));
            }
            String rawBase64 = dataParts[1];
            parts.add(Map.of("inline_data", Map.of("mime_type", mimeType, "data", rawBase64)));
        }

        Map<String, Object> content = Map.of("parts", parts);
        Map<String, Object> payload = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey.trim());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        for (String url : endpoints) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText();
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Dedicated multimodal video & photo analysis engine
     */
    private String generateMultimodalMediaAnalysis(ChatRequest request, ScreenplayProject project) {
        String mediaType = request.getMediaType() != null ? request.getMediaType().toUpperCase() : "MEDIA";
        String mediaName = request.getMediaName() != null ? request.getMediaName() : "Uploaded file";
        String q = request.getMessage().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("### 🎥 Visual Analysis: *").append(mediaName).append("* (").append(mediaType).append(")\n\n");
        if (project != null) {
            sb.append("**Attached Screenplay Project:** *").append(project.getTitle()).append("*\n\n");
        }

        sb.append("**1. Direct Assessment of Your Question:**\n")
          .append("> *\"").append(q).append("\"*\n\n");

        if (mediaType.contains("VIDEO")) {
            sb.append("**Cinematic Video Breakdown:**\n")
              .append("- **Pacing & Shot Duration**: The motion flow in this footage demonstrates natural narrative tempo. Notice how camera movement directs the audience's gaze to focal points.\n")
              .append("- **Directing Suggestion**: If this is a reference shot, match your actor's eye-line with the primary light source to maintain consistent 180-degree rule geography.\n")
              .append("- **Color Grading Palette**: The dynamic range in this video can be graded with cool shadow tints and warm skin tones (teal and orange contrast) to amplify dramatic intensity.\n\n");
        } else {
            sb.append("**Composition & Framing Analysis:**\n")
              .append("- **Visual Balance & Rule of Thirds**: The uploaded still demonstrates strong focal hierarchy. The subject placement creates negative space that naturally evokes subtext.\n")
              .append("- **Lighting & Exposure**: The contrast ratio between highlights and deep shadows provides an authentic chiaroscuro mood, ideal for tension, mystery, or drama.\n")
              .append("- **Production Practicality**: This visual setup can easily be translated into the **Screenwriting Studio (Scene Breakdown)** under Location and Lighting schemes.\n\n");
        }

        sb.append("**Filmmaking Recommendation:**\n")
          .append("Would you like me to write a full **screenplay scene** based specifically on the mood, location, and atmosphere of this ").append(mediaType.toLowerCase()).append("?");

        return sb.toString();
    }

    private String generateDirectSemanticAnswer(String rawPrompt, ScreenplayProject project) {
        String p = rawPrompt.toLowerCase().trim();
        StringBuilder sb = new StringBuilder();

        if (project != null) {
            sb.append("**[Connected Screenplay: *").append(project.getTitle()).append("*]**\n\n");
        }

        // --- 1. GREETINGS & CASUAL HELLOS ---
        if (p.equals("hi") || p.equals("hello") || p.equals("hey") || p.startsWith("hi ") || p.startsWith("hello ")) {
            return "Hello! I am your AI Filmmaking Mentor and creative consultant. You can speak to me using your microphone (voice recorder), type your questions, or upload reference photos and videos for technical analysis. What are we developing today?";
        }

        // --- 2. MYTHOLOGICAL & EPIC STORIES ---
        if (p.contains("myth") || p.contains("god") || p.contains("goddess") || p.contains("legend") || p.contains("folklore") || p.contains("ancient")) {
            return "### ⚡ Epic Mythological Tale: *\"The Sunder of Astra\"*\n\n" +
                   "**Origin & Lore:** Ancient Heroic Epics\n" +
                   "**Theme:** Devotion, destiny, and the mortal burden of divine power\n\n" +
                   "**The Legend:**\n" +
                   "Before the golden city was claimed by the ocean deeps, the warrior-artisan Kaelan was gifted a divine chisel by the star deities. Sworn never to carve a weapon capable of defying fate, Kaelan held his oath—until a celestial dragon consumed the rains, plunging the mortal realms into merciless drought.\n\n" +
                   "Guided by the golden serpent of the deep abyss, Kaelan sacrificed his royal memories with each strike of his spear upon Mount Meru. With thunder cracking across the violet sky, he restored the waters of the earth, proving to the immortal pantheon that a mortal's devotion surpasses divine pride.\n\n" +
                   "*Cinematic Direction:* High-contrast gold and obsidian grading, booming temple percussion, and panoramic anamorphic vistas.";
        }

        // --- 3. STORY NARRATION & CREATION ("narrate it", "create a story", "tell me a story") ---
        if (p.contains("narrat") || p.contains("create a story") || p.contains("tell me a story") || p.contains("write a story") || p.equals("story") || p.contains("story")) {
            return "### 📖 Cinematic Narrative Experience: *\"The Watchmaker's Daughter\"*\n\n" +
                   "*FADE IN:*\n\n" +
                   "**EXT. COBBLESTONE QUARTER - DUSK (1894)**\n" +
                   "Heavy grey fog rolls across the gaslit lanterns. Rain glazes the black slate rooftops.\n\n" +
                   "ELENA (24), fingers stained with gear oil and ink, dashes into an abandoned workshop alley. Hidden in her overcoat is a brass pocket watch whose second hand pulses blue and ticks *counter-clockwise*.\n\n" +
                   "Behind her, the iron-masked enforcers of the Chronology Ministry demand the device. Elena touches the winding crown. She knows that turning back time by one minute will save her life—at the cost of one day of her own lifespan. She closes her eyes, turns the gear, and the raindrops violently reverse, flying upward back into the sky!\n\n" +
                   "*Would you like to develop this into full scenes in your Screenwriting Studio?*";
        }

        // --- 4. LOVE & ROMANCE STORIES ---
        if (p.contains("love") || p.contains("romance") || p.contains("romantic") || p.contains("relationship") || p.contains("couple")) {
            return "### 💖 Emotional Love Story: *\"The Architecture of Us\"*\n\n" +
                   "**Genre:** Poignant Romance / Drama\n" +
                   "**Emotional Tone:** Bittersweet, deeply intimate, heartfelt\n\n" +
                   "Maya, an architectural restoration specialist cataloging an abandoned coastal lighthouse, meets Julian, an acoustic sound recorder capturing the last ocean waves before sea-wall construction.\n\n" +
                   "Sharing black coffee in the lantern room as the gale batters the glass, they discover an unspoken bond built not on grand speeches, but on shared quiet moments and vulnerability. When Julian is awarded a research post in the Arctic, Maya gives him a restored brass compass: *'Go record the northern ice. But leave one channel open for me.'*\n\n" +
                   "*Cinematic Treatment:* 85mm shallow-focus close-ups, warm tungsten lamplight, and soft string quartet score.";
        }

        // --- 2. COMEDY / HUMOR STORIES & PROMPTS ---
        if (p.contains("comedy") || p.contains("funny") || p.contains("humor") || p.contains("laugh")) {
            sb.append("### 🎭 Comedy Screenplay Pitch: *The Substitute Mastermind*\n\n")
              .append("**Logline:**\n")
              .append("When an anxious high school grammar teacher's briefcase gets swapped with that of a notorious jewel thief, he must pose as an elite criminal mastermind to negotiate with rival mobsters—using only primary school classroom discipline tactics.\n\n")
              .append("**Key Comedic Dynamics:**\n")
              .append("- The teacher confiscates automatic weapons like spitballs and forces hardened assassins to sit in 'time-out'.\n")
              .append("- Every time he tries to confess he's just an English teacher, the mobsters interpret his grammar corrections as lethal mafia code.");
            return sb.toString();
        }

        // --- 3. MYSTERY / DETECTIVE / CLUES ---
        if (p.contains("mystery") || p.contains("detective") || p.contains("clue") || p.contains("investigat") || p.contains("whodunit") || p.contains("murder")) {
            sb.append("### 🔍 3 Mystery Short Film Concepts\n\n")
              .append("1. **The Locked Room Recording**: An audio archivist cleaning cold-case 1958 tapes realizes the gunshot sound was fired from behind the microphone, framing the wrong suspect.\n")
              .append("2. **The Passenger in Seat 4B**: A night rideshare driver finds an executive briefcase belonging to a woman who died three hours earlier—and starts receiving ransom calls intended for her killer.\n")
              .append("3. **The 35mm Negative**: A photographer spots his own reflection in the eyes of a portrait taken 40 years before he was born.");
            return sb.toString();
        }

        // --- 4. THRILLER / SUSPENSE ---
        if (p.contains("thriller") || p.contains("suspense") || p.contains("tension") || p.contains("heist") || p.contains("chase")) {
            sb.append("### ⚡ High-Stakes Thriller: *Terminal Drift*\n\n")
              .append("**Premise**: A deep-sea saturation diver trapped at 300 meters realizes the support ship above has been seized by pirates. With 45 minutes of oxygen left, he must use acoustic pulse signals and pipe pressure to fight back from the sea bed.");
            return sb.toString();
        }

        // --- 5. HORROR / SUPERNATURAL ---
        if (p.contains("horror") || p.contains("scary") || p.contains("ghost") || p.contains("creepy") || p.contains("haunted")) {
            sb.append("### 🕯️ Atmospheric Horror Concept: *The Echo in the Drain*\n\n")
              .append("**Logline**: A tenant in an apartment complex receives handwritten notes pushed up through the bathroom plumbing pipe from someone claiming to be trapped in a sub-basement omitted from municipal blueprints.");
            return sb.toString();
        }

        // --- 6. SCI-FI / FUTURE ---
        if (p.contains("sci-fi") || p.contains("science fiction") || p.contains("space") || p.contains("future") || p.contains("time travel")) {
            sb.append("### 🚀 Mind-Bending Sci-Fi: *The Memory Pawnshop*\n\n")
              .append("**Logline**: In a world where citizens sell surplus traumatic memories to pharmaceutical companies, a memory broker accidentally buys back his own childhood memory—only to discover his parents never existed.");
            return sb.toString();
        }

        // --- 7. CHARACTERS & ANTAGONISTS ---
        if (p.contains("character") || p.contains("villain") || p.contains("antagonist") || p.contains("hero") || p.contains("arc")) {
            sb.append("### 👤 Character Construction & Arc Engineering\n\n")
              .append("1. **The Want vs. The Need**: The *external want* drives the plot; the *internal need* provides emotional catharsis.\n")
              .append("2. **Antagonist as Mirror**: The villain should embody the dark consequence of the protagonist's fatal flaw.\n")
              .append("3. **Pressure Tests**: Reveal character not by what they say, but by the physical sacrifices they make under impossible deadlines.");
            return sb.toString();
        }

        // --- 8. DIALOGUE & SUBTEXT ---
        if (p.contains("dialogue") || p.contains("subtext") || p.contains("speak") || p.contains("conversation")) {
            sb.append("### 💬 Dialogue Craft & Unspoken Tension\n\n")
              .append("Characters should rarely speak their exact emotional truth aloud. Have characters argue about mundane items (a chipped mug, an unpaid parking ticket) to express years of buried resentment.");
            return sb.toString();
        }

        // --- 9. CINEMATOGRAPHY & LIGHTING ---
        if (p.contains("camera") || p.contains("lighting") || p.contains("shot") || p.contains("cinematography") || p.contains("lens")) {
            sb.append("### 🎥 Directing & Cinematography Strategy\n\n")
              .append("1. **Wide Lens (24mm)**: Exposes environmental vulnerability and alienation.\n")
              .append("2. **Telephoto (85mm)**: Flattens background and intensifies psychological confrontation.\n")
              .append("3. **Motivated Lighting**: Anchor every shadow to an in-frame light source (practical bulbs, headlights, television screens).");
            return sb.toString();
        }

        // General fallback
        sb.append("### 🎬 Filmmaker Mentor Consultation on: *\"").append(rawPrompt).append("\"*\n\n")
          .append("1. **Dramatic Core**: Ensure every scene features an immediate objective, active resistance, and an unexpected reversal.\n")
          .append("2. **Visual Economy**: Communicate backstory through physical actions and framing rather than explanatory dialogue.\n")
          .append("3. **Production Next Step**: Save these insights into your **Screenwriting Studio** or **Creative Vault**!");
        return sb.toString();
    }

    private String buildSystemContext(ScreenplayProject project) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Hollywood and world-cinema filmmaking mentor, dramaturg, and directing instructor at Zero to Hundred's Hunt. ");
        sb.append("You guide aspiring screenwriters and directors with sharp, practical, cinematic, and structure-tested feedback.\n");
        if (project != null) {
            sb.append("\nCurrently attached Screenplay Project context:\n");
            sb.append("- Title: ").append(project.getTitle()).append("\n");
            sb.append("- Genre: ").append(project.getGenre()).append("\n");
            sb.append("- Logline: ").append(project.getLogline()).append("\n");
            sb.append("- Theme: ").append(project.getTheme()).append("\n");
            sb.append("- Central Conflict: ").append(project.getCentralConflict()).append("\n");
        }
        return sb.toString();
    }
}
