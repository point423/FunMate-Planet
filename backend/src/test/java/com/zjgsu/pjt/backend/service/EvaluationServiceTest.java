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
    @DisplayName("测试用户评价逻辑-自动更新平均分")
    void evaluate_Success_UpdatesUserScore() {
        UserEvaluation evaluation = new UserEvaluation();
        evaluation.setTargetId(1L);
        evaluation.setScoreLevel(3);

        User target = new User();
        target.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(evaluationRepository.findByTargetId(1L)).thenReturn(Collections.singletonList(evaluation));

        // ✅ 修复：方法名由 evaluate 改为 createEvaluation
        evaluationService.createEvaluation(evaluation);

        verify(evaluationRepository).save(evaluation);
        verify(userRepository).save(target);
        assertEquals(3.0, target.getAverageScore());
    }
}
