package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.AiDtos.*;
import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.entity.AiConversation;
import com.zerohundreds.hunt.entity.AiMessage;
import com.zerohundreds.hunt.service.AiMentorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-mentor")
public class AiMentorController {

    private final AiMentorService aiMentorService;

    public AiMentorController(AiMentorService aiMentorService) {
        this.aiMentorService = aiMentorService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<AiConversation>>> getConversations() {
        return ResponseEntity.ok(ApiResponse.ok("Conversations retrieved", aiMentorService.getUserConversations()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<List<AiMessage>>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Messages retrieved", aiMentorService.getConversationMessages(id)));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("AI Mentor response", aiMentorService.chat(request)));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<String>> deleteConversation(@PathVariable Long id) {
        aiMentorService.deleteConversation(id);
        return ResponseEntity.ok(ApiResponse.ok("Conversation deleted successfully"));
    }
}
