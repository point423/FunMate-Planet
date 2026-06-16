package com.zjgsu.pjt.backend.service;


import com.zjgsu.pjt.backend.entity.ActivityDiary;

import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Arrays;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)

public class ActivityDiaryServiceTest {


    @Mock

    private ActivityDiaryRepository diaryRepository;


    @InjectMocks

    private ActivityDiaryService diaryService;


    @Test

    @DisplayName("保存日记-成功")

    void saveDiary_Success() {

        ActivityDiary diary = new ActivityDiary();

        diary.setContent("今天去了西湖");


        when(diaryRepository.save(any())).thenReturn(diary);


        ActivityDiary result = diaryService.saveDiary(diary);

        assertNotNull(result);

        assertEquals("今天去了西湖", result.getContent());

        verify(diaryRepository, times(1)).save(diary);

    }


    @Test

    @DisplayName("按用户ID查日记-返回列表")

    void getDiariesByUserId_ReturnsList() {

        ActivityDiary d1 = new ActivityDiary();

        d1.setContent("diary 1");

        ActivityDiary d2 = new ActivityDiary();

        d2.setContent("diary 2");

        List<ActivityDiary> list = Arrays.asList(d1, d2);


        when(diaryRepository.findByUserId(1L)).thenReturn(list);


        List<ActivityDiary> result = diaryService.getDiariesByUserId(1L);

        assertEquals(2, result.size());

    }


    @Test

    @DisplayName("按用户ID查日记-空列表")

    void getDiariesByUserId_Empty() {

        when(diaryRepository.findByUserId(999L)).thenReturn(Arrays.asList());


        List<ActivityDiary> result = diaryService.getDiariesByUserId(999L);

        assertTrue(result.isEmpty());

    }

}