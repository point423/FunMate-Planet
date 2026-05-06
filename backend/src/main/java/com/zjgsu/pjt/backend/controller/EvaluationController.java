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
                                                    @RequestAttribute("currentUserId") Long currentUserId) {
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
        return evaluationService.getEvaluationById(id)
                .map(Result::success)
                .orElse(Result.error(404, "评价不存在"));
    }

    /**
     * 安全修复：防止水平越权 (IDOR)
     */
    @PutMapping("/{id}")
    public Result<UserEvaluation> updateEvaluation(@PathVariable Long id,
                                                    @RequestBody UserEvaluation updatedEvaluation,
                                                    @RequestAttribute("currentUserId") Long currentUserId) {
        UserEvaluation updated = evaluationService.updateEvaluation(id, updatedEvaluation, currentUserId);
        if (updated == null) {
            return Result.error(403, "无权修改此评价或评价不存在");
        }
        return Result.success(updated);
    }

    /**
     * 安全修复：防止水平越权 (IDOR)
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteEvaluation(@PathVariable Long id,
                                           @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = evaluationService.deleteEvaluation(id, currentUserId);
        return success ? Result.success("评价已删除") : Result.error(403, "无权删除此评价或评价不存在");
    }
}
