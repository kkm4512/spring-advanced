package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.user.entity.User;
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
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthService authService;

    // 테스트용 계정
    User user = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void setUp() {
        TestUserInfo testUser1 = TestUserInfo.MEMBER_ONE;
        SignupRequest signupRequest = new SignupRequest(testUser1.getEmail(), testUser1.getPassword(), testUser1.getRole());
        user = new User(signupRequest);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Nested
    class 회원가입_테스트 {
        @Test
        void 회원가입_성공() throws Exception {
            // Given - 회원가입할 유저 정보 준비
            SignupRequest signupRequest = new SignupRequest(user.getEmail(), user.getPassword(), user.getUserRole().toString());
            String jsonReqeust = objectMapper.writeValueAsString(signupRequest);

            // When - 회원가입 시도
            mvc.perform(post("/auth/signup")
                            .content(jsonReqeust)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 회원가입 한번 호출 됐는지 확인
            then(authService).should(times(1)).signup(any());
        }
    }

    @Nested
    class 로그인_테스트 {
        @Test
        void 로그인_성공() throws Exception {
            // Given - 회원가입할 유저 정보 준비
            SigninRequest signinRequest = new SigninRequest(user.getEmail(), user.getPassword());
            String jsonReqeust = objectMapper.writeValueAsString(signinRequest);

            // When - 회원가입 시도
            mvc.perform(post("/auth/signin")
                            .content(jsonReqeust)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 회원가입 한번 호출 됐는지 확인
            then(authService).should(times(1)).signin(any());
        }
    }
}