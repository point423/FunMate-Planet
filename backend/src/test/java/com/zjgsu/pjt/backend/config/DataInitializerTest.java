package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private GeoOperations<String, String> geoOperations;

    private DataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DataInitializer();
        ReflectionTestUtils.setField(initializer, "userRepository", userRepository);
        ReflectionTestUtils.setField(initializer, "stringRedisTemplate", stringRedisTemplate);
    }

    @Test
    void run_CreatesSeedUsersAndRefreshesRedisLocations() {
        AtomicLong ids = new AtomicLong(100L);
        User existingUser = new User();
        existingUser.setId(887L);
        existingUser.setUsername("887");

        when(userRepository.findByUsername("developer_ali")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("frontend_rose")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("ai_enthusiast")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("funmate_bot")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("887")).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(ids.incrementAndGet());
            }
            return user;
        });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.count()).thenReturn(5L);
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);

        initializer.run();

        verify(userRepository, times(4)).saveAndFlush(any(User.class));
        verify(userRepository).save(existingUser);
        verify(geoOperations, atLeast(5)).add(eq("user:location"), any(Point.class), anyString());
    }

    @Test
    void run_SwallowsInitializationException() {
        when(userRepository.findByUsername(anyString())).thenThrow(new RuntimeException("database not ready"));

        assertThatNoException().isThrownBy(() -> withMutedErr(initializer::run));
    }

    private void withMutedErr(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream ignored = new ByteArrayOutputStream();
        PrintStream muted = new PrintStream(ignored);
        try {
            System.setErr(muted);
            action.run();
        } finally {
            System.setErr(original);
            muted.close();
        }
    }
}
