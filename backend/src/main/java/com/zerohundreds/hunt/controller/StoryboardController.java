package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.dto.StoryboardDtos.*;
import com.zerohundreds.hunt.entity.Storyboard;
import com.zerohundreds.hunt.entity.StoryboardFrame;
import com.zerohundreds.hunt.service.StoryboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storyboards")
public class StoryboardController {

    private final StoryboardService storyboardService;

    public StoryboardController(StoryboardService storyboardService) {
        this.storyboardService = storyboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Storyboard>>> getAllStoryboards() {
        return ResponseEntity.ok(ApiResponse.ok("Storyboards retrieved", storyboardService.getAllStoryboards()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Storyboard>> getStoryboard(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Storyboard retrieved", storyboardService.getStoryboard(id)));
    }

    @GetMapping("/screenplay/{screenplayId}")
    public ResponseEntity<ApiResponse<List<Storyboard>>> getStoryboardsByScreenplay(@PathVariable Long screenplayId) {
        return ResponseEntity.ok(ApiResponse.ok("Storyboards for screenplay retrieved", storyboardService.getStoryboardsByScreenplay(screenplayId)));
    }

    @GetMapping("/{id}/frames")
    public ResponseEntity<ApiResponse<List<StoryboardFrame>>> getFrames(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Storyboard frames retrieved", storyboardService.getStoryboardFrames(id)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Storyboard>> generateStoryboard(@Valid @RequestBody GenerateStoryboardRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Visual Storyboard generated successfully", storyboardService.generateStoryboard(request)));
    }

    @PutMapping("/frames/{frameId}")
    public ResponseEntity<ApiResponse<StoryboardFrame>> updateFrame(
            @PathVariable Long frameId,
            @RequestBody UpdateFrameRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Frame updated", storyboardService.updateFrame(frameId, request)));
    }

    @PostMapping("/frames/{frameId}/regenerate")
    public ResponseEntity<ApiResponse<StoryboardFrame>> regenerateFrame(@PathVariable Long frameId) {
        return ResponseEntity.ok(ApiResponse.ok("Frame regenerated", storyboardService.regenerateSingleFrame(frameId)));
    }

    @DeleteMapping("/frames/{frameId}")
    public ResponseEntity<ApiResponse<String>> deleteFrame(@PathVariable Long frameId) {
        storyboardService.deleteFrame(frameId);
        return ResponseEntity.ok(ApiResponse.ok("Frame deleted successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteStoryboard(@PathVariable Long id) {
        storyboardService.deleteStoryboard(id);
        return ResponseEntity.ok(ApiResponse.ok("Storyboard deleted successfully"));
    }
}
