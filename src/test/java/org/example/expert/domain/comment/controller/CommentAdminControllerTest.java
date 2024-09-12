package org.example.expert.domain.comment.controller;

import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.comment.service.CommentAdminService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentAdminController.class)
class CommentAdminControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    CommentAdminService commentAdminService;

    // 테스트용 계정
    User user = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void setUp() {
        TestUserInfo testAdminInfo = TestUserInfo.ADMIN_ONE;
        SignupRequest signupRequest = new SignupRequest(testAdminInfo.getEmail(), testAdminInfo.getPassword(), testAdminInfo.getRole());
        user = new User(signupRequest);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Nested
    class 관리자_댓글_테스트 {
        @Test
        void 관리자_댓글_삭제_성공() throws Exception {
            // Given - 삭제닿 댓글 번호 준비
            long commnetId = 1L;

            // When - 댓글삭제 시도
            mvc.perform(delete("/admin/comments/{commnetId}",commnetId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - 댓글삭제 로직 한번 실행되었는지 확인
            then(commentAdminService).should(times(1)).deleteComment(commnetId);
        }
    }

}