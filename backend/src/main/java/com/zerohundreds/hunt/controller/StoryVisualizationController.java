package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.entity.StoryCharacterReference;
import com.zerohundreds.hunt.entity.StoryVisualization;
import com.zerohundreds.hunt.entity.VisualizationImage;
import com.zerohundreds.hunt.service.StoryVisualizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/story-visualizer")
public class StoryVisualizationController {

    private final StoryVisualizationService visualizationService;

    public StoryVisualizationController(StoryVisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<StoryVisualizationService.FullVisualizationResponse>> generateVisualization(
            @RequestBody Map<String, Object> req) {
        Long screenplayId = Long.valueOf(req.get("screenplayId").toString());
        String visualStyle = req.containsKey("visualStyle") ? (String) req.get("visualStyle") : "Cinematic Photorealism";
        String aspectRatio = req.containsKey("aspectRatio") ? (String) req.get("aspectRatio") : "16:9";

        StoryVisualizationService.FullVisualizationResponse res = visualizationService.generateVisualization(
                screenplayId, visualStyle, aspectRatio
        );

        return ResponseEntity.ok(ApiResponse.ok("Story visualization generated successfully", res));
    }

    @GetMapping("/{visualizationId}")
    public ResponseEntity<ApiResponse<StoryVisualizationService.FullVisualizationResponse>> getVisualization(
            @PathVariable Long visualizationId) {
        return ResponseEntity.ok(ApiResponse.ok("Visualization retrieved", visualizationService.getVisualizationDetail(visualizationId)));
    }

    @PostMapping("/characters/{characterId}/approve")
    public ResponseEntity<ApiResponse<StoryCharacterReference>> approveCharacter(
            @PathVariable Long characterId) {
        return ResponseEntity.ok(ApiResponse.ok("Character reference approved", visualizationService.approveCharacter(characterId)));
    }

    @PostMapping("/images/{imageId}/regenerate")
    public ResponseEntity<ApiResponse<VisualizationImage>> regenerateImage(
            @PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.ok("Scene image regenerated", visualizationService.regenerateSingleImage(imageId)));
    }
}
