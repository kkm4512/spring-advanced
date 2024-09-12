package org.example.expert.domain.user.service;

import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserAdminService userAdminService;

    // 테스트용 계정
    private SignupRequest signupRequest = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void beforEach() {
        TestUserInfo testUser1 = TestUserInfo.MEMBER_ONE;
        signupRequest = new SignupRequest(testUser1.getEmail(), testUser1.getPassword(), testUser1.getRole());
    }

    @Nested
    class 유저_권한_변경_테스트 {
        @Test
        void 유저_권한_변경_성공(){
            // Given - 권한 변경할 유저 생성
            User user = new User(signupRequest);
            User spyUser = Mockito.spy(user);
            ReflectionTestUtils.setField(spyUser,"id",1L);

            // Given - 변경할 권한 생성
            UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest(UserRole.USER.name());

            // Given - 유저 조회 성공 상황 준비
            given(userRepository.findById(spyUser.getId())).willReturn(Optional.of(spyUser));

            // When - 유저 권한 변경 시도
            userAdminService.changeUserRole(spyUser.getId(), userRoleChangeRequest);

            // Then - 유저 권한 변경 함수가 1번 호출되었는지 확인
            verify(spyUser).updateRole(UserRole.of(userRoleChangeRequest.getRole()));
        }

        @Test
        void 유저_권한_변경_실패_유저존재하지않음(){
            // Given - 권한 변경할 유저 생성
            User user = new User(signupRequest);
            ReflectionTestUtils.setField(user,"id",1L);
            String expectedExceptionMessage = "User not found";

            // Given - 변경할 권한 생성
            UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest(UserRole.USER.name());

            // Given - 유저 조회 실패 상황 준비
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            // When - 유저 조회 실패
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    userAdminService.changeUserRole(user.getId(), userRoleChangeRequest)
            );

            // Then - 유저 조회 실패 문구가, 동일한지 확인
            assertEquals(expectedExceptionMessage, actualExceptionMessage.getMessage());
        }
    }

}