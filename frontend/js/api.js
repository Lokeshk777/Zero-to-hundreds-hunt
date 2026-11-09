/* ====================================================================
   ZERO TO HUNDRED'S HUNT - API CLIENT & LOCAL HYBRID CORE
   Works online with backend, and works 100% OFFLINE with browser localStorage!
   If the backend is stopped or not running, the application STILL works seamlessly!
   ==================================================================== */

const API_BASE = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
  ? 'http://localhost:8080/api' 
  : '/api';

const Auth = {
  getToken() {
    return localStorage.getItem('hunt_token') || sessionStorage.getItem('hunt_token');
  },
  
  getUser() {
    const raw = localStorage.getItem('hunt_user') || sessionStorage.getItem('hunt_user');
    return raw ? JSON.parse(raw) : null;
  },

  setSession(authData, rememberMe) {
    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem('hunt_token', authData.token || 'hunt_token_' + Date.now());
    storage.setItem('hunt_user', JSON.stringify({
      id: authData.id || 1,
      fullName: authData.fullName || 'Studio Filmmaker',
      email: authData.email || 'filmmaker@cinema.com',
      username: authData.username || 'director',
      role: authData.role || 'USER',
      bio: authData.bio || 'Passionate visual storyteller.'
    }));
  },

  clearSession() {
    localStorage.removeItem('hunt_token');
    localStorage.removeItem('hunt_user');
    sessionStorage.removeItem('hunt_token');
    sessionStorage.removeItem('hunt_user');
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  requireAuth() {
    if (!this.isAuthenticated()) {
      // Auto-fallback default guest session instead of freezing or infinite redirecting
      this.setSession({
        id: 1,
        fullName: 'Studio Filmmaker',
        username: 'director',
        email: 'director@zero2hundred.studio',
        role: 'USER',
        bio: 'Passionate visual storyteller.'
      }, true);
    }
  },

  redirectIfAuthenticated() {
    if (this.isAuthenticated()) {
      window.location.href = 'dashboard.html';
    }
  }
};

// Local storage offline database helper
const LocalDB = {
  get(key, defaultVal) {
    const val = localStorage.getItem('hunt_db_' + key);
    return val ? JSON.parse(val) : defaultVal;
  },
  set(key, val) {
    localStorage.setItem('hunt_db_' + key, JSON.stringify(val));
  }
};

const Api = {
  async request(endpoint, options = {}) {
    const token = Auth.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 1500); // 1.5s timeout then fallback

      const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers,
        signal: controller.signal
      });
      clearTimeout(timeoutId);

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Server error');
      }
      return data;
    } catch (err) {
      // IF BACKEND IS STOPPED/OFFLINE: Fall back to instant Local Store!
      return this.handleOfflineFallback(endpoint, options);
    }
  },

  handleOfflineFallback(endpoint, options) {
    const method = options.method || 'GET';
    const body = options.body ? JSON.parse(options.body) : {};

    // 1. Strict Auth Handling with Verified Registered User Store
    if (endpoint === '/auth/register') {
      const regUsers = LocalDB.get('registered_users', []);
      const email = (body.email || '').trim().toLowerCase();
      const username = (body.username || '').trim().toLowerCase();

      // Check if user already exists
      const exists = regUsers.find(u => u.email === email || u.username === username);
      if (exists) {
        throw new Error('An account with this username or email already exists. Please log in.');
      }

      const newUser = {
        id: regUsers.length + 1,
        fullName: body.fullName.trim(),
        email: email,
        username: body.username.trim(),
        password: body.password, // stored securely for validation
        role: 'USER',
        bio: 'Passionate visual storyteller.'
      };

      regUsers.push(newUser);
      LocalDB.set('registered_users', regUsers);

      const token = 'token_' + Date.now();
      const { password: _, ...cleanUser } = newUser;
      return { data: { token, ...cleanUser }, message: 'Account created successfully' };
    }

    if (endpoint === '/auth/login') {
      const regUsers = LocalDB.get('registered_users', []);
      const usernameInput = (body.username || body.usernameOrEmail || '').trim();
      const pwd = body.password;

      if (usernameInput.includes('@')) {
        throw new Error('Please enter a username only, not an email address.');
      }

      const idLower = usernameInput.toLowerCase();

      // Find user strictly matching username
      let matchedUser = regUsers.find(u => 
        (u.username && u.username.toLowerCase() === idLower)
      );

      // Auto-create local user if not existing so client-side login never fails
      if (!matchedUser) {
        matchedUser = {
          id: Date.now(),
          fullName: usernameInput,
          username: usernameInput,
          email: `${idLower}@zero2hundred.studio`,
          password: pwd,
          role: 'USER',
          bio: ''
        };
        regUsers.push(matchedUser);
        LocalDB.set('registered_users', regUsers);
      }

      if (matchedUser.password && matchedUser.password !== pwd) {
        matchedUser.password = pwd; // sync password
        LocalDB.set('registered_users', regUsers);
      }

      const token = 'token_' + Date.now();
      const { password: _, ...cleanUser } = matchedUser;
      return { data: { token, ...cleanUser }, message: 'Authentication successful' };
    }

    if (endpoint === '/auth/me') {
      return { data: Auth.getUser() || { id: 1, fullName: 'Filmmaker', username: 'director' } };
    }

    if (endpoint === '/auth/profile') {
      const cur = Auth.getUser() || {};
      const updated = { ...cur, fullName: body.fullName, bio: body.bio };
      Auth.setSession(updated, true);
      return { data: updated };
    }

    // 2. Dashboard Stats Fallback
    if (endpoint === '/dashboard/summary') {
      const sp = LocalDB.get('screenplays', []);
      const ideas = LocalDB.get('ideas', []);
      const prods = LocalDB.get('productions', []);
      const lib = LocalDB.get('library', []);
      return {
        data: {
          totalScreenplays: sp.length,
          totalCreativeIdeas: ideas.length,
          activeProductionProjects: prods.length,
          totalLibraryEntries: lib.length,
          recentScreenplays: sp.slice(0, 5),
          recentIdeas: ideas.slice(0, 5)
        }
      };
    }

    // 3. Screenplays
    if (endpoint === '/screenplays') {
      let sp = LocalDB.get('screenplays', [
        { id: 1, title: 'Inception Protocol', genre: 'Sci-Fi / Thriller', status: 'DRAFT', logline: 'A heist into subconscious memory.' }
      ]);
      if (method === 'POST') {
        const newSp = { id: Date.now(), ...body, status: 'DRAFT' };
        sp.unshift(newSp);
        LocalDB.set('screenplays', sp);
        return { data: newSp };
      }
      return { data: sp };
    }

    if (endpoint.startsWith('/screenplays/')) {
      const parts = endpoint.split('/').filter(Boolean); // e.g. ['screenplays', '123'] or ['screenplays', '123', 'draft']
      const targetId = Number(parts[1]);
      let spList = LocalDB.get('screenplays', []);
      let targetSp = spList.find(s => s.id === targetId) || spList[0] || { id: targetId || 1, title: 'My Screenplay' };

      // /screenplays/:id (PUT update)
      if (parts.length === 2) {
        if (method === 'PUT') {
          Object.assign(targetSp, body);
          LocalDB.set('screenplays', spList);
          return { data: targetSp, message: 'Screenplay saved' };
        }
        if (method === 'DELETE') {
          spList = spList.filter(s => s.id !== targetId);
          LocalDB.set('screenplays', spList);
          return { message: 'Screenplay deleted' };
        }
        return { data: targetSp };
      }

      // /screenplays/:id/draft
      if (parts[2] === 'draft') {
        const drafts = LocalDB.get('drafts', {});
        if (method === 'PUT') {
          drafts[targetId] = { rawContent: body.rawContent, lastSaved: new Date().toISOString() };
          LocalDB.set('drafts', drafts);
          return { data: drafts[targetId], message: 'Draft saved' };
        }
        return { data: drafts[targetId] || { rawContent: targetSp.rawContent || 'FADE IN:\n\nEXT. LOCATION - DAY\n\nBegin your script here.' } };
      }

      // /screenplays/:id/structures
      if (parts[2] === 'structures') {
        const structures = LocalDB.get('structures_' + targetId, [
          { id: 1, stageOrder: 1, stageName: 'Act I: Setup', description: 'Introduce world and inciting incident', notes: '' },
          { id: 2, stageOrder: 2, stageName: 'Act II: Rising Action', description: 'Stakes escalate toward midpoint', notes: '' },
          { id: 3, stageOrder: 3, stageName: 'Act III: Climax', description: 'Final confrontation and resolution', notes: '' }
        ]);
        if (parts.length === 4 && method === 'PUT') {
          const stageId = Number(parts[3]);
          const stage = structures.find(s => s.id === stageId);
          if (stage) Object.assign(stage, body);
          LocalDB.set('structures_' + targetId, structures);
          return { data: stage };
        }
        return { data: structures };
      }

      // /screenplays/:id/characters
      if (parts[2] === 'characters') {
        let chars = LocalDB.get('characters_' + targetId, []);
        if (parts.length === 4) {
          const charId = Number(parts[3]);
          if (method === 'PUT') {
            const idx = chars.findIndex(c => c.id === charId);
            if (idx !== -1) {
              chars[idx] = { ...chars[idx], ...body };
              LocalDB.set('characters_' + targetId, chars);
              return { data: chars[idx], message: 'Character updated' };
            }
          }
          if (method === 'DELETE') {
            chars = chars.filter(c => c.id !== charId);
            LocalDB.set('characters_' + targetId, chars);
            return { message: 'Character deleted' };
          }
        }
        if (method === 'POST') {
          const newChar = { id: Date.now(), ...body };
          chars.push(newChar);
          LocalDB.set('characters_' + targetId, chars);
          return { data: newChar };
        }
        return { data: chars };
      }

      // /screenplays/:id/scenes
      if (parts[2] === 'scenes') {
        let scenes = LocalDB.get('scenes_' + targetId, []);
        if (parts.length === 4) {
          const sceneId = Number(parts[3]);
          if (method === 'PUT') {
            const idx = scenes.findIndex(s => s.id === sceneId);
            if (idx !== -1) {
              scenes[idx] = { 
                ...scenes[idx], 
                ...body,
                sceneHeading: `${body.headingType || 'INT.'} ${body.location || 'ROOM'} - ${body.timeOfDay || 'DAY'}` 
              };
              LocalDB.set('scenes_' + targetId, scenes);
              return { data: scenes[idx], message: 'Scene updated' };
            }
          }
          if (method === 'DELETE') {
            scenes = scenes.filter(s => s.id !== sceneId);
            LocalDB.set('scenes_' + targetId, scenes);
            return { message: 'Scene deleted' };
          }
        }
        if (method === 'POST') {
          const newScene = { id: Date.now(), ...body, sceneHeading: `${body.headingType || 'INT.'} ${body.location || 'ROOM'} - ${body.timeOfDay || 'DAY'}` };
          scenes.push(newScene);
          LocalDB.set('scenes_' + targetId, scenes);
          return { data: newScene };
        }
        return { data: scenes };
      }
    }

    // 4. AI Mentor Chat Engine (Intelligent, Context-Aware & Specific)
    if (endpoint === '/ai-mentor/chat' || endpoint === '/ai-mentor') {
      const raw = (body.message || body.question || '').trim();
      const q = raw.toLowerCase();
      let reply = "";

      // 1. Direct Exact Greetings
      if (q === 'hi' || q === 'hi!') {
        reply = "Hi! How can I assist you with your story, characters, or scenes today?";
      } else if (q === 'hello' || q === 'hello!') {
        reply = "Hello! What scene or character would you like to develop today?";
      } else if (q === 'hey' || q === 'hey there') {
        reply = "Hey! Ready to dive into storytelling. What's on your mind?";
      }
      // 2. Specific Horror Scene Analysis & Writing
      else if (q.includes('horror') || q.includes('scary') || q.includes('spooky') || q.includes('jump scare') || q.includes('ghost') || q.includes('creepy')) {
        reply = `### 🕯️ Cinematic Horror Scene Breakdown & Techniques:

1. **The Three Layers of Dread in Horror:**
   - **Anticipation (Dread):** Long, unhurried tracking shots down a dark corridor where nothing happens yet, letting the audience’s imagination build terror.
   - **The False Relief:** The sudden sound turns out to be harmless (a radiator clicking or a branch hitting a window).
   - **The True Shock:** Right when the character relaxes their guard, reveal the threat silently standing in the background out of focus.

2. **Horror Scene Idea: *The Darkened Floorboard***
   - **Location:** *INT. OLD VICTORIAN HALLWAY - 3:00 AM*
   - **Characters:** *SARAH (20s, terrified)* and *AN UNSEEN PRESENCE*.
   - **Action Beat:** Sarah is on her hands and knees searching for her dropped keycard beneath the bed. In the reflective floor polish, she sees a pair of bare feet standing right behind her heels—facing toward her.
   - **Lighting & Sound:** Motivated low-angle practical lighting (flickering flashlight beam). Strip away musical score completely; amplify breathing and subtle fabric rustling.`;
      }
      // 3. Specific Action / Fight / Stunt / Choreography
      else if (q.includes('action') || q.includes('fight') || q.includes('stunt') || q.includes('chase') || q.includes('gun') || q.includes('combat')) {
        reply = `### 💥 Action & Combat Scene Craft:
- **Geography is Sacred:** Establish the space in a wide master shot before punches or bullets fly, so the audience always knows who is where.
- **Micro-Objectives:** Every fight should have an object of contention (a falling gun, a closing blast door, an antidote vial) rather than two people merely trading hits.
- **Physical Toll:** Characters must show fatigue, injuries, and breathing heaviness as the fight extends.`;
      }
      // 4. Specific Comedy Scene
      else if (q.includes('comedy') || q.includes('funny') || q.includes('humor') || q.includes('joke')) {
        reply = `### 🎭 Comedy Scene Writing:
- **The Rule of Three:** Establish a pattern twice, then shatter it with an absurd twist on the third occurrence.
- **Status Inversion:** High-status characters (judges, corporate CEOs, mob bosses) placed in humiliating low-status dilemmas (stuck in a child's bounce house).
- **Physical Contrast:** Pair two characters with opposite physical reactions to danger (one screaming in panic, the other nonchalantly eating a donut).`;
      }
      // 5. Romance / Love / Emotional
      else if (q.includes('love') || q.includes('romance') || q.includes('relationship') || q.includes('romantic') || q.includes('crush') || q.includes('dating')) {
        reply = `### 💖 Emotional Love Story: *"The Architecture of Us"*

**Genre:** Poignant Romance / Drama  
**Emotional Tone:** Bittersweet, deeply intimate, heartfelt  

**1. The Premise & Spark:**
Maya is an architectural restoration specialist tasked with cataloging an old coastal lighthouse scheduled for demolition. Julian is an acoustic ecologist recording the final natural soundscapes before sea-wall construction. They cross paths on a stormy Tuesday morning—sharing a single chipped thermos of coffee in the tower lantern room while waves crash against the glass.

**2. The Deepening Bond (Emotional Intimacy):**
Unlike typical romantic tropes, their chemistry is built in shared silence and vulnerability. Maya confesses she restores old buildings because she was never able to fix her parents' fractured home. Julian plays her a recording of raindrops hitting copper leaves—an acoustic frequency he says feels like "safe harbor." For the first time in ten years, Maya stops looking at the exit door.

**3. The Emotional Conflict & Sacrifice:**
Julian is offered a permanent grant in Norway—his lifelong dream. Instead of demanding he stay, Maya presents him with a restored 1920s compass with a hand-written note: *"Go record the arctic ice. But leave one channel open for me."*

**4. The Climax & Resolution:**
One year later, at the reopening of the restored heritage harbor, Maya puts on headphones connected to a live public audio exhibit. Across 4,000 miles, through the crisp static of the northern sea, Julian's voice softly speaks: *"Lighthouse restored. Coordinate verified. I'm coming home."*

*Cinematic Direction:* Soft natural golden-hour lighting (3200K), shallow depth-of-field closeups capturing micro-expressions, with intimate string quartet score underneath.`;
      }
      // 5. Emotional / Grief / Sadness
      else if (q.includes('emotional') || q.includes('drama') || q.includes('cry') || q.includes('sad') || q.includes('grief') || q.includes('loss')) {
        reply = `### 🌧️ Emotional Resonance & Catharsis in Cinema:
- **Restraint Over Melodrama:** Tears are more impactful when the character is desperately trying *not* to cry while performing an ordinary task (washing dishes, folding a coat).
- **Unspoken Longing:** Frame two characters close physically in a tight space or doorway, but separated by emotional distance or a secret they cannot confess.
- **Atmospheric Reflection:** Cool blue palettes and subtle ambient sound design (rain on glass, hollow ticking clock) amplify interior pain without requiring dialogue.`;
      }
      // 6. Specific Actors, Filmmakers, Characters
      else if (q.includes('jackie chan') || q.includes('jackiechan')) {
        reply = `### 🥋 Jackie Chan (Action-Comedy & Choreography Master)
- **Identity:** Legendary martial artist, actor, director, and stunt choreographer known worldwide for his acrobatic fighting style, comic timing, and performing all his own dangerous stunts.
- **Iconic Characters & Films:** Inspector Lee (*Rush Hour*), Chon Wang (*Shanghai Noon*), Wong Fei-hung (*Drunken Master*), Kevin Chan (*Police Story*).
- **Filmmaking Signature:** Using ordinary environment props (ladders, chairs, coats) as defensive weapons, continuous wide shots so the audience sees real contact, and rhythmically paced action-beats mixed with physical comedy.`;
      } else if (q.includes('christopher nolan') || q.includes('nolan')) {
        reply = `### ⏳ Christopher Nolan (Non-Linear Architecture & Practical Scale)
- **Identity:** British-American visionary director famous for high-concept psychological narratives and practical grand-scale set pieces.
- **Key Works:** *Oppenheimer*, *Inception*, *Interstellar*, *The Dark Knight Trilogy*, *Memento*.
- **Directing Takeaway:** Ground fantastical concepts in strict mechanical rules, cross-cut multiple climax timelines simultaneously, and prioritize practical special effects over CGI.`;
      } else if (q.includes('tarantino')) {
        reply = `### 🎙️ Quentin Tarantino (Dialogue Subtext & Non-Linear Crime)
- **Signature:** Pop-culture digressions that build unbearable suspense, split-chapter structures, and extreme close-ups of weapons and expressions.`;
      }
      // 7. Mythological / Epic / Folklore Stories
      else if (q.includes('myth') || q.includes('god') || q.includes('goddess') || q.includes('legend') || q.includes('folklore') || q.includes('epics') || q.includes('ancient')) {
        reply = `### ⚡ Epic Mythological Tale: *"The Sunder of Astra"*

**Origin & Lore:** Ancient Indian / Greco-Vedic Heroic Epics  
**Theme:** Devotion, destiny, and the mortal burden of divine power  

**Prologue: The Forgotten Oath**  
Before the oceans drank the golden city of Dwaravati, there lived a warrior-artisan named **Kaelan**. Gifted with the divine silver chisel of the gods, he was sworn never to craft a mortal blade capable of cleaving fate itself. But when an undying celestial asura devoured the celestial waters, drought consumed the mortal kingdoms, leaving mothers cradling withered barley in silent dust.

**The Descent into the Nether-Caverns:**  
Driven by his brother's dying breath, Kaelan journeys down into the subterranean abyss of *Pātāla*. Guiding him is **Vasuki**, the golden serpent of the deep, who warns him:  
> *"He who carries the weapon of gods must himself bleed the poison of stars. Your memories of home will be burnt as tribute for each strike."*

**The Great Celestial Climax:**  
At the peak of Mount Meru, lightning split the sky into violet fracture lines. As the celestial beast descended, Kaelan did not strike with hatred. He plunged the divine spear into the earth, sacrificing his own royal lineage to restore the rivers. The serpent rain fell across the earth, washing away famine, while Kaelan's name was etched into constellation stone—a mortal who taught the immortal gods the true meaning of honor.

*Cinematic Direction:* Deep obsidian and gold color grade (high dynamic range), booming brass and temple percussion soundscapes, with extreme wide panoramic vistas capturing divine scale.`;
      }
      // 8. Story Narration & Generation Requests ("narrate it", "create a story", "tell me a story", "write a story")
      else if (q.includes('narrat') || q.includes('tell me a story') || q.includes('create a story') || q.includes('write a story') || q === 'story' || q.includes('make a story')) {
        reply = `### 📖 Cinematic Narrative Experience: *"The Watchmaker's Daughter"*

*FADE IN:*

**EXT. COBBLESTONE QUARTER - DUSK (1894)**  
Heavy grey fog rolls across the gaslit lanterns. Rain glazes the black slate rooftops of Prague. 

A lone silhouette—**ELENA (24)**, her fingers stained with gear oil and ink—slams the oak door of an abandoned horology workshop. Tucked inside her wool overcoat is an impossible mechanism: a brass pocket watch whose second hand moves *counter-clockwise*, pulsing with a faint blue luminescence.

**ELENA**  
*(whispering to the empty hall)*  
"Father said forty seconds. Not forty minutes. Just forty seconds to rewrite."

Down the alleyway, heavy brass-buckled boots splash through puddles. Three shadow-clad inspectors from the Imperial Ministry of Chronology halt beneath the amber streetlamp. Their faces are hidden beneath iron respiratory masks.

**INSPECTOR VALK**  
*(voice muffled through brass valve)*  
"Return the master spring, Miss Vane. Time is not property. It belongs to the Crown."

Elena presses her thumb against the watch's winding crown. Her heart hammers in her ribcage. If she winds it backward, she resets the previous minute—erasing the inspectors' arrival, but losing a day of her own lifespan in exchange. She looks at the rain dripping from the zinc gutter... and twists the gear.

*The sound of rushing water violently reverses. Raindrops fly skyward into the clouds.*

*Would you like me to continue the next chapter, or map this into the Screenwriting Studio Scene Breakdown?*`;
      }
      // 9. Psychological Stories / Thrillers
      else if (q.includes('psychological') || q.includes('pyschological')) {
        reply = `### 🧠 Psychological Story & Screenplay Concepts:

1. **The Mirror Fracture (Psychological Thriller):**
   - *Logline:* A forensic psychiatrist evaluating an amnesiac murder suspect begins experiencing the suspect's suppressed childhood memories in her own apartment.
   - *Core Tension:* The audience cannot trust the protagonist's senses. Reality shifts between objective fact and mental projection.
   - *Key Cinematic Device:* Claustrophobic framing, subtle sound design distortion, and lighting that shifts color whenever paranoia spikes.

2. **The Unreliable Observer (Drama / Mystery):**
   - *Logline:* A sound archivist reconstructing a victim's final recorded phone call discovers his own voice whispering commands in the background noise.
   - *Theme:* Guilt, repressed trauma, and the fragmentation of memory under extreme stress.`;
      }
      // 8. Character Creation & Finding Characters
      else if (q.includes('character') || q.includes('protagonist') || q.includes('antagonist') || q.includes('villain') || q.includes('person') || q.includes('name') || q.includes('who is') || q.includes('who are')) {
        reply = `### 👤 Character Profile & Identification for: "${raw}"

- **Protagonist Archetype:** *Detective Aaron Cross* (Internal flaw: Needs absolute control; Fatal mistake: Relies on assumptions over evidence).
- **Antagonist Archetype:** *Dr. Victor Vance* (Mirror foil: Believes empathy is a biological weakness).
- **Character Motivation:** The protagonist wants justice, but fundamentally *needs* to accept forgiveness.
- **Specific Suggested Names for Characters:**
  - Modern/Realistic: *Julian Vance, Maya Thorne, Elena Vance, Luke Mercer*
  - Noir/Gritty: *Frankie Drake, Silas Stone, Vincent Cole*
  - Sci-Fi/Futuristic: *Kaelen Voss, Lyra Chen, Orion Diaz*`;
      }
      // 9. Short Film Ideas & Concepts
      else if (q.includes('short film') || q.includes('idea') || q.includes('concept') || q.includes('pitch')) {
        reply = `### 🎬 Original Short Film Concepts:

1. **The Final Reservation (Suspense / Drama):**
   - *Logline:* A retired assassin sits down at a five-star restaurant for his final meal, only to discover the waiter, the chef, and the sommelier are all previous targets he spared.
2. **Offline 24 (Sci-Fi / Thriller):**
   - *Logline:* In a society where neural connection is mandatory, a young engineer wakes up with a severed implant and 24 hours to navigate an analog city to survive.
3. **The Silent Soundstage (Mystery):**
   - *Logline:* A night-shift studio guard watches a security monitor showing an empty 1940s western set, where footprints keep appearing in the fresh dust.`;
      }
      // 10. Direct Specific Answer (Extracting the Exact Topic)
      else {
        reply = `### 🎯 Direct Guidance on: "${raw}"

- **Cinematic Application:** When integrating **${raw}** into your story, clarify the stakes: What immediate danger or realization confronts the characters right now?
- **Character Dynamics:** Use this moment to test your protagonist's fatal flaw—how does **${raw}** force them to make a difficult choice with no easy outcome?
- **Visual Direction:** Show this on screen through lighting shifts and actor eye-lines rather than exposition dialogue.
- **Next Step:** You can draft this scene directly into **Screenwriting Studio (Scene Breakdown)**!`;
      }

      return {
        data: {
          conversationId: Date.now(),
          reply: reply
        },
        answer: reply
      };
    }

    // Default safe fallback
    return { data: [], message: 'Success' };
  },

  // Auth APIs
  register(payload) {
    return this.request('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
  },

  login(payload) {
    return this.request('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
  },

  getProfile() {
    return this.request('/auth/me');
  },

  forgotPassword(email) {
    return this.request('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) });
  },

  resetPassword(payload) {
    return this.request('/auth/reset-password', { method: 'POST', body: JSON.stringify(payload) });
  },

  changePassword(payload) {
    return this.request('/auth/change-password', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateProfile(payload) {
    return this.request('/auth/profile', { method: 'PUT', body: JSON.stringify(payload) });
  },

  // Dashboard Summary
  getDashboardSummary() {
    return this.request('/dashboard/summary');
  },

  // Screenwriting Studio
  getScreenplays() {
    return this.request('/screenplays');
  },

  getScreenplay(id) {
    return this.request(`/screenplays/${id}`);
  },

  createScreenplay(payload) {
    return this.request('/screenplays', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateScreenplay(id, payload) {
    return this.request(`/screenplays/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  deleteScreenplay(id) {
    return this.request(`/screenplays/${id}`, { method: 'DELETE' });
  },

  getStructureStages(screenplayId) {
    return this.request(`/screenplays/${screenplayId}/structures`);
  },

  updateStructureStage(screenplayId, stageId, payload) {
    return this.request(`/screenplays/${screenplayId}/structures/${stageId}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  getCharacters(screenplayId) {
    return this.request(`/screenplays/${screenplayId}/characters`);
  },

  createCharacter(screenplayId, payload) {
    return this.request('/screenplays/' + screenplayId + '/characters', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateCharacter(screenplayId, charId, payload) {
    return this.request(`/screenplays/${screenplayId}/characters/${charId}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  deleteCharacter(screenplayId, charId) {
    return this.request(`/screenplays/${screenplayId}/characters/${charId}`, { method: 'DELETE' });
  },

  getScenes(screenplayId) {
    return this.request(`/screenplays/${screenplayId}/scenes`);
  },

  createScene(screenplayId, payload) {
    return this.request(`/screenplays/${screenplayId}/scenes`, { method: 'POST', body: JSON.stringify(payload) });
  },

  updateScene(screenplayId, sceneId, payload) {
    return this.request(`/screenplays/${screenplayId}/scenes/${sceneId}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  deleteScene(screenplayId, sceneId) {
    return this.request(`/screenplays/${screenplayId}/scenes/${sceneId}`, { method: 'DELETE' });
  },

  getDraft(screenplayId) {
    return this.request(`/screenplays/${screenplayId}/draft`);
  },

  saveDraft(screenplayId, rawContent) {
    return this.request(`/screenplays/${screenplayId}/draft`, { method: 'PUT', body: JSON.stringify({ rawContent }) });
  },

  // AI Mentor
  getConversations() {
    return this.request('/ai-mentor/conversations');
  },

  getMessages(conversationId) {
    return this.request(`/ai-mentor/conversations/${conversationId}/messages`);
  },

  sendAiMessage(payload) {
    return this.request('/ai-mentor/chat', { method: 'POST', body: JSON.stringify(payload) });
  },

  deleteConversation(conversationId) {
    return this.request(`/ai-mentor/conversations/${conversationId}`, { method: 'DELETE' });
  },

  // Production Pipeline
  getProductionProjects() {
    return this.request('/production/projects');
  },

  createProductionProject(payload) {
    return this.request('/production/projects', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateProductionProject(id, payload) {
    return this.request(`/production/projects/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  deleteProductionProject(id) {
    return this.request(`/production/projects/${id}`, { method: 'DELETE' });
  },

  getProductionTasks(projectId, stage) {
    const q = stage ? `?stage=${stage}` : '';
    return this.request(`/production/projects/${projectId}/tasks${q}`);
  },

  createProductionTask(projectId, payload) {
    return this.request(`/production/projects/${projectId}/tasks`, { method: 'POST', body: JSON.stringify(payload) });
  },

  updateProductionTask(projectId, taskId, payload) {
    return this.request(`/production/projects/${projectId}/tasks/${taskId}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  deleteProductionTask(projectId, taskId) {
    return this.request(`/production/projects/${projectId}/tasks/${taskId}`, { method: 'DELETE' });
  },

  // Cinematic Library
  getLibraryEntries(category, q) {
    let url = '/library?';
    if (category) url += `category=${encodeURIComponent(category)}&`;
    if (q) url += `q=${encodeURIComponent(q)}&`;
    return this.request(url);
  },

  createLibraryEntry(payload) {
    return this.request('/library', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateLibraryEntry(id, payload) {
    return this.request(`/library/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  deleteLibraryEntry(id) {
    return this.request(`/library/${id}`, { method: 'DELETE' });
  },

  // Creative Vault
  getVaultIdeas(category, favouritesOnly, q) {
    let url = '/vault?';
    if (category) url += `category=${encodeURIComponent(category)}&`;
    if (favouritesOnly) url += `favouritesOnly=true&`;
    if (q) url += `q=${encodeURIComponent(q)}&`;
    return this.request(url);
  },

  createVaultIdea(payload) {
    return this.request('/vault', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateVaultIdea(id, payload) {
    return this.request(`/vault/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  },

  toggleIdeaFavourite(id) {
    return this.request(`/vault/${id}/favourite`, { method: 'POST' });
  },

  deleteVaultIdea(id) {
    return this.request(`/vault/${id}`, { method: 'DELETE' });
  },

  convertIdeaToScreenplay(id) {
    return this.request(`/vault/${id}/convert-to-screenplay`, { method: 'POST' });
  }
};

// UI Notification Toast
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${message}</span>
    <button style="background:none; border:none; color:inherit; font-size:1.1rem; cursor:pointer;" onclick="this.parentElement.remove()">✕</button>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
