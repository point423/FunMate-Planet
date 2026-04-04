package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.repository.UserEvaluationRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "*")
public class EvaluationController {

    @Autowired
    private UserEvaluationRepository evaluationRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Result<String> evaluate(@RequestBody UserEvaluation evaluation,
                                   HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        // 设置当前用户为评价者
        evaluation.setEvaluatorId(currentUserId);

        // 保存评价
        evaluationRepository.save(evaluation);

        // 更新被评价人的平均分 (1:低/1分, 2:中/2分, 3:高/3分)
        List<UserEvaluation> evals = evaluationRepository.findByTargetId(evaluation.getTargetId());
        double avg = evals.stream().mapToInt(UserEvaluation::getScoreLevel).average().orElse(0.0);

        User target = userRepository.findById(evaluation.getTargetId()).orElse(null);
        if (target != null) {
            target.setAverageScore(avg);
            userRepository.save(target);
        }

        return Result.created("评价成功，已更新用户评分");
    }
}