package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.repository.UserEvaluationRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EvaluationServiceTest {

    @Mock
    private UserEvaluationRepository evaluationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    @DisplayName("测试用户评价逻辑-自动更新好评率")
    void evaluate_Success_UpdatesUserPositiveRate() {
        UserEvaluation evaluation = new UserEvaluation();
        evaluation.setTargetId(1L);
        evaluation.setScoreLevel(3);

        User target = new User();
        target.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(evaluationRepository.findByTargetId(1L)).thenReturn(Collections.singletonList(evaluation));

        evaluationService.createEvaluation(evaluation);

        verify(evaluationRepository).save(evaluation);
        verify(userRepository).save(target);
        assertEquals(100.0, target.getAverageScore());
    }

    @Test
    @DisplayName("安全校验-越权修改他人评价应返回null")
    void updateEvaluation_Forbidden_ReturnsNull() {
        UserEvaluation existing = new UserEvaluation();
        existing.setId(1L);
        existing.setEvaluatorId(99L); // 评价人是 99

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(existing));

        // 当前用户是 1L，尝试修改 99L 的评价
        UserEvaluation result = evaluationService.updateEvaluation(1L, new UserEvaluation(), 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("安全校验-越权删除他人评价应返回false")
    void deleteEvaluation_Forbidden_ReturnsFalse() {
        UserEvaluation existing = new UserEvaluation();
        existing.setId(1L);
        existing.setEvaluatorId(99L);

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = evaluationService.deleteEvaluation(1L, 1L);
        assertFalse(result);
    }
}
