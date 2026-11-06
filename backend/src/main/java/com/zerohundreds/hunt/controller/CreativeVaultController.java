package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.dto.VaultDtos.IdeaRequest;
import com.zerohundreds.hunt.entity.CreativeIdea;
import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.service.CreativeVaultService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
public class CreativeVaultController {

    private final CreativeVaultService vaultService;

    public CreativeVaultController(CreativeVaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreativeIdea>>> getIdeas(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean favouritesOnly,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok("Ideas retrieved", vaultService.getAllIdeas(category, favouritesOnly, q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreativeIdea>> getIdea(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Idea retrieved", vaultService.getIdea(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreativeIdea>> createIdea(@Valid @RequestBody IdeaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Idea saved to vault", vaultService.createIdea(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CreativeIdea>> updateIdea(
            @PathVariable Long id,
            @Valid @RequestBody IdeaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Idea updated", vaultService.updateIdea(id, request)));
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<ApiResponse<CreativeIdea>> toggleFavourite(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Favourite updated", vaultService.toggleFavourite(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteIdea(@PathVariable Long id) {
        vaultService.deleteIdea(id);
        return ResponseEntity.ok(ApiResponse.ok("Idea deleted from vault"));
    }

    @PostMapping("/{id}/convert-to-screenplay")
    public ResponseEntity<ApiResponse<ScreenplayProject>> convertToScreenplay(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Idea successfully converted into a Screenplay Project!", vaultService.convertToScreenplay(id)));
    }
}
