package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Page<Activity> findByStatus(Integer status, Pageable pageable);
    List<Activity> findByIdIn(Collection<Long> ids);
    List<Activity> findByCreatorIdAndStatus(Long creatorId, Integer status);
}
