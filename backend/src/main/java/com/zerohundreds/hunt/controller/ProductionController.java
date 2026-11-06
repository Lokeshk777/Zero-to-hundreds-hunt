package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.dto.ProductionDtos.*;
import com.zerohundreds.hunt.entity.ProductionProject;
import com.zerohundreds.hunt.entity.ProductionTask;
import com.zerohundreds.hunt.service.ProductionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProductionProject>>> getAllProjects() {
        return ResponseEntity.ok(ApiResponse.ok("Production projects retrieved", productionService.getAllProjects()));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProductionProject>> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Production project retrieved", productionService.getProject(id)));
    }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProductionProject>> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Production project created", productionService.createProject(request)));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProductionProject>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Production project updated", productionService.updateProject(id, request)));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        productionService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.ok("Production project deleted successfully"));
    }

    // Tasks
    @GetMapping("/projects/{id}/tasks")
    public ResponseEntity<ApiResponse<List<ProductionTask>>> getTasks(
            @PathVariable Long id,
            @RequestParam(required = false) String stage) {
        return ResponseEntity.ok(ApiResponse.ok("Production tasks retrieved", productionService.getTasks(id, stage)));
    }

    @PostMapping("/projects/{id}/tasks")
    public ResponseEntity<ApiResponse<ProductionTask>> createTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Production task created", productionService.createTask(id, request)));
    }

    @PutMapping("/projects/{id}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<ProductionTask>> updateTask(
            @PathVariable Long id,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Production task updated", productionService.updateTask(id, taskId, request)));
    }

    @DeleteMapping("/projects/{id}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<String>> deleteTask(
            @PathVariable Long id,
            @PathVariable Long taskId) {
        productionService.deleteTask(id, taskId);
        return ResponseEntity.ok(ApiResponse.ok("Production task deleted successfully"));
    }
}
