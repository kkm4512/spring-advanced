package org.example.expert.domain.auth.service;

import org.example.expert.TestUserInfo;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository userRepository;

    @Spy
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthService authService;

    // 테스트용 계정
    private SignupRequest signupRequest = null;
    private SigninRequest signinRequest = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void beforEach() {
        TestUserInfo testUser1 = TestUserInfo.MEMBER_ONE;
        signupRequest = new SignupRequest(testUser1.getEmail(), testUser1.getPassword(), testUser1.getRole());
        signinRequest = new SigninRequest(testUser1.getEmail(), testUser1.getPassword());
    }

    @Nested
    class 유저_회원가입_테스트 {

        @Test
        void 유저_회원가입_성공() {
            // Given - 전체적인 회원가입할 사용자의 정보 준비
            User user = new User(signupRequest);
            ReflectionTestUtils.setField(user, "id", 1L);

            // Given - 이메일이 중복 되지 않을때
            given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(user);

            // When - 중복되지 않은 이메일로 회원가입
            SignupResponse signupResponse = authService.signup(signupRequest);

            // Then - 정상적으로 회원가입 되었을때, 토큰 확인
            String expectedBearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
            String actualBearerToken = signupResponse.getBearerToken();
            assertEquals(
                    expectedBearerToken,
                    actualBearerToken
            );
        }

        @Test
        void 유저_회원가입_실패_이메일중복() {
            // Given - 전체적인 회원가입할 사용자의 정보 준비
            User user = new User(signupRequest);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "이미 존재하는 이메일입니다.";

            // Given - 이메일이 중복 됐을때
            given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

            // When - 중복된 이메일로 회원가입 시도
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));

            // Then - 이메일 중복 예외문구 나오는지 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }
    }

    @Nested
    class 유저_로그인_테스트 {

        @Test
        void 유저_로그인_성공() {
            // Given - 전체적인 로그인할 유저의 정보 준비
            User user = new User(signinRequest.getEmail(), signinRequest.getPassword(), UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedBearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

            // Given - 계정도 존재하고, 비밀번호도 일치하는 상황 준비
            given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);

            // When - 로그인에 성공하여, 토큰을 받아옴
            String actualBearerToken = authService.signin(signinRequest).getBearerToken();

            // Then - 토큰값이 일치하는지 확인
            assertEquals(
                    expectedBearerToken,
                    actualBearerToken
            );
        }

        @Test
        void 유저_로그인_실패_계정존재하지않음() {
            // Given - 전체적인 로그인할 유저의 정보 준비
            User user = new User(signinRequest.getEmail(), signinRequest.getPassword(), UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "가입되지 않은 유저입니다.";

            // Given - 존재하지 않는 계정으로 로그인 시도 상황 준비
            given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

            // When - 계정이 존재하지않아, 예외 처리 발생
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class,
                    () -> authService.signin(signinRequest));

            // Then - 예외처리시 문구가 동일한지 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }

        @Test
        void 유저_로그인_실패_비밀번호_불일치() {
            // Given - 전체적인 로그인할 유저의 정보 준비
            User user = new User(signinRequest.getEmail(), signinRequest.getPassword(), UserRole.USER);
            String expectedExceptionMessage = "잘못된 비밀번호입니다.";

            // Given - 계정은 존재하는데, 비밀번호가 일치하지 않는 상황 준비
            given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(false);

            // When - 로그인시, 비밀번호가 일치하지않으 예외 처리 발생
            AuthException actualExceptionMessage = assertThrows(AuthException.class,
                    () -> authService.signin(signinRequest));

            // Then - 예외처리시 문구가 동일한지 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }
    }
}