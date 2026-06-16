package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.DiscoverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoverControllerUnitTest {

    @Mock
    private DiscoverService discoverService;

    private DiscoverController controller;

    @BeforeEach
    void setUp() {
        controller = new DiscoverController();
        ReflectionTestUtils.setField(controller, "discoverService", discoverService);
    }

    @Test
    void getNearby_ReturnsUsersFromService() {
        User user = new User();
        user.setId(1L);
        when(discoverService.getNearbyUsers(120.0, 30.0, 5.0)).thenReturn(List.of(user));

        Result<List<User>> result = controller.getNearby(120.0, 30.0, 5.0);

        assertThat(result.getData()).containsExactly(user);
    }

    @Test
    void getRanking_ReturnsRankingFromService() {
        User user = new User();
        when(discoverService.getRanking(3)).thenReturn(List.of(user));

        Result<List<User>> result = controller.getRanking(3);

        assertThat(result.getData()).containsExactly(user);
    }

    @Test
    void getRandomUser_Returns404WhenNoUserExists() {
        when(discoverService.getRandomUser()).thenReturn(null);

        Result<User> result = controller.getRandomUser();

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void updateLocation_UsesCurrentUserId() {
        Result<String> result = controller.updateLocation(
                Map.of("longitude", 120.0, "latitude", 30.0),
                8L
        );

        assertThat(result.getCode()).isEqualTo(0);
        verify(discoverService).updateUserLocation(8L, 120.0, 30.0);
    }

    @Test
    void deleteLocation_ReturnsSuccessOrNotFound() {
        when(discoverService.deleteUserLocation(8L)).thenReturn(true, false);

        assertThat(controller.deleteLocation(8L).getCode()).isEqualTo(0);
        assertThat(controller.deleteLocation(8L).getCode()).isEqualTo(404);
    }
}
