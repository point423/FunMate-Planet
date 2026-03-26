package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Page<Activity> findByStatus(Integer status, Pageable pageable);
}
