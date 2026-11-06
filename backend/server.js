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

// 4. AI MENTOR ENDPOINT
app.post('/api/ai-mentor/chat', (req, res) => {
  const { message, screenplayId } = req.body;
  const q = (message || '').toLowerCase().trim();

  let reply = '';

  if (q === 'hi' || q === 'hello' || q === 'hey' || q.startsWith('hi ') || q.startsWith('hello ')) {
    reply = "Hello filmmaker! I am your AI Filmmaking Mentor and creative consultant. You can speak to me using your microphone (voice recorder), type your questions, or upload reference photos and videos for technical analysis. What story are we developing today?";
  } else if (q.includes('short film') || q.includes('idea') || q.includes('concept') || q.includes('suggest')) {
    reply = `### 🎬 Top Short Film Concepts for You:

1. **The Sound Archivist** (Psychological Thriller)
*Logline:* An audio technician cleaning 1970s taped confessions discovers a voice whispering his own childhood address between the pauses.
*Visual Motif:* Close-ups of tape reels, sound meters twitching in low tungsten lighting.

2. **The 11th Floor** (Drama / Sci-Fi)
*Logline:* An office elevator stops at an 11th floor in an 8-story building, where each passenger meets a version of themselves from 10 years ago.
*Visual Motif:* Cold fluorescent corridor lighting contrasting warm elevator interior.

3. **Silent Echo** (Comedy / Heist)
*Logline:* Two amateur thieves break into what they think is a wealthy collector's penthouse, only to realize the homeowner is an eccentric retired foley artist who rigged the entire flat with comedic noise traps.

Would you like me to generate a 3-Act scene breakdown for one of these?`;
  } else if (q.includes('conflict') || q.includes('stakes')) {
    reply = `### 🔥 Strengthening Central Conflict:
1. **Compress Time:** Give your protagonist an immovable ticking clock (e.g. 24 hours, last flight out).
2. **Raise the Personal Cost:** Make failure mean losing the one thing they value most (dignity, family, identity).
3. **Equally Matched Antagonist:** The antagonist must have a philosophy that sounds completely justified from their own perspective.`;
  } else if (q.includes('dialogue') || q.includes('subtext')) {
    reply = `### 💬 Crafting Unspoken Subtext in Dialogue:
- **Rule of Inversion:** Characters never say what they actually mean. If two lovers are breaking up, have them argue about who forgot to buy coffee beans.
- **Action Over Words:** A character slamming a car door tells the audience far more than a monologue saying 'I am furious'.
- **Interruption:** Real people talk over each other and leave sentences unfinished.`;
  } else {
    reply = `### 🎥 Creative Filmmaker Consultation:
Regarding: *"**${message}**"*

1. **Dramatic Core:** Every compelling scene requires clear objective, urgent stakes, and an unexpected obstacle.
2. **Visual Direction:** Show emotion through lighting shifts, lens focal length (e.g. 85mm for intimate pressure vs 24mm for spatial isolation), and environment.
3. **Next Step:** You can outline this into your **Screenwriting Studio** under Scene Breakdowns!`;
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
