package com.zerohundreds.hunt.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StoryChartGenerationService {

    /**
     * CHART 1: Story Structure Chart (Setup, Inciting Incident, Rising Action, Climax, Falling Action, Resolution)
     */
    public String generateStoryStructureChart(String title, List<String> beats) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph LR\n");
        sb.append("  classDef actNode fill:#1f242d,stroke:#e5c07b,stroke-width:2px,color:#ffffff;\n");
        sb.append("  classDef climaxNode fill:#e06c75,stroke:#ffffff,stroke-width:2px,color:#ffffff;\n");

        if (beats == null || beats.isEmpty()) {
            sb.append("  A[\"1. Setup & Introduction\"] --> B[\"2. Inciting Incident\"];\n");
            sb.append("  B --> C[\"3. Rising Action & Obstacles\"];\n");
            sb.append("  C --> D[\"4. Story Climax & Showdown\"]:::climaxNode;\n");
            sb.append("  D --> E[\"5. Falling Action\"];\n");
            sb.append("  E --> F[\"6. Resolution & Aftermath\"];\n");
        } else {
            for (int i = 0; i < beats.size(); i++) {
                String clean = sanitize(beats.get(i));
                String nodeId = "N" + i;
                boolean isClimax = (i == beats.size() / 2) || clean.toLowerCase().contains("climax");
                sb.append("  ").append(nodeId).append("[\"").append(i + 1).append(". ").append(clean).append("\"]");
                if (isClimax) sb.append(":::climaxNode");
                else sb.append(":::actNode");
                sb.append(";\n");

                if (i > 0) {
                    sb.append("  N").append(i - 1).append(" --> ").append(nodeId).append(";\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * CHART 2: Character Relationship Map (Nodes with labeled connection edges)
     */
    public String generateCharacterRelationshipMap(List<Map<String, String>> relationships) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");
        sb.append("  classDef charNode fill:#14171d,stroke:#61afef,stroke-width:2px,color:#ffffff;\n");
        sb.append("  classDef enemyNode fill:#2d161a,stroke:#e06c75,stroke-width:2px,color:#ffffff;\n");

        if (relationships == null || relationships.isEmpty()) {
            sb.append("  Protagonist[\"Protagonist (Lead)\"]:::charNode;\n");
            sb.append("  Partner[\"Partner / Confidant\"]:::charNode;\n");
            sb.append("  Antagonist[\"Antagonist (Rival)\"]:::enemyNode;\n");
            sb.append("  Protagonist -- \"Loyal Alliance\" --> Partner;\n");
            sb.append("  Protagonist -- \"Direct Conflict\" --> Antagonist;\n");
        } else {
            int edgeIdx = 0;
            for (Map<String, String> rel : relationships) {
                String from = sanitizeId(rel.getOrDefault("from", "CharA"));
                String to = sanitizeId(rel.getOrDefault("to", "CharB"));
                String type = sanitize(rel.getOrDefault("relationship", "Allies"));
                boolean isConflict = type.toLowerCase().contains("conflict") || type.toLowerCase().contains("enemy") || type.toLowerCase().contains("rival");

                sb.append("  ").append(from).append("[\"").append(sanitize(rel.getOrDefault("from", "CharA"))).append("\"]");
                if (isConflict) sb.append(":::enemyNode"); else sb.append(":::charNode");
                sb.append(";\n");

                sb.append("  ").append(to).append("[\"").append(sanitize(rel.getOrDefault("to", "CharB"))).append("\"]");
                sb.append(":::charNode;\n");

                sb.append("  ").append(from).append(" -- \"").append(type).append("\" --> ").append(to).append(";\n");
            }
        }
        return sb.toString();
    }

    /**
     * CHART 3: Chronological Story Timeline
     */
    public String generateStoryTimeline(List<Map<String, String>> timelineEvents) {
        StringBuilder sb = new StringBuilder();
        sb.append("timeline\n");
        sb.append("  title Story Narrative Chronology\n");

        if (timelineEvents == null || timelineEvents.isEmpty()) {
            sb.append("  Opening : Arrival & Setting : Initial Status Quo\n");
            sb.append("  Disruption : Mystery Revelation : First Encounter\n");
            sb.append("  Turning Point : High Tension Consequence : Major Choice\n");
            sb.append("  Climax : The Final Confrontation\n");
            sb.append("  Ending : New Reality & Resolution\n");
        } else {
            for (Map<String, String> ev : timelineEvents) {
                String phase = sanitize(ev.getOrDefault("phase", "Phase"));
                String eventText = sanitize(ev.getOrDefault("event", "Key story milestone"));
                sb.append("  ").append(phase).append(" : ").append(eventText).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * CHART 4: Scene Transition Flowchart
     */
    public String generateSceneTransitionFlowchart(List<Map<String, String>> scenesList) {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TD\n");
        sb.append("  classDef sceneCard fill:#181b22,stroke:#98c379,stroke-width:1.5px,color:#ffffff;\n");

        if (scenesList == null || scenesList.isEmpty()) {
            sb.append("  S1[\"Scene 01: The Arrival\"]:::sceneCard --> |\"Cut To\"| S2[\"Scene 02: Encounter\"]:::sceneCard;\n");
            sb.append("  S2 --> |\"Dissolve\"| S3[\"Scene 03: The Climax\"]:::sceneCard;\n");
        } else {
            for (int i = 0; i < scenesList.size(); i++) {
                Map<String, String> sc = scenesList.get(i);
                String id = "SC_" + i;
                String heading = sanitize(sc.getOrDefault("heading", "Scene " + (i + 1)));
                String trans = sanitize(sc.getOrDefault("transition", "Cut To"));

                sb.append("  ").append(id).append("[\"").append(heading).append("\"]:::sceneCard;\n");
                if (i > 0) {
                    String prevId = "SC_" + (i - 1);
                    sb.append("  ").append(prevId).append(" --> |\"").append(trans).append("\"| ").append(id).append(";\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * CHART 5: Character Emotional Arc
     */
    public String generateEmotionalArcGraph(List<Map<String, String>> emotionalBeats) {
        StringBuilder sb = new StringBuilder();
        sb.append("xychart-beta\n");
        sb.append("  title \"Protagonist Emotional Tension Arc\"\n");
        sb.append("  x-axis [\"Setup\", \"Inciting\", \"Rising Action\", \"Midpoint\", \"Dark Night\", \"Climax\", \"Resolution\"]\n");
        sb.append("  y-axis \"Tension / Emotional Intensity (1-10)\" 0 --> 10\n");
        sb.append("  bar [2, 4, 6, 7, 3, 10, 5]\n");
        sb.append("  line [2, 4, 6, 7, 3, 10, 5]\n");
        return sb.toString();
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("\"", "'").replace("\n", " ").trim();
    }

    private String sanitizeId(String input) {
        if (input == null) return "Node";
        return input.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
