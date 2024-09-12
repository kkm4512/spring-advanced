package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.service.TodoService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoController.class)
class TodoControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    TodoService todoService;

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
    class 게시글_저장_테스트 {
        @Test
        void 게시글_저장_성공() throws Exception {
            // Given - 저장할 게시글 준비
            TodoSaveRequest todoSaveRequest = new TodoSaveRequest("test", "contents");
            String jsonReqeust = objectMapper.writeValueAsString(todoSaveRequest);

            // When - 게시글 저장 시도
            mvc.perform(post("/todos")
                            .content(jsonReqeust)
                            .requestAttr("userId", user.getId())
                            .requestAttr("email", user.getEmail())
                            .requestAttr("userRole", user.getUserRole().name()) // 원하는 UserRole로 설정
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 게시글 저장 1번 호출되었는지 확인
            then(todoService).should(times(1)).saveTodo(any(), any());
        }
    }

    @Nested
    class 게시글_조회_테스트 {
        @Test
        void 다건_게시글_조회_성공() throws Exception {
            // Given - 조회할 일정 페이지,갯수 정하기
            String page = "1";
            String size = "10";

            // When - 게시글 저장 시도
            mvc.perform(get("/todos")
                            .param("page", page)
                            .param("size", size)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 게시글 저장 1번 호출되었는지 확인

            then(todoService).should(times(1)).getTodos(anyInt(), anyInt());
        }

        @Test
        void 단건_게시글_조회_성공() throws Exception {
            // Given - 조회할 게시글 준비
            long todoId = 1L;

            // When - 게시글 저장 시도
            mvc.perform(get("/todos/{todoId}", todoId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 게시글 저장 1번 호출되었는지 확인

            then(todoService).should(times(1)).getTodo(anyLong());
        }
    }
}
