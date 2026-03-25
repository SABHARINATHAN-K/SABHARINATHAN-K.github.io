package com.careerplanning.backend.modules.auth.service;

import com.careerplanning.backend.common.exception.AccessDeniedException;
import com.careerplanning.backend.modules.auth.dto.AuthResponse;
import com.careerplanning.backend.modules.auth.dto.GoogleSignInRequest;
import com.careerplanning.backend.modules.auth.dto.RegisterRequest;
import com.careerplanning.backend.modules.career.service.CareerTrackCatalogService;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpleTokenService simpleTokenService;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private CareerTrackCatalogService careerTrackCatalogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, simpleTokenService, googleTokenVerifierService, careerTrackCatalogService);
    }

    @Test
    void googleSignInCreatesNewUserWhenEmailDoesNotExist() {
        GoogleTokenVerifierService.VerifiedGoogleUser verifiedGoogleUser =
                new GoogleTokenVerifierService.VerifiedGoogleUser(
                        "new.user@example.com",
                        "New User",
                        "google-sub-1"
                );

        when(googleTokenVerifierService.verifyIdToken("valid-id-token")).thenReturn(verifiedGoogleUser);
        when(userRepository.findByEmail("new.user@example.com")).thenReturn(Optional.empty());
        when(careerTrackCatalogService.defaultCareerTrack()).thenReturn(CareerTrack.FULL_STACK_DEVELOPER.name());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setId(user, 101L);
            return user;
        });
        when(simpleTokenService.createToken(101L)).thenReturn("token-101");

        AuthResponse response = authService.googleSignIn(new GoogleSignInRequest("valid-id-token"));

        assertThat(response.userId()).isEqualTo(101L);
        assertThat(response.token()).isEqualTo("token-101");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("new.user@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("New User");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.STUDENT.name());
        assertThat(savedUser.getCareerTrack()).isEqualTo(CareerTrack.FULL_STACK_DEVELOPER.name());
        assertThat(savedUser.isOnboardingCompleted()).isFalse();
        assertThat(savedUser.getPassword()).isNotBlank();
    }

    @Test
    void googleSignInUsesExistingUserWhenEmailExists() {
        User existingUser = new User();
        setId(existingUser, 44L);
        existingUser.setEmail("existing.user@example.com");
        existingUser.setFullName("Existing User");
        existingUser.setPassword("hashed-password");
        existingUser.setRole(UserRole.PROFESSIONAL.name());
        existingUser.setCareerTrack(CareerTrack.DATA_SCIENCE.name());
        existingUser.setOnboardingCompleted(true);

        GoogleTokenVerifierService.VerifiedGoogleUser verifiedGoogleUser =
                new GoogleTokenVerifierService.VerifiedGoogleUser(
                        "existing.user@example.com",
                        "Existing User",
                        "google-sub-2"
                );

        when(googleTokenVerifierService.verifyIdToken("valid-id-token")).thenReturn(verifiedGoogleUser);
        when(userRepository.findByEmail("existing.user@example.com")).thenReturn(Optional.of(existingUser));
        when(simpleTokenService.createToken(44L)).thenReturn("token-44");

        AuthResponse response = authService.googleSignIn(new GoogleSignInRequest("valid-id-token"));

        assertThat(response.userId()).isEqualTo(44L);
        assertThat(response.token()).isEqualTo("token-44");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerAllowsBootstrappingFirstAdmin() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.existsByRole(UserRole.ADMIN.name())).thenReturn(false);
        when(careerTrackCatalogService.defaultCareerTrack()).thenReturn(CareerTrack.FULL_STACK_DEVELOPER.name());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setId(user, 55L);
            return user;
        });
        when(simpleTokenService.createToken(55L)).thenReturn("admin-token");

        AuthResponse response = authService.register(new RegisterRequest(
                "Admin User",
                "admin@example.com",
                "Password123",
                UserRole.ADMIN.name(),
                null
        ));

        assertThat(response.userId()).isEqualTo(55L);
        assertThat(response.token()).isEqualTo("admin-token");
    }

    @Test
    void registerRejectsAdminSelfAssignmentAfterBootstrap() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.existsByRole(UserRole.ADMIN.name())).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> authService.register(new RegisterRequest(
                "Admin User",
                "admin@example.com",
                "Password123",
                UserRole.ADMIN.name(),
                null
        )));
    }

    private static void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set user id in test", e);
        }
    }
}
