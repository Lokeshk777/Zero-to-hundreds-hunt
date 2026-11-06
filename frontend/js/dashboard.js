/* ====================================================================
   ZERO TO HUNDRED'S HUNT - DASHBOARD & WORKSPACE LOGIC
   Seamless SPA navigation, live database CRUD for all 6 modules
   ==================================================================== */

const Workspace = {
  currentUser: null,
  activeModule: 'dashboard',

  async init() {
    Auth.requireAuth();
    this.currentUser = Auth.getUser();

    // Setup UI
    this.setupUserInfo();
    this.setupNavigation();
    this.setupModals();

    // Load initial dashboard statistics
    await this.loadDashboardStats();

    // Preload Screenplay list for selector dropdowns
    await ScreenwritingStudio.loadScreenplays();

    // Preload Production projects
    await ProductionPipeline.loadProjects();
  },

  setupUserInfo() {
    if (!this.currentUser) return;
    const nameEl = document.getElementById('sidebar-user-name');
    const avatarEl = document.getElementById('sidebar-user-avatar');
    const welcomeEl = document.getElementById('dash-welcome-name');

    if (nameEl) nameEl.textContent = this.currentUser.fullName || this.currentUser.username;
    if (welcomeEl) welcomeEl.textContent = this.currentUser.fullName || this.currentUser.username;
    if (avatarEl) {
      avatarEl.textContent = (this.currentUser.fullName || this.currentUser.username).charAt(0).toUpperCase();
    }

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        Auth.clearSession();
        showToast('Logged out securely.', 'info');
        setTimeout(() => window.location.href = 'login.html', 500);
      });
    }

    const profBtn = document.getElementById('open-profile-btn');
    if (profBtn) {
      profBtn.addEventListener('click', () => this.openProfileModal());
    }
  },

  setupNavigation() {
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
      item.addEventListener('click', () => {
        const mod = item.getAttribute('data-module');
        this.switchModule(mod);
      });
    });
  },

  switchModule(moduleName) {
    this.activeModule = moduleName;

    // Update sidebar nav items
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(el => {
      if (el.getAttribute('data-module') === moduleName) {
        el.classList.add('active');
      } else {
        el.classList.remove('active');
      }
    });

    // Update main containers
    document.querySelectorAll('.module-container').forEach(el => {
      el.classList.remove('active');
    });

    const target = document.getElementById(`mod-${moduleName}`);
    if (target) {
      target.classList.add('active');
    }

    // Update Topbar Title
    const titleMap = {
      'dashboard': 'Dashboard & Creative Overview',
      'screenwriting': 'Screenwriting Studio',
      'ai-mentor': 'Filmmaking AI Mentor',
      'production': 'Production Pipeline Management',
      'library': 'Cinematic Knowledge & Film Analysis',
      'vault': 'Creative Vault & Idea Incubator'
    };
    const topTitle = document.getElementById('topbar-title');
    if (topTitle) topTitle.textContent = titleMap[moduleName] || 'Workspace';

    // Refresh data on switch
    if (moduleName === 'dashboard') this.loadDashboardStats();
    if (moduleName === 'screenwriting') ScreenwritingStudio.refreshActiveScreenplay();
    if (moduleName === 'production') ProductionPipeline.refreshActiveProject();
    if (moduleName === 'library') CinematicLibrary.loadEntries();
    if (moduleName === 'vault') CreativeVault.loadIdeas();
  },

  async loadDashboardStats() {
    try {
      const res = await Api.getDashboardSummary();
      const d = res.data;

      // Counters
      const elSp = document.getElementById('dash-total-screenplays');
      const elIdeas = document.getElementById('dash-total-ideas');
      const elProd = document.getElementById('dash-active-productions');
      const elLib = document.getElementById('dash-total-library');

      if (elSp) elSp.textContent = d.totalScreenplays;
      if (elIdeas) elIdeas.textContent = d.totalCreativeIdeas;
      if (elProd) elProd.textContent = d.activeProductionProjects;
      if (elLib) elLib.textContent = d.totalLibraryEntries;

      // Recent screenplays list
      const spList = document.getElementById('dash-recent-screenplays-list');
      if (spList) {
        if (!d.recentScreenplays || d.recentScreenplays.length === 0) {
          spList.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem;">No screenplays created yet. Start your first draft!</p>`;
        } else {
          spList.innerHTML = d.recentScreenplays.map(sp => `
            <div class="task-item" style="cursor: pointer;" onclick="ScreenwritingStudio.selectAndOpen(${sp.id})">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <strong style="color: var(--text-primary);">${this.escapeHtml(sp.title)}</strong>
                <span class="badge badge-gold">${sp.status}</span>
              </div>
              <p style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 4px;">${this.escapeHtml(sp.genre || 'Unspecified genre')}</p>
            </div>
          `).join('');
        }
      }

      // Recent ideas list
      const idList = document.getElementById('dash-recent-ideas-list');
      if (idList) {
        if (!d.recentIdeas || d.recentIdeas.length === 0) {
          idList.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem;">No ideas captured in the vault yet.</p>`;
        } else {
          idList.innerHTML = d.recentIdeas.map(idea => `
            <div class="task-item" style="cursor: pointer;" onclick="Workspace.switchModule('vault')">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <strong style="color: var(--text-primary);">${idea.favourite ? '★ ' : ''}${this.escapeHtml(idea.title)}</strong>
                <span class="badge badge-blue">${idea.category}</span>
              </div>
            </div>
          `).join('');
        }
      }

    } catch (err) {
      console.warn("Could not load dashboard stats from backend:", err);
    }
  },

  // Modals management
  setupModals() {
    // Close modal on background click
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
      overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
          overlay.classList.remove('active');
        }
      });
    });
  },

  openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
  },

  closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
  },

  openNewScreenplayModal() {
    document.getElementById('new-screenplay-form').reset();
    this.openModal('modal-new-screenplay');
  },

  openQuickIdeaModal() {
    CreativeVault.openNewIdeaModal();
  },

  openProfileModal() {
    const user = Auth.getUser();
    if (!user) return;
    document.getElementById('prof-full-name').value = user.fullName || '';
    document.getElementById('prof-email').value = user.email || '';
    document.getElementById('prof-username').value = user.username || '';
    document.getElementById('prof-bio').value = user.bio || '';
    this.openModal('modal-profile');
  },

  async updateUserProfile() {
    const fullName = document.getElementById('prof-full-name').value.trim();
    const bio = document.getElementById('prof-bio').value.trim();

    try {
      const res = await Api.updateProfile({ fullName, bio });
      Auth.setSession(res.data, true);
      this.currentUser = res.data;
      this.setupUserInfo();
      this.closeModal('modal-profile');
      showToast('Profile updated successfully!', 'success');
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async changePassword() {
    const currentPassword = document.getElementById('chg-curr-pwd').value;
    const newPassword = document.getElementById('chg-new-pwd').value;
    const confirmPassword = document.getElementById('chg-conf-pwd').value;

    if (newPassword !== confirmPassword) {
      showToast('New passwords do not match.', 'error');
      return;
    }

    try {
      await Api.changePassword({ currentPassword, newPassword, confirmPassword });
      document.getElementById('change-pwd-form').reset();
      showToast('Password updated securely.', 'success');
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function(m) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m];
    });
  }
};

// ====================================================================
// MODULE 2: SCREENWRITING STUDIO LOGIC
// ====================================================================
const ScreenwritingStudio = {
  screenplays: [],
  currentScreenplayId: null,
  currentScreenplay: null,
  activeSubtab: 'sub-story',

  async loadScreenplays() {
    try {
      const res = await Api.getScreenplays();
      this.screenplays = res.data || [];
      this.populateSelector();

      // If we don't have an active screenplay selected, default to the first one
      if (this.screenplays.length > 0 && !this.currentScreenplayId) {
        this.selectScreenplay(this.screenplays[0].id);
      }
    } catch (err) {
      console.warn('Screenplays load error:', err);
    }
  },

  populateSelector() {
    const sel = document.getElementById('screenplay-selector');
    if (!sel) return;
    sel.innerHTML = '<option value="">-- Select a Screenplay --</option>' +
      this.screenplays.map(sp => `<option value="${sp.id}">${Workspace.escapeHtml(sp.title)}</option>`).join('');

    if (this.currentScreenplayId) {
      sel.value = this.currentScreenplayId;
    }
  },

  async onScreenplaySelected(id) {
    if (!id) return;
    await this.selectScreenplay(Number(id));
  },

  async selectAndOpen(id) {
    Workspace.switchModule('screenwriting');
    await this.selectScreenplay(id);
  },

  async selectScreenplay(id) {
    this.currentScreenplayId = id;
    const sel = document.getElementById('screenplay-selector');
    if (sel) sel.value = id;

    const delBtn = document.getElementById('btn-delete-screenplay');
    if (delBtn) delBtn.style.display = 'inline-flex';

    await this.refreshActiveScreenplay();
  },

  async refreshActiveScreenplay() {
    if (!this.currentScreenplayId) return;

    try {
      const res = await Api.getScreenplay(this.currentScreenplayId);
      this.currentScreenplay = res.data;

      // Populate Story Details Form
      document.getElementById('sp-title').value = this.currentScreenplay.title || '';
      document.getElementById('sp-genre').value = this.currentScreenplay.genre || '';
      document.getElementById('sp-theme').value = this.currentScreenplay.theme || '';
      document.getElementById('sp-structure-type').value = this.currentScreenplay.structureType || 'THREE_ACT';
      document.getElementById('sp-logline').value = this.currentScreenplay.logline || '';
      document.getElementById('sp-concept').value = this.currentScreenplay.storyConcept || '';
      document.getElementById('sp-conflict').value = this.currentScreenplay.centralConflict || '';
      
      const synEditor = document.getElementById('sp-synopsis-editor');
      const synVal = this.currentScreenplay.synopsis || '';
      if (synEditor) {
        synEditor.innerHTML = synVal ? synVal : '<p>Start writing your Full Screenplay here. Use the toolbar above for highlighted headings and colored pens...</p>';
      }
      document.getElementById('sp-synopsis').value = synVal;

      // Auto-analyze if synopsis is already present
      if (synVal) {
        this.analyzeAndDisplayBreakdown(synVal, this.currentScreenplay.storyConcept || '');
      }

      // Update Structure Badge
      const badge = document.getElementById('current-structure-badge');
      if (badge) badge.textContent = `Structure: ${this.currentScreenplay.structureType}`;

      // Refresh subtab content
      if (this.activeSubtab === 'sub-structures') await this.loadStructures();
      if (this.activeSubtab === 'sub-characters') await this.loadCharacters();
      if (this.activeSubtab === 'sub-scenes') await this.loadScenes();
      if (this.activeSubtab === 'sub-editor') await this.loadDraft();

    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  switchSubtab(subtabId) {
    this.activeSubtab = subtabId;
    document.querySelectorAll('.subtabs-bar .subtab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-subtab') === subtabId);
    });

    document.querySelectorAll('.subtab-content').forEach(el => {
      el.style.display = el.id === subtabId ? 'block' : 'none';
    });

    this.refreshActiveScreenplay();
  },

  formatDoc(cmd, val = null) {
    document.execCommand(cmd, false, val);
    const editor = document.getElementById('sp-synopsis-editor');
    if (editor) editor.focus();
  },

  async submitNewScreenplay() {
    const title = document.getElementById('new-sp-title').value.trim();
    const genre = document.getElementById('new-sp-genre').value.trim();
    const structureType = document.getElementById('new-sp-structure').value;
    const logline = document.getElementById('new-sp-logline').value.trim();

    try {
      const res = await Api.createScreenplay({ title, genre, structureType, logline });
      Workspace.closeModal('modal-new-screenplay');
      showToast('Screenplay created successfully!', 'success');
      await this.loadScreenplays();
      await this.selectAndOpen(res.data.id);
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async saveStoryDetails() {
    if (!this.currentScreenplayId) {
      showToast('Please select or create a screenplay project first.', 'error');
      return;
    }

    const title = document.getElementById('sp-title').value.trim();
    const genre = document.getElementById('sp-genre').value.trim();
    const theme = document.getElementById('sp-theme').value.trim();
    const structureType = document.getElementById('sp-structure-type').value;
    const logline = document.getElementById('sp-logline').value.trim();
    const storyConcept = document.getElementById('sp-concept').value.trim();
    const centralConflict = document.getElementById('sp-conflict').value.trim();
    
    // Read rich formatted screenplay canvas
    const synEditor = document.getElementById('sp-synopsis-editor');
    const synopsis = synEditor ? synEditor.innerHTML.trim() : document.getElementById('sp-synopsis').value.trim();
    document.getElementById('sp-synopsis').value = synopsis;

    // Validation: Require key details before saving!
    if (!title) {
      showToast('Please enter the Screenplay Title before saving.', 'error');
      document.getElementById('sp-title').focus();
      return;
    }

    if (!logline) {
      showToast('Please provide a Logline for your screenplay.', 'error');
      document.getElementById('sp-logline').focus();
      return;
    }

    if (!storyConcept && !synopsis) {
      showToast('Please fill in either the Story Concept or Full Synopsis to save.', 'error');
      document.getElementById('sp-concept').focus();
      return;
    }

    const payload = {
      title,
      genre,
      theme,
      structureType,
      logline,
      storyConcept,
      centralConflict,
      synopsis
    };

    try {
      await Api.updateScreenplay(this.currentScreenplayId, payload);
      showToast('Screenplay details saved successfully!', 'success');
      
      // Analyze and display extracted characters and physical places
      this.analyzeAndDisplayBreakdown(synopsis, storyConcept);

      await this.loadScreenplays();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  analyzeAndDisplayBreakdown(rawSynopsis, rawConcept) {
    const combinedText = (rawSynopsis + ' ' + rawConcept).replace(/<[^>]*>/g, ' '); // Strip HTML tags
    const panel = document.getElementById('screenplay-analysis-panel');
    const charListEl = document.getElementById('analysis-characters-list');
    const locListEl = document.getElementById('analysis-locations-list');
    const charCountEl = document.getElementById('analysis-char-count');
    const locCountEl = document.getElementById('analysis-loc-count');
    const timeBadge = document.getElementById('analysis-time-badge');

    if (!panel) return;

    // 1. Detect Physical Places / Locations
    const locationKeywords = [
      'hospital', 'clinic', 'police station', 'precinct', 'apartment', 'house', 'mansion', 
      'office', 'warehouse', 'rooftop', 'street', 'alley', 'diner', 'restaurant', 'bar', 
      'subway', 'station', 'airport', 'courtroom', 'jail', 'prison', 'cabin', 'forest', 
      'beach', 'hotel', 'motel', 'corridor', 'basement', 'laboratory', 'school', 'church'
    ];

    const detectedLocations = new Set();
    const lowerText = combinedText.toLowerCase();

    // Check sluglines like INT. / EXT.
    const sluglineMatches = combinedText.match(/(?:INT\.|EXT\.)\s+([A-Z0-9\s\-]+?)(?:\s+-\s+DAY|\s+-\s+NIGHT|\n|$)/gi);
    if (sluglineMatches) {
      sluglineMatches.forEach(match => {
        const cleaned = match.replace(/(?:INT\.|EXT\.)/i, '').replace(/-\s*(?:DAY|NIGHT|DUSK|DAWN)/i, '').trim();
        if (cleaned.length > 2) detectedLocations.add(cleaned.toUpperCase());
      });
    }

    // Check keyword places
    locationKeywords.forEach(loc => {
      const regex = new RegExp(`\\b${loc}s?\\b`, 'i');
      if (regex.test(lowerText)) {
        detectedLocations.add(loc.charAt(0).toUpperCase() + loc.slice(1));
      }
    });

    // 2. Detect Characters & Persons (e.g. Man, Woman, Doctor, Names)
    const detectedCharacters = [];
    const personRoles = [
      { name: 'Protagonist / Detective', type: 'Person (Lead)', regex: /\b(detective|officer|investigator|agent)\b/i },
      { name: 'Doctor / Surgeon', type: 'Person (Medical)', regex: /\b(doctor|physician|surgeon|nurse|psychiatrist)\b/i },
      { name: 'Mystery Woman / Female Lead', type: 'Woman', regex: /\b(woman|female|lady|girl|mother|daughter|wife|sister)\b/i },
      { name: 'Man / Male Lead', type: 'Man', regex: /\b(man|male|gentleman|boy|father|son|husband|brother)\b/i },
      { name: 'Suspect / Assassin', type: 'Person (Antagonist)', regex: /\b(suspect|killer|assassin|thief|criminal|villain)\b/i }
    ];

    personRoles.forEach(role => {
      if (role.regex.test(lowerText)) {
        detectedCharacters.push(role);
      }
    });

    // Extract Capitalized character names (e.g. MARCUS, ELENA, JOHN)
    const nameMatches = combinedText.match(/\b([A-Z]{3,15})\b/g);
    const stopWords = new Set(['ACT', 'THE', 'AND', 'FOR', 'OUT', 'DAY', 'FADE', 'INT', 'EXT', 'WITH', 'FROM', 'THAT', 'THIS', 'SCENE']);
    const customNames = new Set();

    if (nameMatches) {
      nameMatches.forEach(name => {
        if (!stopWords.has(name) && !detectedLocations.has(name)) {
          customNames.add(name);
        }
      });
    }

    customNames.forEach(name => {
      detectedCharacters.unshift({
        name: name,
        type: 'Identified Character'
      });
    });

    // Render Characters
    if (detectedCharacters.length > 0) {
      charListEl.innerHTML = detectedCharacters.map(c => `
        <div style="display: flex; justify-content: space-between; align-items: center; background: #1C1E26; padding: 8px 12px; border-radius: 6px;">
          <strong style="color: #F3F4F6; font-size: 0.9rem;">${Workspace.escapeHtml(c.name)}</strong>
          <span class="badge badge-blue" style="font-size: 0.7rem;">${c.type}</span>
        </div>
      `).join('');
      charCountEl.textContent = detectedCharacters.length;
    } else {
      charListEl.innerHTML = '<p style="color: var(--text-muted); font-size: 0.85rem;">Describe characters (e.g. Detective, A young woman, Doctor) to populate here.</p>';
      charCountEl.textContent = '0';
    }

    // Render Locations
    const locArray = Array.from(detectedLocations);
    if (locArray.length > 0) {
      locListEl.innerHTML = locArray.map(loc => `
        <div style="display: flex; justify-content: space-between; align-items: center; background: #1C1E26; padding: 8px 12px; border-radius: 6px;">
          <strong style="color: var(--accent); font-size: 0.9rem;">📍 ${Workspace.escapeHtml(loc)}</strong>
          <span class="badge badge-gold" style="font-size: 0.7rem;">Physical Location</span>
        </div>
      `).join('');
      locCountEl.textContent = locArray.length;
    } else {
      locListEl.innerHTML = '<p style="color: var(--text-muted); font-size: 0.85rem;">Mention physical places (e.g. Hospital, Apartment, Rooftop) to populate here.</p>';
      locCountEl.textContent = '0';
    }

    if (timeBadge) timeBadge.textContent = 'Analyzed at ' + new Date().toLocaleTimeString();
    panel.style.display = 'block';

    // AUTO-CREATE AND SAVE SCENES & CHARACTERS AUTOMATICALLY (No manual entry required!)
    this.autoSyncBreakdownToProject(detectedLocations, detectedCharacters, combinedText);
  },

  async autoSyncBreakdownToProject(locationsSet, charactersList, fullText) {
    if (!this.currentScreenplayId) return;

    try {
      // 1. Auto-generate Scene Breakdown from detected locations and paragraphs
      const locArray = Array.from(locationsSet);
      if (locArray.length > 0) {
        // Fetch existing scenes so we don't duplicate
        const existingScenesRes = await Api.getScenes(this.currentScreenplayId);
        const existingScenes = existingScenesRes.data || [];
        const existingLocs = new Set(existingScenes.map(s => (s.location || '').toUpperCase()));

        // Extract action sentences from full text
        const sentences = fullText.split(/(?<=[.?!])\s+/).filter(s => s.trim().length > 15);
        let sceneNum = existingScenes.length + 1;

        for (const loc of locArray) {
          const upperLoc = loc.toUpperCase();
          if (!existingLocs.has(upperLoc)) {
            // Find sentences where this location is mentioned
            const matchingAction = sentences.find(s => s.toUpperCase().includes(upperLoc)) || `Action unfolds in ${loc}.`;
            const characterNames = charactersList.map(c => c.name).filter(n => n.length > 2).slice(0, 3).join(', ') || 'Characters';

            const newScenePayload = {
              sceneNumber: sceneNum++,
              headingType: upperLoc.includes('STREET') || upperLoc.includes('BEACH') || upperLoc.includes('ROOFTOP') ? 'EXT.' : 'INT.',
              location: upperLoc,
              timeOfDay: 'DAY',
              characterAppearances: characterNames,
              sceneObjective: `Develop key story narrative at ${loc}.`,
              emotionalPurpose: 'Build tension and advance character objectives.',
              actions: matchingAction
            };

            await Api.createScene(this.currentScreenplayId, newScenePayload);
          }
        }
      }

      // 2. Auto-generate Character Profiles for newly identified characters
      if (charactersList.length > 0) {
        const existingCharsRes = await Api.getCharacters(this.currentScreenplayId);
        const existingChars = existingCharsRes.data || [];
        const existingNames = new Set(existingChars.map(c => (c.name || '').toUpperCase()));

        for (const charItem of charactersList) {
          const upperName = charItem.name.toUpperCase();
          if (!existingNames.has(upperName) && charItem.name.length >= 3) {
            const newCharPayload = {
              name: charItem.name,
              roleType: charItem.type.includes('Lead') ? 'MAIN' : (charItem.type.includes('Antagonist') ? 'ANTAGONIST' : 'SUPPORTING'),
              age: 'Adult',
              background: `Identified from screenplay narrative as ${charItem.type}.`,
              personality: 'Determined, reactive to narrative stakes.',
              motivation: 'Pursues key story objective.',
              goals: 'Overcome immediate obstacle in screenplay.'
            };

            await Api.createCharacter(this.currentScreenplayId, newCharPayload);
          }
        }
      }

      // Reload subtabs if active
      if (this.activeSubtab === 'sub-scenes') await this.loadScenes();
      if (this.activeSubtab === 'sub-characters') await this.loadCharacters();

    } catch (err) {
      console.warn('Auto-sync breakdown notice:', err);
    }
  },

  async deleteCurrentScreenplay() {
    if (!this.currentScreenplayId) return;
    if (!confirm('Are you sure you want to delete this entire screenplay and all its characters, scenes, and drafts?')) return;

    try {
      await Api.deleteScreenplay(this.currentScreenplayId);
      showToast('Screenplay deleted successfully.', 'info');
      this.currentScreenplayId = null;
      document.getElementById('story-details-form').reset();
      const delBtn = document.getElementById('btn-delete-screenplay');
      if (delBtn) delBtn.style.display = 'none';
      await this.loadScreenplays();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  // Structures
  async loadStructures() {
    if (!this.currentScreenplayId) return;
    try {
      const res = await Api.getStructureStages(this.currentScreenplayId);
      const container = document.getElementById('structures-beats-container');
      if (!container) return;

      container.innerHTML = res.data.map(stage => `
        <div class="card-panel" style="padding: 18px; margin-bottom: 0; background: #151619;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <strong style="color: var(--accent); font-size: 1rem;">${stage.stageOrder}. ${Workspace.escapeHtml(stage.stageName)}</strong>
            <button class="btn btn-secondary btn-sm" onclick="ScreenwritingStudio.saveStructureBeat(${stage.id})">Save Beat Notes</button>
          </div>
          <p style="color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 10px;">${Workspace.escapeHtml(stage.description || '')}</p>
          <textarea id="beat-notes-${stage.id}" rows="2" placeholder="Write your scene ideas and story beats for this milestone...">${Workspace.escapeHtml(stage.notes || '')}</textarea>
        </div>
      `).join('');
    } catch (err) {
      console.warn(err);
    }
  },

  async saveStructureBeat(stageId) {
    const notes = document.getElementById(`beat-notes-${stageId}`).value;
    try {
      await Api.updateStructureStage(this.currentScreenplayId, stageId, { notes });
      showToast('Structure beat updated!', 'success');
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  // Characters
  async loadCharacters() {
    if (!this.currentScreenplayId) return;
    try {
      const res = await Api.getCharacters(this.currentScreenplayId);
      const grid = document.getElementById('characters-list-grid');
      if (!grid) return;

      if (!res.data || res.data.length === 0) {
        grid.innerHTML = '<p style="color: var(--text-muted);">No characters created yet. Click "+ Add Character" to begin.</p>';
        return;
      }

      grid.innerHTML = res.data.map(c => `
        <div class="card-panel" style="margin-bottom: 0; background: #151619;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <h4 style="font-size: 1.1rem; color: var(--text-primary);">${Workspace.escapeHtml(c.name)}</h4>
            <span class="badge badge-gold">${c.roleType}</span>
          </div>
          <p style="color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 6px;"><strong>Age:</strong> ${Workspace.escapeHtml(c.age || 'N/A')}</p>
          <p style="color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 6px;"><strong>Motivation:</strong> ${Workspace.escapeHtml(c.motivation || 'N/A')}</p>
          <p style="color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 6px;"><strong>Arc:</strong> ${Workspace.escapeHtml(c.characterArc || 'N/A')}</p>
          <div style="display: flex; gap: 8px; margin-top: 14px; justify-content: flex-end;">
            <button class="btn btn-secondary btn-sm" onclick="ScreenwritingStudio.editCharacter(${JSON.stringify(c).replace(/"/g, '&quot;')})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="ScreenwritingStudio.deleteCharacter(${c.id})">Delete</button>
          </div>
        </div>
      `).join('');
    } catch (err) {
      console.warn(err);
    }
  },

  openNewCharacterModal() {
    if (!this.currentScreenplayId) {
      showToast('Please select a screenplay first.', 'error');
      return;
    }
    document.getElementById('character-form').reset();
    document.getElementById('char-edit-id').value = '';
    document.getElementById('char-modal-title').textContent = 'Add Character Profile';
    Workspace.openModal('modal-character');
  },

  editCharacter(c) {
    document.getElementById('char-edit-id').value = c.id;
    document.getElementById('char-name').value = c.name;
    document.getElementById('char-role').value = c.roleType;
    document.getElementById('char-age').value = c.age || '';
    document.getElementById('char-background').value = c.background || '';
    document.getElementById('char-personality').value = c.personality || '';
    document.getElementById('char-motivation').value = c.motivation || '';
    document.getElementById('char-goals').value = c.goals || '';
    document.getElementById('char-strengths').value = c.strengths || '';
    document.getElementById('char-weaknesses').value = c.weaknesses || '';
    document.getElementById('char-internal').value = c.internalConflict || '';
    document.getElementById('char-external').value = c.externalConflict || '';
    document.getElementById('char-arc').value = c.characterArc || '';
    document.getElementById('char-modal-title').textContent = 'Edit Character Profile';
    Workspace.openModal('modal-character');
  },

  async submitCharacter() {
    const editId = document.getElementById('char-edit-id').value;
    const payload = {
      name: document.getElementById('char-name').value.trim(),
      roleType: document.getElementById('char-role').value,
      age: document.getElementById('char-age').value.trim(),
      background: document.getElementById('char-background').value.trim(),
      personality: document.getElementById('char-personality').value.trim(),
      motivation: document.getElementById('char-motivation').value.trim(),
      goals: document.getElementById('char-goals').value.trim(),
      strengths: document.getElementById('char-strengths').value.trim(),
      weaknesses: document.getElementById('char-weaknesses').value.trim(),
      internalConflict: document.getElementById('char-internal').value.trim(),
      externalConflict: document.getElementById('char-external').value.trim(),
      characterArc: document.getElementById('char-arc').value.trim()
    };

    try {
      if (editId) {
        await Api.updateCharacter(this.currentScreenplayId, editId, payload);
        showToast('Character profile updated!', 'success');
      } else {
        await Api.createCharacter(this.currentScreenplayId, payload);
        showToast('Character profile added!', 'success');
      }
      Workspace.closeModal('modal-character');
      this.loadCharacters();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteCharacter(charId) {
    if (!confirm('Delete this character profile?')) return;
    try {
      await Api.deleteCharacter(this.currentScreenplayId, charId);
      showToast('Character deleted.', 'info');
      this.loadCharacters();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  // Scenes
  async loadScenes() {
    if (!this.currentScreenplayId) return;
    try {
      const res = await Api.getScenes(this.currentScreenplayId);
      const container = document.getElementById('scenes-list-container');
      if (!container) return;

      if (!res.data || res.data.length === 0) {
        container.innerHTML = '<p style="color: var(--text-muted);">No scenes created yet. Break down your first scene!</p>';
        return;
      }

      container.innerHTML = res.data.map(s => `
        <div class="card-panel" style="margin-bottom: 0; background: #151619;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <strong style="color: var(--accent); font-size: 1.05rem;">SCENE ${s.sceneNumber}: ${Workspace.escapeHtml(s.sceneHeading)}</strong>
            <div style="display: flex; gap: 8px;">
              <button class="btn btn-secondary btn-sm" onclick="ScreenwritingStudio.editScene(${JSON.stringify(s).replace(/"/g, '&quot;')})">Edit</button>
              <button class="btn btn-danger btn-sm" onclick="ScreenwritingStudio.deleteScene(${s.id})">Delete</button>
            </div>
          </div>
          <p style="color: var(--text-secondary); font-size: 0.88rem; margin-bottom: 6px;"><strong>Characters:</strong> ${Workspace.escapeHtml(s.characterAppearances || 'None listed')}</p>
          <p style="color: var(--text-secondary); font-size: 0.88rem; margin-bottom: 6px;"><strong>Objective:</strong> ${Workspace.escapeHtml(s.sceneObjective || 'None')}</p>
          <p style="color: var(--text-primary); font-size: 0.9rem; margin-top: 8px; font-family: var(--font-mono);">${Workspace.escapeHtml(s.actions || '')}</p>
        </div>
      `).join('');
    } catch (err) {
      console.warn(err);
    }
  },

  openNewSceneModal() {
    if (!this.currentScreenplayId) {
      showToast('Please select a screenplay first.', 'error');
      return;
    }
    document.getElementById('scene-form').reset();
    document.getElementById('scene-edit-id').value = '';
    document.getElementById('scene-modal-title').textContent = 'Add Scene';
    Workspace.openModal('modal-scene');
  },

  editScene(s) {
    document.getElementById('scene-edit-id').value = s.id;
    document.getElementById('scene-number').value = s.sceneNumber;
    document.getElementById('scene-heading-type').value = s.headingType;
    document.getElementById('scene-location').value = s.location;
    document.getElementById('scene-time').value = s.timeOfDay;
    document.getElementById('scene-characters').value = s.characterAppearances || '';
    document.getElementById('scene-objective').value = s.sceneObjective || '';
    document.getElementById('scene-emotional').value = s.emotionalPurpose || '';
    document.getElementById('scene-actions').value = s.actions || '';
    document.getElementById('scene-modal-title').textContent = 'Edit Scene';
    Workspace.openModal('modal-scene');
  },

  async submitScene() {
    const editId = document.getElementById('scene-edit-id').value;
    const payload = {
      sceneNumber: parseInt(document.getElementById('scene-number').value) || 1,
      headingType: document.getElementById('scene-heading-type').value,
      location: document.getElementById('scene-location').value.trim(),
      timeOfDay: document.getElementById('scene-time').value,
      characterAppearances: document.getElementById('scene-characters').value.trim(),
      sceneObjective: document.getElementById('scene-objective').value.trim(),
      emotionalPurpose: document.getElementById('scene-emotional').value.trim(),
      actions: document.getElementById('scene-actions').value.trim()
    };

    try {
      if (editId) {
        await Api.updateScene(this.currentScreenplayId, editId, payload);
        showToast('Scene updated!', 'success');
      } else {
        await Api.createScene(this.currentScreenplayId, payload);
        showToast('Scene added!', 'success');
      }
      Workspace.closeModal('modal-scene');
      this.loadScenes();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteScene(sceneId) {
    if (!confirm('Delete this scene?')) return;
    try {
      await Api.deleteScene(this.currentScreenplayId, sceneId);
      showToast('Scene deleted.', 'info');
      this.loadScenes();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  // Editor Draft
  async loadDraft() {
    if (!this.currentScreenplayId) return;
    try {
      const res = await Api.getDraft(this.currentScreenplayId);
      const editor = document.getElementById('screenplay-editor-textarea');
      const timeLbl = document.getElementById('draft-last-saved');
      if (editor) editor.value = res.data.rawContent || '';
      if (timeLbl && res.data.lastSaved) {
        timeLbl.textContent = `Last saved: ${new Date(res.data.lastSaved).toLocaleTimeString()}`;
      }
    } catch (err) {
      console.warn(err);
    }
  },

  async saveCurrentDraft() {
    if (!this.currentScreenplayId) {
      showToast('Please select a screenplay first.', 'error');
      return;
    }
    const editor = document.getElementById('screenplay-editor-textarea');
    const content = editor.value.trim();

    if (!content) {
      showToast('Screenplay editor is empty. Write your script scenes before saving.', 'error');
      editor.focus();
      return;
    }

    try {
      const res = await Api.saveDraft(this.currentScreenplayId, content);
      const timeLbl = document.getElementById('draft-last-saved');
      if (timeLbl) timeLbl.textContent = `Last saved: ${new Date().toLocaleTimeString()}`;
      showToast('Screenplay draft saved successfully!', 'success');
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  insertSlugline(type) {
    const editor = document.getElementById('screenplay-editor-textarea');
    if (!editor) return;
    const textToInsert = `\n\n${type} LOCATION - DAY\n\n`;
    const start = editor.selectionStart;
    const end = editor.selectionEnd;
    editor.value = editor.value.substring(0, start) + textToInsert + editor.value.substring(end);
    editor.focus();
    editor.selectionStart = editor.selectionEnd = start + textToInsert.length;
  }
};

// MODULE 3: AI MENTOR LOGIC (Handled in js/ai-mentor.js)

// ====================================================================
// MODULE 4: PRODUCTION PIPELINE LOGIC
// ====================================================================
const ProductionPipeline = {
  projects: [],
  currentProjectId: null,

  async loadProjects() {
    try {
      const res = await Api.getProductionProjects();
      this.projects = res.data || [];
      this.populateSelector();

      if (this.projects.length > 0 && !this.currentProjectId) {
        this.selectProject(this.projects[0].id);
      }
    } catch (err) {
      console.warn(err);
    }
  },

  populateSelector() {
    const sel = document.getElementById('prod-project-selector');
    if (!sel) return;
    sel.innerHTML = '<option value="">-- Select Production Project --</option>' +
      this.projects.map(p => `<option value="${p.id}">${Workspace.escapeHtml(p.title)} (${p.stage})</option>`).join('');

    if (this.currentProjectId) {
      sel.value = this.currentProjectId;
    }
  },

  onProjectSelected(id) {
    if (!id) return;
    this.selectProject(Number(id));
  },

  async selectProject(id) {
    this.currentProjectId = id;
    const delBtn = document.getElementById('btn-delete-prod-project');
    if (delBtn) delBtn.style.display = 'inline-flex';
    await this.refreshActiveProject();
  },

  async refreshActiveProject() {
    if (!this.currentProjectId) return;
    const p = this.projects.find(item => item.id === this.currentProjectId);
    if (!p) return;

    // Overview Card
    const card = document.getElementById('prod-overview-card');
    if (card) {
      card.style.display = 'block';
      document.getElementById('prod-detail-title').textContent = p.title;
      document.getElementById('prod-detail-meta').textContent = `Director: ${p.director || 'Unassigned'} | Producer: ${p.producer || 'Unassigned'} | Target: ${p.targetCompletionDate || 'Open'}`;
      document.getElementById('prod-detail-stage').textContent = p.stage;
      document.getElementById('prod-detail-pct').textContent = `${p.progressPercentage || 0}% Complete`;
    }

    await this.loadTasks();
  },

  async loadTasks() {
    if (!this.currentProjectId) return;

    const stages = ['DEVELOPMENT', 'PRE_PRODUCTION', 'PRODUCTION', 'POST_PRODUCTION', 'COMPLETED'];
    for (const stage of stages) {
      try {
        const res = await Api.getProductionTasks(this.currentProjectId, stage);
        const tasks = res.data || [];
        const countBadge = document.getElementById(`count-${stage}`);
        const colContainer = document.getElementById(`tasks-${stage}`);

        if (countBadge) countBadge.textContent = tasks.length;
        if (colContainer) {
          colContainer.innerHTML = tasks.map(t => `
            <div class="task-item">
              <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <strong style="font-size: 0.88rem; color: var(--text-primary);">${Workspace.escapeHtml(t.title)}</strong>
                <span class="badge ${t.status === 'COMPLETED' ? 'badge-green' : 'badge-gold'}" style="font-size: 0.65rem;">${t.status}</span>
              </div>
              <p style="font-size: 0.78rem; color: var(--text-secondary); margin-top: 4px;">${Workspace.escapeHtml(t.description || '')}</p>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
                <small style="color: var(--text-muted); font-size: 0.72rem;">Due: ${t.dueDate || 'No date'}</small>
                <div style="display: flex; gap: 4px;">
                  <button class="btn btn-secondary btn-sm" style="padding: 2px 6px; font-size: 0.7rem;" onclick="ProductionPipeline.editTask(${JSON.stringify(t).replace(/"/g, '&quot;')})">Edit</button>
                  <button class="btn btn-danger btn-sm" style="padding: 2px 6px; font-size: 0.7rem;" onclick="ProductionPipeline.deleteTask(${t.id})">✕</button>
                </div>
              </div>
            </div>
          `).join('');
        }
      } catch (err) {
        console.warn(err);
      }
    }
  },

  openNewProjectModal() {
    document.getElementById('production-project-form').reset();
    const spSelect = document.getElementById('new-prod-sp-id');
    if (spSelect) {
      spSelect.innerHTML = '<option value="">-- No Attached Screenplay --</option>' +
        ScreenwritingStudio.screenplays.map(sp => `<option value="${sp.id}">${Workspace.escapeHtml(sp.title)}</option>`).join('');
    }
    Workspace.openModal('modal-production-project');
  },

  async submitNewProject() {
    const title = document.getElementById('new-prod-title').value.trim();
    const screenplayId = document.getElementById('new-prod-sp-id').value || null;
    const director = document.getElementById('new-prod-director').value.trim();
    const producer = document.getElementById('new-prod-producer').value.trim();
    const targetCompletionDate = document.getElementById('new-prod-date').value || null;
    const description = document.getElementById('new-prod-desc').value.trim();

    try {
      const res = await Api.createProductionProject({
        title, screenplayId, director, producer, targetCompletionDate, description
      });
      Workspace.closeModal('modal-production-project');
      showToast('Production Pipeline created with default stage tasks!', 'success');
      await this.loadProjects();
      this.selectProject(res.data.id);
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteCurrentProject() {
    if (!this.currentProjectId) return;
    if (!confirm('Are you sure you want to delete this production project?')) return;
    try {
      await Api.deleteProductionProject(this.currentProjectId);
      showToast('Production project deleted.', 'info');
      this.currentProjectId = null;
      await this.loadProjects();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  openNewTaskModal() {
    if (!this.currentProjectId) {
      showToast('Please select a production project first.', 'error');
      return;
    }
    document.getElementById('production-task-form').reset();
    document.getElementById('task-edit-id').value = '';
    document.getElementById('task-modal-title').textContent = 'Add Pipeline Task';
    Workspace.openModal('modal-production-task');
  },

  editTask(t) {
    document.getElementById('task-edit-id').value = t.id;
    document.getElementById('task-stage').value = t.stage;
    document.getElementById('task-title').value = t.title;
    document.getElementById('task-status').value = t.status;
    document.getElementById('task-priority').value = t.priority;
    document.getElementById('task-due-date').value = t.dueDate || '';
    document.getElementById('task-desc').value = t.description || '';
    document.getElementById('task-modal-title').textContent = 'Edit Pipeline Task';
    Workspace.openModal('modal-production-task');
  },

  async submitTask() {
    const editId = document.getElementById('task-edit-id').value;
    const payload = {
      stage: document.getElementById('task-stage').value,
      title: document.getElementById('task-title').value.trim(),
      status: document.getElementById('task-status').value,
      priority: document.getElementById('task-priority').value,
      dueDate: document.getElementById('task-due-date').value || null,
      description: document.getElementById('task-desc').value.trim()
    };

    try {
      if (editId) {
        await Api.updateProductionTask(this.currentProjectId, editId, payload);
        showToast('Task updated!', 'success');
      } else {
        await Api.createProductionTask(this.currentProjectId, payload);
        showToast('Task created!', 'success');
      }
      Workspace.closeModal('modal-production-task');
      await this.loadProjects();
      this.refreshActiveProject();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteTask(taskId) {
    if (!confirm('Delete this task?')) return;
    try {
      await Api.deleteProductionTask(this.currentProjectId, taskId);
      showToast('Task removed.', 'info');
      await this.loadProjects();
      this.refreshActiveProject();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }
};

// ====================================================================
// MODULE 5: CINEMATIC LIBRARY LOGIC
// ====================================================================
const CinematicLibrary = {
  entries: [],

  async loadEntries() {
    const cat = document.getElementById('lib-category-filter')?.value || 'ALL';
    const q = document.getElementById('lib-search-input')?.value.trim() || '';

    try {
      const res = await Api.getLibraryEntries(cat, q);
      this.entries = res.data || [];
      const grid = document.getElementById('library-entries-grid');
      if (!grid) return;

      if (this.entries.length === 0) {
        grid.innerHTML = '<p style="color: var(--text-muted);">No observations found. Click "+ Add Observation" to record your study notes.</p>';
        return;
      }

      grid.innerHTML = this.entries.map(e => `
        <div class="card-panel" style="margin-bottom: 0; background: #151619;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
            <h4 style="font-size: 1.1rem; color: var(--text-primary);">${Workspace.escapeHtml(e.title)}</h4>
            <span class="badge badge-gold" style="font-size: 0.68rem;">${e.category}</span>
          </div>
          ${e.filmReference ? `<p style="color: var(--accent); font-size: 0.85rem; font-style: italic; margin-bottom: 6px;">🎬 ${Workspace.escapeHtml(e.filmReference)}</p>` : ''}
          <p style="color: var(--text-secondary); font-size: 0.88rem; line-height: 1.5; margin-bottom: 10px;">${Workspace.escapeHtml(e.personalObservations)}</p>
          ${e.tags ? `<p style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 12px;">Tags: ${Workspace.escapeHtml(e.tags)}</p>` : ''}
          <div style="display: flex; gap: 8px; justify-content: flex-end;">
            <button class="btn btn-secondary btn-sm" onclick="CinematicLibrary.editEntry(${JSON.stringify(e).replace(/"/g, '&quot;')})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="CinematicLibrary.deleteEntry(${e.id})">Delete</button>
          </div>
        </div>
      `).join('');
    } catch (err) {
      console.warn(err);
    }
  },

  onSearch() {
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.loadEntries(), 300);
  },

  openNewEntryModal() {
    document.getElementById('library-entry-form').reset();
    document.getElementById('lib-edit-id').value = '';
    document.getElementById('lib-modal-title').textContent = 'Add Cinematic Observation';
    Workspace.openModal('modal-library-entry');
  },

  editEntry(e) {
    document.getElementById('lib-edit-id').value = e.id;
    document.getElementById('lib-title').value = e.title;
    document.getElementById('lib-category').value = e.category;
    document.getElementById('lib-film').value = e.filmReference || '';
    document.getElementById('lib-desc').value = e.description || '';
    document.getElementById('lib-observations').value = e.personalObservations;
    document.getElementById('lib-tags').value = e.tags || '';
    document.getElementById('lib-modal-title').textContent = 'Edit Cinematic Observation';
    Workspace.openModal('modal-library-entry');
  },

  async submitEntry() {
    const editId = document.getElementById('lib-edit-id').value;
    const payload = {
      title: document.getElementById('lib-title').value.trim(),
      category: document.getElementById('lib-category').value,
      filmReference: document.getElementById('lib-film').value.trim(),
      description: document.getElementById('lib-desc').value.trim(),
      personalObservations: document.getElementById('lib-observations').value.trim(),
      tags: document.getElementById('lib-tags').value.trim()
    };

    try {
      if (editId) {
        await Api.updateLibraryEntry(editId, payload);
        showToast('Observation updated!', 'success');
      } else {
        await Api.createLibraryEntry(payload);
        showToast('Saved to Cinematic Library!', 'success');
      }
      Workspace.closeModal('modal-library-entry');
      this.loadEntries();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteEntry(id) {
    if (!confirm('Delete this observation entry?')) return;
    try {
      await Api.deleteLibraryEntry(id);
      showToast('Entry deleted.', 'info');
      this.loadEntries();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }
};

// ====================================================================
// MODULE 6: CREATIVE VAULT LOGIC
// ====================================================================
const CreativeVault = {
  ideas: [],
  favouritesOnly: false,

  async loadIdeas() {
    const cat = document.getElementById('vault-category-filter')?.value || 'ALL';
    const q = document.getElementById('vault-search-input')?.value.trim() || '';

    try {
      const res = await Api.getVaultIdeas(cat, this.favouritesOnly, q);
      this.ideas = res.data || [];
      const grid = document.getElementById('vault-ideas-grid');
      if (!grid) return;

      if (this.ideas.length === 0) {
        grid.innerHTML = '<p style="color: var(--text-muted);">No creative ideas found. Click "+ Capture New Idea" to store an inspiration.</p>';
        return;
      }

      grid.innerHTML = this.ideas.map(i => `
        <div class="card-panel" style="margin-bottom: 0; background: #151619; display: flex; flex-direction: column;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <button style="background: none; border: none; font-size: 1.15rem; cursor: pointer; color: ${i.favourite ? 'var(--accent)' : 'var(--text-muted)'};" onclick="CreativeVault.toggleFavourite(${i.id})">★</button>
              <h4 style="font-size: 1.1rem; color: var(--text-primary);">${Workspace.escapeHtml(i.title)}</h4>
            </div>
            <span class="badge badge-blue" style="font-size: 0.68rem;">${i.category}</span>
          </div>
          <p style="color: var(--text-secondary); font-size: 0.9rem; line-height: 1.6; flex: 1; margin: 10px 0;">${Workspace.escapeHtml(i.content)}</p>
          ${i.tags ? `<p style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 12px;">Tags: ${Workspace.escapeHtml(i.tags)}</p>` : ''}
          <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 12px; margin-top: 8px;">
            <button class="btn btn-primary btn-sm" onclick="CreativeVault.convertIdea(${i.id})">➔ Screenplay</button>
            <div style="display: flex; gap: 6px;">
              <button class="btn btn-secondary btn-sm" onclick="CreativeVault.editIdea(${JSON.stringify(i).replace(/"/g, '&quot;')})">Edit</button>
              <button class="btn btn-danger btn-sm" onclick="CreativeVault.deleteIdea(${i.id})">✕</button>
            </div>
          </div>
        </div>
      `).join('');
    } catch (err) {
      console.warn(err);
    }
  },

  toggleFavouritesFilter() {
    this.favouritesOnly = !this.favouritesOnly;
    const btn = document.getElementById('vault-fav-toggle');
    if (btn) {
      btn.style.color = this.favouritesOnly ? 'var(--accent)' : 'var(--text-secondary)';
      btn.style.borderColor = this.favouritesOnly ? 'var(--accent)' : 'var(--border-color)';
    }
    this.loadIdeas();
  },

  onSearch() {
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.loadIdeas(), 300);
  },

  openNewIdeaModal() {
    document.getElementById('vault-idea-form').reset();
    document.getElementById('vault-edit-id').value = '';
    document.getElementById('vault-modal-title').textContent = 'Capture Creative Idea';
    Workspace.openModal('modal-vault-idea');
  },

  editIdea(i) {
    document.getElementById('vault-edit-id').value = i.id;
    document.getElementById('vault-title').value = i.title;
    document.getElementById('vault-category').value = i.category;
    document.getElementById('vault-content').value = i.content;
    document.getElementById('vault-tags').value = i.tags || '';
    document.getElementById('vault-fav').checked = i.favourite;
    document.getElementById('vault-modal-title').textContent = 'Edit Creative Idea';
    Workspace.openModal('modal-vault-idea');
  },

  async submitIdea() {
    const editId = document.getElementById('vault-edit-id').value;
    const payload = {
      title: document.getElementById('vault-title').value.trim(),
      category: document.getElementById('vault-category').value,
      content: document.getElementById('vault-content').value.trim(),
      tags: document.getElementById('vault-tags').value.trim(),
      favourite: document.getElementById('vault-fav').checked
    };

    try {
      if (editId) {
        await Api.updateVaultIdea(editId, payload);
        showToast('Idea updated!', 'success');
      } else {
        await Api.createVaultIdea(payload);
        showToast('Idea preserved in Vault!', 'success');
      }
      Workspace.closeModal('modal-vault-idea');
      this.loadIdeas();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async toggleFavourite(id) {
    try {
      await Api.toggleIdeaFavourite(id);
      this.loadIdeas();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteIdea(id) {
    if (!confirm('Delete this idea from vault?')) return;
    try {
      await Api.deleteVaultIdea(id);
      showToast('Idea deleted.', 'info');
      this.loadIdeas();
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async convertIdea(id) {
    try {
      const res = await Api.convertIdeaToScreenplay(id);
      showToast('Converted into a Screenplay Project!', 'success');
      await ScreenwritingStudio.loadScreenplays();
      ScreenwritingStudio.selectAndOpen(res.data.id);
      Workspace.loadDashboardStats();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }
};

// Initialize Application when DOM ready
document.addEventListener('DOMContentLoaded', () => {
  Workspace.init();
});
