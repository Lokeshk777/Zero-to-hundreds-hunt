package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.dto.ScreenplayDtos.ScreenplayRequest;
import com.zerohundreds.hunt.dto.VaultDtos.IdeaRequest;
import com.zerohundreds.hunt.entity.CreativeIdea;
import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.User;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.CreativeIdeaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreativeVaultService {

    private final CreativeIdeaRepository vaultRepository;
    private final ScreenplayService screenplayService;
    private final AuthService authService;

    public CreativeVaultService(CreativeIdeaRepository vaultRepository,
                                ScreenplayService screenplayService,
                                AuthService authService) {
        this.vaultRepository = vaultRepository;
        this.screenplayService = screenplayService;
        this.authService = authService;
    }

    public List<CreativeIdea> getAllIdeas(String category, Boolean favouritesOnly, String query) {
        User user = authService.getCurrentUser();
        if (query != null && !query.trim().isEmpty()) {
            return vaultRepository.searchIdeas(user, query.trim());
        }
        if (Boolean.TRUE.equals(favouritesOnly)) {
            return vaultRepository.findByUserAndIsFavouriteTrueOrderByUpdatedAtDesc(user);
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            return vaultRepository.findByUserAndCategoryOrderByUpdatedAtDesc(user, category.trim());
        }
        return vaultRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public CreativeIdea getIdea(Long id) {
        User user = authService.getCurrentUser();
        return vaultRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Creative idea not found: " + id));
    }

    @Transactional
    public CreativeIdea createIdea(IdeaRequest request) {
        User user = authService.getCurrentUser();
        CreativeIdea idea = new CreativeIdea();
        idea.setUser(user);
        idea.setTitle(request.getTitle().trim());
        idea.setCategory(request.getCategory().trim().toUpperCase());
        idea.setContent(request.getContent().trim());
        idea.setTags(request.getTags());
        idea.setFavourite(request.isFavourite());
        return vaultRepository.save(idea);
    }

    @Transactional
    public CreativeIdea updateIdea(Long id, IdeaRequest request) {
        CreativeIdea idea = getIdea(id);
        idea.setTitle(request.getTitle().trim());
        idea.setCategory(request.getCategory().trim().toUpperCase());
        idea.setContent(request.getContent().trim());
        idea.setTags(request.getTags());
        idea.setFavourite(request.isFavourite());
        return vaultRepository.save(idea);
    }

    @Transactional
    public CreativeIdea toggleFavourite(Long id) {
        CreativeIdea idea = getIdea(id);
        idea.setFavourite(!idea.isFavourite());
        return vaultRepository.save(idea);
    }

    @Transactional
    public void deleteIdea(Long id) {
        CreativeIdea idea = getIdea(id);
        vaultRepository.delete(idea);
    }

    @Transactional
    public ScreenplayProject convertToScreenplay(Long id) {
        CreativeIdea idea = getIdea(id);

        ScreenplayRequest req = new ScreenplayRequest();
        req.setTitle(idea.getTitle());
        req.setGenre("Drama / Thriller");
        req.setLogline(idea.getContent().length() > 200 ? idea.getContent().substring(0, 197) + "..." : idea.getContent());
        req.setStoryConcept(idea.getContent());
        req.setTheme("Transformation");
        req.setStatus("DRAFT");
        req.setStructureType("THREE_ACT");

        ScreenplayProject project = screenplayService.createScreenplay(req);
        idea.setConvertedToScreenplay(project);
        vaultRepository.save(idea);

        return project;
    }
}
