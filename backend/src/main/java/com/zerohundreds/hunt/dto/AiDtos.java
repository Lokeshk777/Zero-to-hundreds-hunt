package com.zerohundreds.hunt.dto;

import jakarta.validation.constraints.NotBlank;

public class AiDtos {

    public static class ChatRequest {
        private Long conversationId;
        private Long screenplayId;

        @NotBlank(message = "Message prompt cannot be empty")
        private String message;

        // Base64 or metadata for uploaded media
        private String mediaData;     // Base64 payload (data:image/png;base64,... or data:video/mp4;base64,...)
        private String mediaType;     // "image" or "video"
        private String mediaName;     // Original filename

        public ChatRequest() {}

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
        public Long getScreenplayId() { return screenplayId; }
        public void setScreenplayId(Long screenplayId) { this.screenplayId = screenplayId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getMediaData() { return mediaData; }
        public void setMediaData(String mediaData) { this.mediaData = mediaData; }
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        public String getMediaName() { return mediaName; }
        public void setMediaName(String mediaName) { this.mediaName = mediaName; }
    }

    public static class ChatResponse {
        private Long conversationId;
        private String reply;
        private boolean configured;

        public ChatResponse(Long conversationId, String reply, boolean configured) {
            this.conversationId = conversationId;
            this.reply = reply;
            this.configured = configured;
        }

        public Long getConversationId() { return conversationId; }
        public String getReply() { return reply; }
        public boolean isConfigured() { return configured; }
    }
}
