package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "*")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping
    public Result<UserEvaluation> createEvaluation(@RequestBody UserEvaluation evaluation,
                                                    @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }

        evaluation.setEvaluatorId(currentUserId);
        UserEvaluation created = evaluationService.createEvaluation(evaluation);
        return Result.created(created);
    }

    @GetMapping("/target/{targetId}")
    public Result<List<UserEvaluation>> getEvaluationsByTargetId(@PathVariable Long targetId) {
        return Result.success(evaluationService.getEvaluationsByTargetId(targetId));
    }

    @GetMapping("/evaluator/{evaluatorId}")
    public Result<List<UserEvaluation>> getEvaluationsByEvaluatorId(@PathVariable Long evaluatorId) {
        return Result.success(evaluationService.getEvaluationsByEvaluatorId(evaluatorId));
    }

    @GetMapping("/{id}")
    public Result<UserEvaluation> getEvaluationById(@PathVariable Long id) {
        Optional<UserEvaluation> evaluation = evaluationService.getEvaluationById(id);
        return evaluation.map(Result::success).orElseGet(() -> Result.error(404, "评价不存在"));
    }

    @PutMapping("/{id}")
    public Result<UserEvaluation> updateEvaluation(@PathVariable Long id,
                                                    @RequestBody UserEvaluation updatedEvaluation) {
        UserEvaluation updated = evaluationService.updateEvaluation(id, updatedEvaluation);
        return updated != null ? Result.success(updated) : Result.error(404, "评价不存在");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteEvaluation(@PathVariable Long id) {
        boolean success = evaluationService.deleteEvaluation(id);
        return success ? Result.success("评价已删除") : Result.error(404, "评价不存在");
    }
}
