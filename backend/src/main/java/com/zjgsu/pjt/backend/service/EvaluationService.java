package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.repository.UserEvaluationRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private UserEvaluationRepository evaluationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserEvaluation createEvaluation(UserEvaluation evaluation) {
        UserEvaluation saved = evaluationRepository.save(evaluation);
        updateUserPositiveFeedbackRate(evaluation.getTargetId());
        return saved;
    }

    public List<UserEvaluation> getEvaluationsByTargetId(Long targetId) {
        return evaluationRepository.findByTargetId(targetId);
    }

    public List<UserEvaluation> getEvaluationsByEvaluatorId(Long evaluatorId) {
        return evaluationRepository.findAll().stream()
                .filter(eval -> Objects.equals(eval.getEvaluatorId(), evaluatorId))
                .collect(Collectors.toList());
    }

    public Optional<UserEvaluation> getEvaluationById(Long id) {
        return evaluationRepository.findById(id);
    }

    /**
     * 安全加固：更新评价（增加越权校验）
     */
    @Transactional
    public UserEvaluation updateEvaluation(Long id, UserEvaluation updated, Long currentUserId) {
        UserEvaluation existing = evaluationRepository.findById(id).orElse(null);
        if (existing != null && Objects.equals(existing.getEvaluatorId(), currentUserId)) {
            existing.setScoreLevel(updated.getScoreLevel());
            existing.setActivityId(updated.getActivityId());
            UserEvaluation saved = evaluationRepository.save(existing);
            updateUserPositiveFeedbackRate(existing.getTargetId());
            return saved;
        }
        return null;
    }

    /**
     * 安全加固：删除评价（增加越权校验）
     */
    @Transactional
    public boolean deleteEvaluation(Long id, Long currentUserId) {
        UserEvaluation evaluation = evaluationRepository.findById(id).orElse(null);
        if (evaluation != null && Objects.equals(evaluation.getEvaluatorId(), currentUserId)) {
            Long targetId = evaluation.getTargetId();
            evaluationRepository.deleteById(id);
            updateUserPositiveFeedbackRate(targetId);
            return true;
        }
        return false;
    }

    private void updateUserPositiveFeedbackRate(Long targetId) {
        List<UserEvaluation> evals = evaluationRepository.findByTargetId(targetId);
        long total = evals.stream()
                .filter(eval -> eval.getScoreLevel() != null)
                .count();
        long positive = evals.stream()
                .filter(eval -> eval.getScoreLevel() != null && eval.getScoreLevel() == 3)
                .count();
        double positiveRate = total == 0 ? 0.0 : (positive * 100.0 / total);
        User target = userRepository.findById(targetId).orElse(null);
        if (target != null) {
            target.setAverageScore(Math.round(positiveRate * 10.0) / 10.0);
            userRepository.save(target);
        }
    }
}
