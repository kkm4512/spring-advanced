package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAdminController.class)
class UserAdminControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    UserAdminService userAdminService;

    @Autowired
    ObjectMapper objectMapper;

    // 테스트용 계정
    User user = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void setUp() {
        TestUserInfo userInfo1 = TestUserInfo.MEMBER_ONE;
        SignupRequest signupRequest = new SignupRequest(userInfo1.getEmail(), userInfo1.getPassword(), userInfo1.getRole());
        user = new User(signupRequest);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Nested
    class 권한_변경_관리자_테스트 {
        @Test
        void 권한_변경_성공() throws Exception {
            // Given - 변경할 권한 설정
            UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest(UserRole.USER.name());
            String jsonReqeust = objectMapper.writeValueAsString(userRoleChangeRequest);
            long userId = 1L;

            // When - 권한 변경 시도
            mvc.perform(patch("/admin/users/{userId}",userId)
                            .content(jsonReqeust)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 권한변경 1번 호출되었는지 확인
            then(userAdminService).should(times(1)).changeUserRole(anyLong(), any());
        }
    }
}