package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.UserEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserEvaluationRepository extends JpaRepository<UserEvaluation, Long> {
    List<UserEvaluation> findByTargetId(Long targetId);
}
