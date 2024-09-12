package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

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
    class 유저_조회_테스트 {
        @Test
        void 다건_유저_조회_성공() throws Exception {
            // Given - 게시글 조회할 유저 정보 준비
            long userId = 1L;

            // When - 게시글 조회 시도
            mvc.perform(get("/users/{userId}",userId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 게시글 조회 1번 호출되었는지 확인
            then(userService).should(times(1)).getUser(anyLong());
        }
    }
    @Nested
    class 유저_수정_테스트 {
        @Test
        void 유저_비밀번호_수정_성공() throws Exception {
            // Given - 수정할 비밀번호 상황준비
            UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("old", "new");
            String jsonReqeust = objectMapper.writeValueAsString(userChangePasswordRequest);

            // When - 비밀번호 수정 시도
            mvc.perform(put("/users")
                            .content(jsonReqeust)
                            .requestAttr("userId", user.getId())
                            .requestAttr("email", user.getEmail())
                            .requestAttr("userRole", user.getUserRole().name()) // 원하는 UserRole로 설정
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 비밀번호 수정 1번 호출되었는지 확인
            then(userService).should(times(1)).changePassword(anyLong(), any());
        }
    }
}