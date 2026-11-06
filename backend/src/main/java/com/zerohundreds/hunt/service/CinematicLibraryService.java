package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.dto.LibraryDtos.EntryRequest;
import com.zerohundreds.hunt.entity.CinematicLibraryEntry;
import com.zerohundreds.hunt.entity.User;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.CinematicLibraryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CinematicLibraryService {

    private final CinematicLibraryEntryRepository libraryRepository;
    private final AuthService authService;

    public CinematicLibraryService(CinematicLibraryEntryRepository libraryRepository, AuthService authService) {
        this.libraryRepository = libraryRepository;
        this.authService = authService;
    }

    public List<CinematicLibraryEntry> getAllEntries(String category, String query) {
        User user = authService.getCurrentUser();
        if (query != null && !query.trim().isEmpty()) {
            return libraryRepository.searchEntries(user, query.trim());
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            return libraryRepository.findByUserAndCategoryOrderByUpdatedAtDesc(user, category.trim());
        }
        return libraryRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public CinematicLibraryEntry getEntry(Long id) {
        User user = authService.getCurrentUser();
        return libraryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Library entry not found: " + id));
    }

    @Transactional
    public CinematicLibraryEntry createEntry(EntryRequest request) {
        User user = authService.getCurrentUser();
        CinematicLibraryEntry entry = new CinematicLibraryEntry();
        entry.setUser(user);
        entry.setTitle(request.getTitle().trim());
        entry.setCategory(request.getCategory().trim().toUpperCase());
        entry.setFilmReference(request.getFilmReference());
        entry.setDescription(request.getDescription());
        entry.setPersonalObservations(request.getPersonalObservations().trim());
        entry.setTags(request.getTags());
        return libraryRepository.save(entry);
    }

    @Transactional
    public CinematicLibraryEntry updateEntry(Long id, EntryRequest request) {
        CinematicLibraryEntry entry = getEntry(id);
        entry.setTitle(request.getTitle().trim());
        entry.setCategory(request.getCategory().trim().toUpperCase());
        entry.setFilmReference(request.getFilmReference());
        entry.setDescription(request.getDescription());
        entry.setPersonalObservations(request.getPersonalObservations().trim());
        entry.setTags(request.getTags());
        return libraryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long id) {
        CinematicLibraryEntry entry = getEntry(id);
        libraryRepository.delete(entry);
    }
}
