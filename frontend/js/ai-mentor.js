// AI Mentor front-end integration
const AiMentor = (function () {
  const chatHistory = document.getElementById('ai-chat-history');
  const inputEl = document.getElementById('ai-input');
  const previewBar = document.getElementById('ai-media-preview-bar');
  const mediaBadge = document.getElementById('ai-media-badge');
  const mediaName = document.getElementById('ai-media-filename');
  const fileInput = document.getElementById('ai-media-file');
  const voiceBtn = document.getElementById('ai-voice-btn');
  let uploadedFile = null;
  let recognizer = null;
  let isRecording = false;

  // Speech-to-Text Initialization
  const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (SpeechRec) {
    recognizer = new SpeechRec();
    recognizer.lang = 'en-US';
    recognizer.interimResults = false;
    recognizer.continuous = false;

    recognizer.onstart = () => {
      isRecording = true;
      if (voiceBtn) {
        voiceBtn.style.backgroundColor = '#E75A5A';
        voiceBtn.style.transform = 'scale(1.15)';
        voiceBtn.title = 'Listening... Speak into microphone';
      }
      showToast('Listening... Speak now!', 'info');
    };

    recognizer.onresult = e => {
      const txt = e.results[0][0].transcript;
      if (inputEl) {
        inputEl.value = (inputEl.value ? inputEl.value + ' ' : '') + txt;
        inputEl.focus();
      }
    };

    recognizer.onerror = e => {
      console.warn('Voice recognition error:', e.error);
      showToast('Microphone error: ' + (e.error || 'Access denied'), 'error');
      stopVoice();
    };

    recognizer.onend = () => {
      stopVoice();
    };
  }

  function stopVoice() {
    isRecording = false;
    if (voiceBtn) {
      voiceBtn.style.backgroundColor = '';
      voiceBtn.style.transform = '';
      voiceBtn.title = 'Speak via Voice Recorder';
    }
  }

  function toggleVoiceRecording() {
    if (!recognizer) {
      alert('Speech Recognition is not supported by your browser. Please use Google Chrome or Edge.');
      return;
    }
    if (isRecording) {
      recognizer.stop();
      stopVoice();
    } else {
      try {
        recognizer.start();
      } catch (err) {
        console.warn(err);
      }
    }
  }

  function handleMediaUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    uploadedFile = file;
    mediaBadge.textContent = file.type.startsWith('image') ? 'PHOTO' : 'VIDEO';
    mediaName.textContent = file.name;
    previewBar.style.display = 'flex';
  }

  function removeUploadedMedia() {
    uploadedFile = null;
    if (fileInput) fileInput.value = '';
    if (previewBar) previewBar.style.display = 'none';
  }

  async function sendMessage() {
    const question = inputEl ? inputEl.value.trim() : '';
    if (!question && !uploadedFile) {
      alert('Enter a question or attach a photo/video.');
      return;
    }

    // Display user question in chat bubble
    appendChat('user', question || `[Uploaded ${uploadedFile.name}]`);
    if (inputEl) inputEl.value = '';

    // Create assistant loading placeholder
    const loadingId = 'ai-loading-' + Date.now();
    const loadingBubble = document.createElement('div');
    loadingBubble.className = 'chat-bubble assistant';
    loadingBubble.id = loadingId;
    loadingBubble.innerHTML = '<em>Consulting filmmaking knowledge base...</em>';
    chatHistory.appendChild(loadingBubble);
    chatHistory.scrollTop = chatHistory.scrollHeight;

    try {
      // Use Api module communicating with backend
      const res = await Api.sendAiMessage({
        message: question,
        mediaName: uploadedFile ? uploadedFile.name : null,
        mediaType: uploadedFile ? uploadedFile.type : null,
        screenplayId: typeof ScreenwritingStudio !== 'undefined' ? ScreenwritingStudio.currentScreenplayId : null
      });

      const replyText = res.data?.reply || res.answer || 'Thank you for your question. How else can I assist your filmmaking?';
      
      const formatted = Workspace.escapeHtml(replyText)
        .replace(/\n\n/g, '<br><br>')
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>');

      loadingBubble.innerHTML = formatted;
    } catch (err) {
      console.warn('AI Mentor call failed', err);
      loadingBubble.innerHTML = `<span style="color: var(--color-danger);">Could not connect to mentor: ${err.message}</span>`;
    } finally {
      removeUploadedMedia();
      chatHistory.scrollTop = chatHistory.scrollHeight;
    }
  }

  function appendChat(role, text) {
    const bubble = document.createElement('div');
    bubble.className = `chat-bubble ${role}`;
    bubble.textContent = text;
    chatHistory.appendChild(bubble);
    chatHistory.scrollTop = chatHistory.scrollHeight;
  }

  return {
    toggleVoiceRecording,
    handleMediaUpload,
    removeUploadedMedia,
    sendMessage,
    askQuick: q => {
      if (inputEl) inputEl.value = q;
      sendMessage();
    }
  };
})();
