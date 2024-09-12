package org.example.expert.domain.comment.service;

import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentAdminService commentAdminService;

    // 테스트용 계정
    User user = null;
    Todo todo = null;

    // 각각의 테스트는 독립적으로 시행되야 하므로, BeforeEach 사용
    @BeforeEach
    void setUp() {
        TestUserInfo testUser1 = TestUserInfo.MEMBER_ONE;
        SignupRequest signupRequest = new SignupRequest(testUser1.getEmail(), testUser1.getPassword(), testUser1.getRole());
        user = new User(signupRequest);
        ReflectionTestUtils.setField(user, "id", 1L);
        todo = new Todo(
                "title",
                "contents",
                "weahter",
                user
        );
        ReflectionTestUtils.setField(todo, "id", 1L);
    }

    @Nested
    class 관리자_댓글_테스트 {
        @Test
        void 관리자_댓글_삭제_성공() {
            // Given - 삭제할 댓글 준비
            long commentId = 1L;

            // When - 댓글 삭제 시도
            doNothing().when(commentRepository).deleteById(commentId);
            commentAdminService.deleteComment(commentId);

            // Then - 댓글 삭제 호출 1번인지 확인
            then(commentRepository).should().deleteById(commentId);
        }
    }

}