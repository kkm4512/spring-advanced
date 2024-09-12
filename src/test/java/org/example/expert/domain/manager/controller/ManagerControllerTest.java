package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.entity.User;
import org.hibernate.event.spi.SaveOrUpdateEvent;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ManagerController.class)
class ManagerControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ManagerService managerService;

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
    class 담당자_저장_테스트 {
        @Test
        void 담당자_저장_성공() throws Exception {
            // Given - 저장할 담당자 상황 준비
            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(1L);
            String jsonReqeust = objectMapper.writeValueAsString(managerSaveRequest);
            long todoId = 1L;

            // When - 담당자 저장 시도
            mvc.perform(post("/todos/{todoId}/managers",todoId)
                            .content(jsonReqeust)
                            .accept(MediaType.APPLICATION_JSON)
                            .requestAttr("userId", user.getId())
                            .requestAttr("email", user.getEmail())
                            .requestAttr("userRole", user.getUserRole().name()) // 원하는 UserRole로 설정
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 회원가입 한번 호출 됐는지 확인
            then(managerService).should(times(1)).saveManager(any(),anyLong(),any());
        }
    }

    @Nested
    class 담당자_조회_테스트 {
        @Test
        void 담당자_조회_성공() throws Exception {
            // Given - 조회할 일정 생성 준비
            long todoId = 1L;

            // When - 담당자 조회 시도
            mvc.perform(get("/todos/{todoId}/managers",todoId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 회원가입 한번 호출 됐는지 확인
            then(managerService).should(times(1)).getManagers(anyLong());
        }
    }

    @Nested
    class 담당자_삭제_테스트 {
        @Test
        void 담당자_삭제_성공() throws Exception {
            // Given - 삭제할 일정, 담당자 지정
            long todoId = 1L;
            long managerId = 1L;

            // When - 담당자 저장 시도
            mvc.perform(delete("/todos/{todoId}/managers/{managerId}",todoId,managerId)
                            .accept(MediaType.APPLICATION_JSON)
                            .requestAttr("userId", user.getId())
                            .requestAttr("email", user.getEmail())
                            .requestAttr("userRole", user.getUserRole().name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 회원가입 한번 호출 됐는지 확인
            then(managerService).should(times(1)).deleteManager(any(),anyLong(),anyLong());
        }
    }
}