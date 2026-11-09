package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.StoryboardFrame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryboardFrameRepository extends JpaRepository<StoryboardFrame, Long> {
    List<StoryboardFrame> findByStoryboardIdOrderByDisplayOrderAsc(Long storyboardId);
    List<StoryboardFrame> findByStoryboardIdAndSceneNumberOrderByDisplayOrderAsc(Long storyboardId, Integer sceneNumber);
    void deleteByStoryboardId(Long storyboardId);
}
