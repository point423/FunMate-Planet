package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.repository.UserEvaluationRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private UserEvaluationRepository evaluationRepository;

    @Autowired
    private UserRepository userRepository;

    public UserEvaluation createEvaluation(UserEvaluation evaluation) {
        UserEvaluation saved = evaluationRepository.save(evaluation);
        updateUserAverageScore(evaluation.getTargetId());
        return saved;
    }

    public List<UserEvaluation> getEvaluationsByTargetId(Long targetId) {
        return evaluationRepository.findByTargetId(targetId);
    }

    public List<UserEvaluation> getEvaluationsByEvaluatorId(Long evaluatorId) {
        return evaluationRepository.findAll().stream()
                .filter(eval -> eval.getEvaluatorId().equals(evaluatorId))
                .collect(Collectors.toList());
    }

    public Optional<UserEvaluation> getEvaluationById(Long id) {
        return evaluationRepository.findById(id);
    }

    public UserEvaluation updateEvaluation(Long id, UserEvaluation updatedEvaluation) {
        Optional<UserEvaluation> existingOpt = evaluationRepository.findById(id);
        if (existingOpt.isPresent()) {
            UserEvaluation existing = existingOpt.get();
            existing.setScoreLevel(updatedEvaluation.getScoreLevel());
            existing.setActivityId(updatedEvaluation.getActivityId());
            UserEvaluation saved = evaluationRepository.save(existing);
            updateUserAverageScore(existing.getTargetId());
            return saved;
        }
        return null;
    }

    public boolean deleteEvaluation(Long id) {
        Optional<UserEvaluation> evaluationOpt = evaluationRepository.findById(id);
        if (evaluationOpt.isPresent()) {
            Long targetId = evaluationOpt.get().getTargetId();
            evaluationRepository.deleteById(id);
            updateUserAverageScore(targetId);
            return true;
        }
        return false;
    }

    private void updateUserAverageScore(Long targetId) {
        List<UserEvaluation> evals = evaluationRepository.findByTargetId(targetId);
        double avg = evals.stream().mapToInt(UserEvaluation::getScoreLevel).average().orElse(0.0);

        User target = userRepository.findById(targetId).orElse(null);
        if (target != null) {
            target.setAverageScore(avg);
            userRepository.save(target);
        }
    }
}
