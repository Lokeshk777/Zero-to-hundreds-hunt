package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.dto.DashboardSummaryDto;
import com.zerohundreds.hunt.dto.DashboardSummaryDto.*;
import com.zerohundreds.hunt.entity.CreativeIdea;
import com.zerohundreds.hunt.entity.ProductionProject;
import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.User;
import com.zerohundreds.hunt.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ScreenplayProjectRepository screenplayRepository;
    private final CreativeIdeaRepository ideaRepository;
    private final ProductionProjectRepository productionRepository;
    private final CinematicLibraryEntryRepository libraryRepository;
    private final AuthService authService;

    public DashboardService(ScreenplayProjectRepository screenplayRepository,
                            CreativeIdeaRepository ideaRepository,
                            ProductionProjectRepository productionRepository,
                            CinematicLibraryEntryRepository libraryRepository,
                            AuthService authService) {
        this.screenplayRepository = screenplayRepository;
        this.ideaRepository = ideaRepository;
        this.productionRepository = productionRepository;
        this.libraryRepository = libraryRepository;
        this.authService = authService;
    }

    public DashboardSummaryDto getDashboardSummary() {
        User user = authService.getCurrentUser();

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setUserFullName(user.getFullName());
        dto.setUsername(user.getUsername());

        dto.setTotalScreenplays(screenplayRepository.countByUser(user));
        dto.setTotalCreativeIdeas(ideaRepository.countByUser(user));
        dto.setActiveProductionProjects(productionRepository.countByUserAndStageNot(user, "COMPLETED"));
        dto.setTotalLibraryEntries(libraryRepository.countByUser(user));

        List<ScreenplayProject> screenplays = screenplayRepository.findTop5ByUserOrderByUpdatedAtDesc(user);
        dto.setRecentScreenplays(screenplays.stream().map(sp -> new ScreenplaySummary(
                sp.getId(),
                sp.getTitle(),
                sp.getGenre(),
                sp.getStatus(),
                sp.getUpdatedAt() != null ? sp.getUpdatedAt().toString() : ""
        )).collect(Collectors.toList()));

        List<CreativeIdea> ideas = ideaRepository.findTop5ByUserOrderByUpdatedAtDesc(user);
        dto.setRecentIdeas(ideas.stream().map(ci -> new IdeaSummary(
                ci.getId(),
                ci.getTitle(),
                ci.getCategory(),
                ci.isFavourite(),
                ci.getUpdatedAt() != null ? ci.getUpdatedAt().toString() : ""
        )).collect(Collectors.toList()));

        List<ProductionProject> productions = productionRepository.findByUserOrderByUpdatedAtDesc(user);
        dto.setRecentProductions(productions.stream().limit(5).map(pp -> new ProductionSummary(
                pp.getId(),
                pp.getTitle(),
                pp.getStage(),
                pp.getProgressPercentage() != null ? pp.getProgressPercentage() : 0
        )).collect(Collectors.toList()));

        return dto;
    }
}
