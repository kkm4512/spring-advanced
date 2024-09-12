package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.service.CommentService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    CommentService commentService;

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
    class 댓글_저장_테스트 {
        @Test
        void 댓글_저장_성공() throws Exception {
            // Given - 저장할 댓글 준비
            CommentSaveRequest commentSaveRequest = new CommentSaveRequest("contents");
            String jsonReqeust = objectMapper.writeValueAsString(commentSaveRequest);
            long todoId = 1L;

            // When - 댓글 저장 시도
            mvc.perform(post("/todos/{todoId}/comments",todoId)
                            .content(jsonReqeust)
                            .requestAttr("userId", user.getId())
                            .requestAttr("email", user.getEmail())
                            .requestAttr("userRole", user.getUserRole().name()) // 원하는 UserRole로 설정
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 댓글 저장 1번 호출되었는지 확인
            then(commentService).should(times(1)).saveComment(any(),anyLong(),any());
        }
    }
    @Nested
    class 댓글_조회_테스트 {
        @Test
        void 다건_댓글_조회_성공() throws Exception {
            // Given - 조회할 댓글 준비
            long todoId = 1L;

            // When - 댓글 조회 시도
            mvc.perform(get("/todos/{todoId}/comments", todoId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 댓글 시도 1번 호출되었는지 확인
            then(commentService).should(times(1)).getComments(anyLong());
        }
    }
}