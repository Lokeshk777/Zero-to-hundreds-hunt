package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.dto.LibraryDtos.EntryRequest;
import com.zerohundreds.hunt.entity.CinematicLibraryEntry;
import com.zerohundreds.hunt.service.CinematicLibraryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class CinematicLibraryController {

    private final CinematicLibraryService libraryService;

    public CinematicLibraryController(CinematicLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CinematicLibraryEntry>>> getEntries(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok("Library entries retrieved", libraryService.getAllEntries(category, q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CinematicLibraryEntry>> getEntry(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Library entry retrieved", libraryService.getEntry(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CinematicLibraryEntry>> createEntry(@Valid @RequestBody EntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Library entry saved", libraryService.createEntry(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CinematicLibraryEntry>> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody EntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Library entry updated", libraryService.updateEntry(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEntry(@PathVariable Long id) {
        libraryService.deleteEntry(id);
        return ResponseEntity.ok(ApiResponse.ok("Library entry deleted successfully"));
    }
}
