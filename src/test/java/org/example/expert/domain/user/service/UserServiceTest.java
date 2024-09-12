package org.example.expert.domain.user.service;

import org.example.expert.TestUserInfo;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Spy
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    // 테스트용 계정
    private SignupRequest signupRequest = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void beforEach() {
        TestUserInfo testUser1 = TestUserInfo.MEMBER_ONE;
        signupRequest = new SignupRequest(testUser1.getEmail(), testUser1.getPassword(), testUser1.getRole());
    }

    @Nested
    class 유저_조회_테스트 {
        @Test
        void 유저_조회_성공() {

            // Given - 조회할 유저의 정보 준비
            User expectedUser = new User(signupRequest);
            ReflectionTestUtils.setField(expectedUser, "id", 1L);

            // Given - 유저 조회 상황 준비
            given(userRepository.findById(expectedUser.getId())).willReturn(Optional.of(expectedUser));

            // When - 유저 조회 성공
            UserResponse actualUser = userService.getUser(expectedUser.getId());

            // Then - 서로의 id를 비교하여 일치한지 검증
            assertEquals(
                    expectedUser.getId(),
                    actualUser.getId()
            );
        }

        @Test
        void 유저_조회_실패() {
            // Given - 조회할 유저의 정보 준비
            User user = new User(signupRequest);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "User not found";

            // Given - 유저 조회 실패 상황 준비
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            // WHen - 유저 조회 실패
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    userService.getUser(user.getId())
            );

            // Then - 예외 처리 문구 일치하는지 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }
    }

    @Nested
    class 비밀번호_변경_테스트 {

        @Test
        void 비밀번호_변경_성공() {
            // Given - 비밀번호 변경할 유저 준비
            User user = new User(signupRequest);
            String newPassword = "!@Test1234";
            String oldPassword = user.getPassword();
            user.changePassword(passwordEncoder.encode(user.getPassword()));
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
            ReflectionTestUtils.setField(user, "id", 1L);

            // Given - 유저 조회 성공 상황 준비
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            // Given - 변경하려는 비밀번호와 현재 비밀번호와 달라, 성공 상황 준비
            given(passwordEncoder.matches(newPassword, user.getPassword())).willReturn(false);

            // Given - 현재 입력한 비밀번호와, 실제 비밀번호가 일치, 성공 상황 준비
            given(passwordEncoder.matches(oldPassword, user.getPassword())).willReturn(true);

            // When - 비밀번호 변경 성공
            userService.changePassword(user.getId(), userChangePasswordRequest);

            // Then - 비밀번호 확인 메서드가 2번 호출되었는지 검증
            verify(passwordEncoder, times(2)).matches(anyString(), anyString());
        }

        @Test
        void 비밀번호_변경_실패_계정존재하지않음() {
            // Given - 비밀번호 변경할 유저 준비
            User user = new User(signupRequest);
            String newPassword = "!@Test1234";
            String oldPassword = user.getPassword();
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "User not found";

            // Given - 유저 조회 실패 상황 준비
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            // When - 유저 조회 실패
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    userService.changePassword(user.getId(), userChangePasswordRequest)
            );

            // Then - 유저 조회 실패 문구와 동일한지 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }

        @Test
        void 비밀번호_변경_실패_새비밀번호_기존비밀번호_같음() {
            // Given - 비밀번호 변경할 유저 준비
            User user = new User(signupRequest);
            String newPassword = user.getPassword();
            String oldPassword = user.getPassword();
            user.changePassword(passwordEncoder.encode(user.getPassword()));
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "새 비밀번호는 기존 비밀번호와 같을 수 없습니다.";

            // Given - 유저 조회 성공 준비
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            // Given - 변경하려는 비밀번호와 현재 비밀번호 동일하여, 실패 상황 준비
            given(passwordEncoder.matches(newPassword, user.getPassword())).willReturn(true);

            // When - 비밀번호 변경 실패
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    userService.changePassword(user.getId(), userChangePasswordRequest)
            );

            // Then - 비밀번호 변경 실패시 오류 문구 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );

        }

        @Test
        void 비밀번호_변경_실패_비밀번호_다름() {
            // Given - 비밀번호 변경할 유저 준비
            User user = new User(signupRequest);
            String newPassword = "!@Test1234";
            String oldPassword = user.getPassword();
            user.changePassword(passwordEncoder.encode(user.getPassword()));
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "잘못된 비밀번호입니다.";

            // Given - 유저 조회 성공 준비
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            // Given - 변경하려는 비밀번호와 현재 비밀번호와 달라, 성공 상황 준비
            given(passwordEncoder.matches(newPassword, user.getPassword())).willReturn(false);

            // Given - 현재 입력한 비밀번호와, 실제 비밀번호가 불일치 상황 준비
            given(passwordEncoder.matches(oldPassword, user.getPassword())).willReturn(false);

            // When - 비밀번호 변경 실패
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    userService.changePassword(user.getId(), userChangePasswordRequest)
            );

            // Then - 비밀번호 변경 실패시 오류 문구 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }

        @Test
        void 비밀번호_변경_실패_비밀번호_형식_불일치() {
            // Given - 비밀번호 변경할 유저 준비
            User user = new User(signupRequest);
            String newPassword = "a";
            String oldPassword = user.getPassword();
            user.changePassword(passwordEncoder.encode(user.getPassword()));
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest(oldPassword, newPassword);
            ReflectionTestUtils.setField(user, "id", 1L);
            String expectedExceptionMessage = "새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.";

            // When - 새로운 비밀번호가, 형식에 맞지않는 상황 준비
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    userService.changePassword(user.getId(), userChangePasswordRequest)
            );

            // Then
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }
    }
}