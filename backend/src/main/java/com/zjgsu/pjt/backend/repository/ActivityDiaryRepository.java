package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityDiaryRepository extends JpaRepository<ActivityDiary, Long> {
    List<ActivityDiary> findByUserId(Long userId);
}
