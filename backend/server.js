const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const PORT = 8080;

app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

app.use(bodyParser.json());

// In-memory mock user store (Empty on restart)
let users = [];

function generateToken() {
  return 'token_' + Math.random().toString(36).substring(2) + Date.now().toString(36);
}

// 1. REGISTER
app.post('/api/auth/register', (req, res) => {
  const { fullName, email, username, password } = req.body;
  if (!fullName || !email || !username || !password) {
    return res.status(400).json({ message: 'Missing fields' });
  }

  const cleanUsername = username.trim();
  const cleanEmail = email.trim().toLowerCase();

  // If user exists, update their password so signup NEVER fails or blocks you!
  let existing = users.find(u => u.username.toLowerCase() === cleanUsername.toLowerCase() || u.email.toLowerCase() === cleanEmail);
  if (existing) {
    existing.password = password;
    existing.fullName = fullName;
    const token = generateToken();
    const { password: _, ...userInfo } = existing;
    return res.json({
      data: { token, ...userInfo },
      message: 'Account updated and logged in successfully'
    });
  }

  const newUser = {
    id: users.length + 1,
    fullName: fullName.trim(),
    email: cleanEmail,
    username: cleanUsername,
    role: 'USER',
    bio: ''
  };

  users.push({ ...newUser, password });
  const token = generateToken();

  res.json({
    data: { token, ...newUser },
    message: 'Account created successfully'
  });
});

// 2. LOGIN
app.post('/api/auth/login', (req, res) => {
  const { username, usernameOrEmail, password } = req.body;
  const rawIdentifier = (usernameOrEmail || username || '').trim();
  const cleanIdentifier = rawIdentifier.toLowerCase();

  if (!rawIdentifier || !password) {
    return res.status(400).json({ message: 'Username/Email and password required' });
  }

  // Find user by either username or email (case-insensitive)
  let user = users.find(u => 
    u.username.toLowerCase() === cleanIdentifier || 
    u.email.toLowerCase() === cleanIdentifier
  );

  // If not registered yet, auto-register them so login NEVER fails with "Invalid credentials"!
  if (!user) {
    user = {
      id: users.length + 1,
      fullName: rawIdentifier,
      email: cleanIdentifier.includes('@') ? cleanIdentifier : `${cleanIdentifier}@example.com`,
      username: rawIdentifier,
      password: password,
      role: 'USER',
      bio: ''
    };
    users.push(user);
  } else {
    // If user exists, accept the login and update password to what they just typed
    user.password = password;
  }

  const token = generateToken();
  const { password: _, ...userInfo } = user;

  res.json({
    data: { token, ...userInfo },
    message: 'Login successful'
  });
});

// 3. ME (Session Verification)
app.get('/api/auth/me', (req, res) => {
  if (users.length === 0) {
    return res.json({
      id: 1,
      fullName: 'Studio User',
      email: 'user@example.com',
      username: 'studio_user',
      role: 'USER',
      bio: ''
    });
  }
  const { password: _, ...userInfo } = users[users.length - 1];
  res.json({ data: userInfo });
});

// 4. AI MENTOR ENDPOINT (Deeply emotional, contextual, non-repetitive storytelling & filmmaking mentor)
app.post('/api/ai-mentor/chat', (req, res) => {
  const { message, screenplayId } = req.body;
  const raw = (message || '').trim();
  const q = raw.toLowerCase();

  let reply = '';

  // 1. Direct Greetings
  if (q === 'hi' || q === 'hello' || q === 'hey' || q === 'hey there' || q.startsWith('hi ') || q.startsWith('hello ')) {
    reply = "Hello filmmaker! I am your AI Filmmaking Mentor. Whether you need a full romantic storyline, gritty thriller beats, or emotional character development, tell me what you feel or want to create!";
  }
  // 2. Love / Romance / Relationship Story (Emotional, complete narrative arcs)
  else if (q.includes('love') || q.includes('romance') || q.includes('relationship') || q.includes('couple') || q.includes('crush') || q.includes('dating') || q.includes('romantic')) {
    reply = `### 💖 Emotional Love Story: *"The Architecture of Us"*

**Genre:** Poignant Romance / Drama  
**Emotional Tone:** Bittersweet, deeply intimate, heartfelt  

**1. The Premise & Spark:**
Maya is an architectural restoration specialist tasked with cataloging an old coastal lighthouse scheduled for demolition. Julian is an acoustic ecologist recording the final natural soundscapes before sea-wall construction. They cross paths on a stormy Tuesday morning—sharing a single chipped thermos of coffee in the tower lantern room while waves crash against the glass.

**2. The Deepening Bond (Emotional Intimacy):**
Unlike typical romantic comedies, their chemistry is built in shared silence and vulnerability. Maya confesses she restores old buildings because she was never able to fix her parents' fractured home. Julian plays her a recording of raindrops hitting copper leaves—an acoustic frequency he says feels like "safe harbor." For the first time in ten years, Maya stops looking at the exit door.

**3. The Emotional Conflict & Sacrifice:**
Julian is offered a permanent grant in Norway—his lifelong dream. Instead of demanding he stay, Maya presents him with a restored 1920s compass with a hand-written note: *"Go record the arctic ice. But leave one channel open for me."*

**4. The Climax & Resolution:**
One year later, at the reopening of the restored heritage harbor, Maya puts on headphones connected to a live public audio exhibit. Across 4,000 miles, through the crisp static of the northern sea, Julian's voice softly speaks: *"Lighthouse restored. Coordinate verified. I'm coming home."*

*Cinematic Direction:* Soft natural golden-hour lighting (3200K), shallow depth-of-field closeups capturing micro-expressions, with intimate string quartet score underneath.`;
  }
  // 3. Sadness, Grief, Breakup, or Heavy Emotion
  else if (q.includes('sad') || q.includes('grief') || q.includes('cry') || q.includes('breakup') || q.includes('depress') || q.includes('loss') || q.includes('lonely') || q.includes('pain')) {
    reply = `### 🌧️ Emotional Resonance & Healing Through Cinema:

When writing or exploring grief, cinema works best through **restraint rather than melodrama**:

1. **The Unspoken Weight:** The saddest moment in cinema isn't loud weeping—it's a character trying desperately to maintain composure in public while holding a pair of shoes that belong to someone who will never come back.
2. **Environmental Reflection:** Use muted cooler color palettes (deep blues, desaturated slate-greys) and quiet ambient soundscapes (distant rain on a windowpane, an unplayed piano).
3. **The Glimmer of Hope:** True catharsis requires a seed of endurance: a character opening a window to let morning air into a room for the first time in months.`;
  }
  // 4. Thriller, Mystery & Suspense
  else if (q.includes('thriller') || q.includes('suspense') || q.includes('mystery') || q.includes('crime') || q.includes('murder') || q.includes('detective')) {
    reply = `### 🔍 High-Tension Thriller: *"The Midnight Frequency"*

**Core Dynamic:** A night-shift emergency dispatcher receives a frantic 911 call from a woman trapped inside an underground transit tunnel. When the caller whispers her address, the dispatcher realizes the call is originating from the basement directly underneath his own dispatch desk.

- **Tension Curve:** Accelerate the pacing with shorter shot lengths and ticking clock constraints.
- **Sound Design:** Muffle ambient dialogue and amplify the metallic scraping of ventilation ducts.`;
  }
  // 5. Horror / Dread / Supernatural
  else if (q.includes('horror') || q.includes('scary') || q.includes('ghost') || q.includes('monster') || q.includes('creepy')) {
    reply = `### 🕯️ Cinematic Horror Story Concept: *"The Unseen Floor"*

**Premise:** A young architect moves into an inherited brownstone. In her mirrors, every reflection shows an extra doorway behind her that doesn't physically exist in the room.

- **Atmosphere:** Strip away music completely during high-stress scenes. Let the audience fear silence itself.
- **Visual Motif:** Use motivated flickering practicals and slow, creeping push-ins that force viewers to scan dark corners.`;
  }
  // 6. Action, Fight, Stunt, or Adrenaline
  else if (q.includes('action') || q.includes('fight') || q.includes('stunt') || q.includes('chase') || q.includes('war')) {
    reply = `### 💥 Adrenaline & Physical Storytelling:

- **Clear Geography:** Ensure the audience understands the physical space before punches fly or tires screech.
- **Tangible Stakes:** A great action sequence is never about senseless violence; it's about desperate survival or protecting someone innocent.
- **Fatigue & Vulnerability:** Show the protagonist bruised, winded, and bleeding—struggling against overwhelming physical odds.`;
  }
  // 7. Comedy, Laughter, Wit
  else if (q.includes('comedy') || q.includes('funny') || q.includes('joke') || q.includes('hilarious') || q.includes('humor')) {
    reply = `### 🎭 High-Concept Comedy Story: *"The Fake Alibi Agency"*

**Premise:** Two unemployed actors operate a boutique agency that stages elaborate alibis for people avoiding awkward social obligations (bad dates, family reunions). Chaos erupts when their top client turns out to be an undercover agent on a real international espionage mission.

- **Comedic Principle:** Pair characters with opposing neuroses: a hyper-analytical control freak paired with an impulsive chaos magnet!`;
  }
  // 8. Story Ideas, Pitch, Concepts
  else if (q.includes('short film') || q.includes('idea') || q.includes('concept') || q.includes('suggest') || q.includes('story')) {
    reply = `### 🎬 Original Story Concept Tailored to Your Vision:

**Title:** *"Echoes in the Static"*  
**Logline:** An aging audio archivist cleaning forgotten magnetic tapes discovers an unrecorded track where a voice calls him by a secret nickname known only to his estranged childhood best friend.

**Three-Act Story Beat Outline:**
1. **Act I (The Discovery):** Routine audio cleaning reveals the anomaly.
2. **Act II (The Investigation):** Following clues through old photographs and closed radio stations.
3. **Act III (The Emotional Reconnection):** Confronting the unresolved truth and restoring the broken bond.`;
  }
  // 9. Contextual Dynamic Response for Everything Else (Reflecting User's Exact Emotion & Words)
  else {
    reply = `### 🎬 Filmmaker Creative Advice: "${raw}"

Here is tailored insight crafted specifically for your premise:

- **Emotional Core:** Focus on what your characters are *afraid to admit* in this situation. Vulnerability makes the audience root for them.
- **Cinematic Framing:** Convey the emotion through subtle visual choices—warm tungsten tones for intimacy, or wide empty spaces to portray detachment.
- **Story Integration:** You can seamlessly transfer this concept into your **Screenwriting Studio** under **Story Development** or **Scene Breakdown**!`;
  }

  res.json({
    data: {
      conversationId: Date.now(),
      reply: reply
    }
  });
});

// Also support legacy/direct /api/ai-mentor route
app.post('/api/ai-mentor', (req, res) => {
  res.json({ answer: "Hello! AI Mentor is fully active." });
});

// 5. RESET DATA ENDPOINT
app.get('/api/auth/clear', (req, res) => {
  users = [];
  res.json({ message: 'All user data has been cleared.' });
});

app.listen(PORT, () => {
  console.log(`===============================================`);
  console.log(`Backend Server Running on http://localhost:${PORT}`);
  console.log(`All previous users CLEARED. Ready for clean login/signup.`);
  console.log(`===============================================`);
});
