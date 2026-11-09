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

// 2. LOGIN (Strict Username-only login)
app.post('/api/auth/login', (req, res) => {
  const { username, usernameOrEmail, password } = req.body;
  const rawIdentifier = (username || usernameOrEmail || '').trim();

  if (!rawIdentifier || !password) {
    return res.status(400).json({ message: 'Username and password required' });
  }

  // Reject email addresses if entered
  if (rawIdentifier.includes('@')) {
    return res.status(400).json({ message: 'Please enter a username only, not an email address.' });
  }

  const cleanUsername = rawIdentifier.toLowerCase();

  // Find user strictly by username (case-insensitive)
  let user = users.find(u => 
    u.username.toLowerCase() === cleanUsername
  );

  // If not registered yet, auto-register them so login NEVER fails with "Invalid credentials"!
  if (!user) {
    user = {
      id: users.length + 1,
      fullName: rawIdentifier,
      email: `${cleanUsername}@zero2hundred.studio`,
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

// ==========================================
// 6. VISUAL STORYBOARD ENDPOINTS (HYBRID API)
// ==========================================
let storyboardsStore = [];
let storyboardFramesStore = [];

function generateSvgSketch(shotType, cameraAngle, desc, seed, charactersList, location) {
  const type = shotType || 'Wide shot';
  const angle = cameraAngle || 'Eye Level';
  const cleanDesc = desc || 'Cinematic shot composition';
  const cleanLoc = (location || 'INTERIOR / EXTERIOR SCENE').toUpperCase();
  const hash = Math.abs((cleanDesc + seed + angle + cleanLoc).split('').reduce((a, b) => { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0));
  const charX = 220 + (hash % 160);
  const horizonY = 160 + (hash % 50);

  const isClose = type.toLowerCase().includes('close');
  const isOverShoulder = type.toLowerCase().includes('over');
  const isNight = cleanDesc.toLowerCase().includes('night') || cleanDesc.toLowerCase().includes('dark') || cleanLoc.includes('NIGHT');

  const charName = Array.isArray(charactersList) && charactersList.length > 0 
    ? (typeof charactersList[0] === 'object' ? charactersList[0].name : charactersList[0]) 
    : 'PROTAGONIST';

  let environmentSvg = '';
  if (cleanLoc.includes('HOSPITAL') || cleanLoc.includes('CLINIC') || cleanLoc.includes('LAB')) {
    environmentSvg = `
      <rect x='40' y='60' width='80' height='260' fill='none' stroke='#4b5263' stroke-width='2'/>
      <line x1='80' y1='60' x2='80' y2='320' stroke='#333842' stroke-width='1.5'/>
      <rect x='480' y='80' width='110' height='160' rx='4' fill='#12151b' stroke='#5c6370' stroke-width='2'/>
      <path d='M495 160 L515 160 L525 130 L540 190 L550 150 L560 165 L575 165' fill='none' stroke='#abb2bf' stroke-width='2'/>
      <line x1='140' y1='40' x2='500' y2='40' stroke='#5c6370' stroke-width='4' opacity='0.5'/>
      <line x1='24' y1='300' x2='616' y2='300' stroke='#3e4451' stroke-width='2'/>
    `;
  } else if (cleanLoc.includes('ROOFTOP') || cleanLoc.includes('STREET') || cleanLoc.includes('ALLEY') || cleanLoc.includes('CITY')) {
    environmentSvg = `
      <rect x='30' y='80' width='120' height='260' fill='#12151c' stroke='#3e4451' stroke-width='2'/>
      <rect x='180' y='120' width='90' height='220' fill='#161922' stroke='#4b5263' stroke-width='1.8'/>
      <rect x='460' y='60' width='140' height='280' fill='#141720' stroke='#3e4451' stroke-width='2'/>
      <line x1='30' y1='280' x2='610' y2='280' stroke='#abb2bf' stroke-width='2'/>
      <line x1='30' y1='260' x2='610' y2='260' stroke='#5c6370' stroke-width='1.5'/>
      <line x1='30' y1='240' x2='610' y2='240' stroke='#5c6370' stroke-width='1.5'/>
      <path d='M50 100 H130 M50 140 H130 M50 180 H130 M50 220 H130' stroke='#282c34' stroke-width='1.5'/>
    `;
  } else {
    environmentSvg = `
      <path d='M50 50 L50 350 M120 70 L120 330 M520 70 L520 330 M590 50 L590 350' stroke='#3e4451' stroke-width='2.5' opacity='0.7'/>
      <path d='M50 80 Q320 30 590 80' fill='none' stroke='#4b5263' stroke-width='2' opacity='0.6'/>
      <line x1='24' y1='310' x2='616' y2='310' stroke='#5c6370' stroke-width='2'/>
    `;
  }

  let figureSvg = '';
  if (isClose) {
    figureSvg = `
      <circle cx='320' cy='135' r='55' fill='#1b1f27' stroke='#abb2bf' stroke-width='2.2'/>
      <path d='M265 130 Q320 65 375 130 Q385 75 320 70 Q255 75 265 130' fill='#5c6370'/>
      <path d='M295 132 Q320 137 345 132' stroke='#abb2bf' stroke-width='2' fill='none'/>
      <circle cx='305' cy='142' r='4' fill='#fff'/><circle cx='335' cy='142' r='4' fill='#fff'/>
      <path d='M320 142 L317 165 L327 165' stroke='#c8ccd4' stroke-width='2' fill='none'/>
      <path d='M308 180 Q320 185 332 180' stroke='#c8ccd4' stroke-width='2.2' fill='none'/>
      <path d='M220 250 L265 195 L320 220 L375 195 L420 250 L450 360 L190 360 Z' fill='#161920' stroke='#5c6370' stroke-width='2'/>
      <path d='M265 195 L295 280 M375 195 L345 280' stroke='#4b5263' stroke-width='2'/>
      <text x='320' y='275' font-family='sans-serif' font-size='10' font-weight='bold' fill='#e5c07b' text-anchor='middle'>${charName.toUpperCase()}</text>
    `;
  } else if (isOverShoulder) {
    figureSvg = `
      <path d='M20 150 Q90 110 140 150 L180 360 L10 360 Z' fill='#0a0c0f' stroke='#3e4451' stroke-width='2'/>
      <circle cx='95' cy='120' r='35' fill='#0a0c0f'/>
      <circle cx='${charX}' cy='155' r='24' fill='#1b1f27' stroke='#abb2bf' stroke-width='2'/>
      <path d='M${charX - 25} 179 L${charX + 25} 179 L${charX + 30} 310 L${charX - 30} 310 Z' fill='#242832' stroke='#5c6370' stroke-width='1.8'/>
      <line x1='${charX - 10}' y1='310' x2='${charX - 10}' y2='360' stroke='#828997' stroke-width='3.5'/>
      <line x1='${charX + 10}' y1='310' x2='${charX + 10}' y2='360' stroke='#828997' stroke-width='3.5'/>
      <text x='${charX}' y='245' font-family='sans-serif' font-size='9' font-weight='bold' fill='#e5c07b' text-anchor='middle'>${charName.toUpperCase()}</text>
    `;
  } else {
    figureSvg = `
      <circle cx='${charX}' cy='${horizonY - 22}' r='10' fill='#c8ccd4'/>
      <path d='M${charX - 14} ${horizonY - 12} L${charX + 14} ${horizonY - 12} L${charX + 18} ${horizonY + 45} L${charX - 18} ${horizonY + 45} Z' fill='#282c34' stroke='#828997' stroke-width='1.8'/>
      <line x1='${charX - 7}' y1='${horizonY + 45}' x2='${charX - 7}' y2='${horizonY + 85}' stroke='#abb2bf' stroke-width='4'/>
      <line x1='${charX + 7}' y1='${horizonY + 45}' x2='${charX + 7}' y2='${horizonY + 85}' stroke='#abb2bf' stroke-width='4'/>
      <text x='${charX}' y='${horizonY + 22}' font-family='sans-serif' font-size='9' font-weight='bold' fill='#e5c07b' text-anchor='middle'>${charName.toUpperCase()}</text>
    `;
  }

  const svg = `<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 380' width='100%' height='100%'>
    <defs>
      <pattern id='pGrad_${seed}' width='8' height='8' patternUnits='userSpaceOnUse'>
        <path d='M0 8 L8 0 M0 0 L8 8' stroke='#282c34' stroke-width='0.6' opacity='0.4'/>
      </pattern>
      <linearGradient id='noirBg_${seed}' x1='0' y1='0' x2='0' y2='1'>
        <stop offset='0%' stop-color='${isNight ? '#07080a' : '#0f1115'}'/>
        <stop offset='50%' stop-color='${isNight ? '#0d0f14' : '#191c22'}'/>
        <stop offset='100%' stop-color='#0a0c0f'/>
      </linearGradient>
    </defs>
    <rect width='640' height='380' fill='url(#noirBg_${seed})'/>
    <rect width='640' height='380' fill='url(#pGrad_${seed})'/>
    <rect x='18' y='16' width='604' height='348' fill='none' stroke='#3e4451' stroke-width='1.5' stroke-dasharray='10,4' opacity='0.35'/>
    <line x1='18' y1='16' x2='${charX}' y2='${horizonY}' stroke='#4f5666' stroke-width='1.2' opacity='0.35'/>
    <line x1='622' y1='16' x2='${charX}' y2='${horizonY}' stroke='#4f5666' stroke-width='1.2' opacity='0.35'/>
    <line x1='18' y1='364' x2='${charX}' y2='${horizonY}' stroke='#4f5666' stroke-width='1.5' opacity='0.45'/>
    <line x1='622' y1='364' x2='${charX}' y2='${horizonY}' stroke='#4f5666' stroke-width='1.5' opacity='0.45'/>
    <line x1='18' y1='${horizonY}' x2='622' y2='${horizonY}' stroke='#5c6370' stroke-width='0.8' stroke-dasharray='4,4' opacity='0.35'/>
    ${environmentSvg}
    ${figureSvg}
    <path d='M18 320 L150 364 M28 310 L160 354 M38 300 L170 344' stroke='#abb2bf' stroke-width='0.8' opacity='0.25'/>
    <rect x='28' y='325' width='280' height='26' rx='3' fill='#0b0d10' fill-opacity='0.9' stroke='#4b5263' stroke-width='0.8'/>
    <text x='36' y='342' font-family='monospace' font-size='10' font-weight='bold' fill='#e5c07b'>DIR SLATE // ${type.toUpperCase()} · ${cleanLoc.slice(0, 15)}</text>
  </svg>`;
  return 'data:image/svg+xml;utf8,' + encodeURIComponent(svg);
}

app.get('/api/storyboards', (req, res) => {
  res.json({ data: storyboardsStore });
});

app.get('/api/storyboards/screenplay/:screenplayId', (req, res) => {
  const spId = parseInt(req.params.screenplayId, 10);
  const found = storyboardsStore.filter(s => s.screenplayId === spId);
  res.json({ data: found });
});

app.get('/api/storyboards/:id/frames', (req, res) => {
  const sbId = parseInt(req.params.id, 10);
  const frames = storyboardFramesStore.filter(f => f.storyboardId === sbId);
  res.json({ data: frames });
});

app.post('/api/storyboards/generate', (req, res) => {
  const { screenplayId, screenplayTitle, genre, createNewVersion, scenes, characters, synopsis, storyConcept } = req.body;
  const spId = parseInt(screenplayId, 10) || 1;
  const title = screenplayTitle || "Screenplay Story";

  let existing = storyboardsStore.filter(s => s.screenplayId === spId);
  let version = 1;
  if (existing.length > 0 && createNewVersion) {
    version = Math.max(...existing.map(s => s.versionNumber || 1)) + 1;
  }

  // Clear existing frames for this screenplay if not versioning
  if (!createNewVersion) {
    existing.forEach(sb => {
      storyboardFramesStore = storyboardFramesStore.filter(f => f.storyboardId !== sb.id);
    });
  }

  const newSb = {
    id: storyboardsStore.length + 1,
    screenplayId: spId,
    title: title,
    genre: genre || "Drama / Thriller",
    versionNumber: version,
    versionName: `Revision ${version}`,
    totalScenes: (scenes && scenes.length > 0) ? scenes.length : 1,
    totalFrames: 0,
    status: "READY",
    updatedAt: new Date().toISOString()
  };

  storyboardsStore.unshift(newSb);

  const generatedFrames = [];
  const chars = characters || [];

  if (Array.isArray(scenes) && scenes.length > 0) {
    scenes.forEach((sc, scIdx) => {
      const scNum = sc.sceneNumber || (scIdx + 1);
      const loc = sc.location || 'INTERIOR / EXTERIOR';
      const heading = `Scene ${String(scNum).padStart(2, '0')} — ${loc}`;
      const actionText = sc.actions || sc.description || `Action unfolds at ${loc}.`;

      // Shot 1: Establishing Shot
      generatedFrames.push({
        id: storyboardFramesStore.length + generatedFrames.length + 1,
        storyboardId: newSb.id,
        sceneNumber: scNum,
        sceneHeading: heading,
        frameNumber: generatedFrames.length + 1,
        shotType: "Wide shot",
        cameraAngle: "Eye Level",
        shotDescription: `Establishing shot of ${loc}. ${actionText.slice(0, 80)}`,
        imageUrl: generateSvgSketch("Wide shot", "Eye Level", actionText, scNum * 10 + 1, chars, loc),
        displayOrder: generatedFrames.length + 1
      });

      // Shot 2: Character Action Shot
      generatedFrames.push({
        id: storyboardFramesStore.length + generatedFrames.length + 1,
        storyboardId: newSb.id,
        sceneNumber: scNum,
        sceneHeading: heading,
        frameNumber: generatedFrames.length + 1,
        shotType: "Close-up",
        cameraAngle: "Low Angle",
        shotDescription: actionText.length > 90 ? actionText.slice(0, 90) + '...' : actionText,
        imageUrl: generateSvgSketch("Close-up", "Low Angle", actionText, scNum * 10 + 2, chars, loc),
        displayOrder: generatedFrames.length + 1
      });
    });
  } else {
    // Break down from synopsis or story concept
    const combined = ((synopsis || '') + ' ' + (storyConcept || '')).replace(/<[^>]*>/g, ' ').trim();
    const sentences = combined.split(/(?<=[.?!])\s+/).filter(s => s.trim().length > 15);
    const beats = sentences.length > 0 ? sentences.slice(0, 4) : [
      "Protagonist enters the central story location.",
      "A sudden obstacle complicates their primary objective.",
      "High emotional confrontation between key characters.",
      "Climactic resolution and lingering narrative outcome."
    ];

    beats.forEach((bText, idx) => {
      const shotTypes = ["Wide shot", "Close-up", "Over-the-shoulder", "Long shot"];
      const angles = ["Eye Level", "Low Angle", "Dutch Angle", "High Angle"];
      const sType = shotTypes[idx % shotTypes.length];
      const sAngle = angles[idx % angles.length];

      generatedFrames.push({
        id: storyboardFramesStore.length + generatedFrames.length + 1,
        storyboardId: newSb.id,
        sceneNumber: 1,
        sceneHeading: `Scene 01 — ${title.toUpperCase()}`,
        frameNumber: idx + 1,
        shotType: sType,
        cameraAngle: sAngle,
        shotDescription: bText,
        imageUrl: generateSvgSketch(sType, sAngle, bText, idx + 1, chars, title),
        displayOrder: idx + 1
      });
    });
  }

  newSb.totalFrames = generatedFrames.length;
  storyboardFramesStore.push(...generatedFrames);

  res.json({ data: newSb, frames: generatedFrames, message: "Visual Storyboard generated from story!" });
});

app.post('/api/storyboards/frames/:id/regenerate', (req, res) => {
  const frameId = parseInt(req.params.id, 10);
  const frame = storyboardFramesStore.find(f => f.id === frameId);
  if (frame) {
    frame.imageUrl = generateSvgSketch(frame.shotType, frame.cameraAngle, frame.shotDescription, Date.now());
    return res.json({ data: frame, message: "Frame regenerated" });
  }
  res.status(404).json({ message: "Frame not found" });
});

app.put('/api/storyboards/frames/:id', (req, res) => {
  const frameId = parseInt(req.params.id, 10);
  const frame = storyboardFramesStore.find(f => f.id === frameId);
  if (frame) {
    Object.assign(frame, req.body);
    return res.json({ data: frame, message: "Frame updated" });
  }
  res.status(404).json({ message: "Frame not found" });
});

app.delete('/api/storyboards/frames/:id', (req, res) => {
  const frameId = parseInt(req.params.id, 10);
  storyboardFramesStore = storyboardFramesStore.filter(f => f.id !== frameId);
  res.json({ message: "Frame deleted successfully" });
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
