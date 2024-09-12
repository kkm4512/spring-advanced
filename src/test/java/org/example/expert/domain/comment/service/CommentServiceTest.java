package org.example.expert.domain.comment.service;

import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

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

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(2L, "email", UserRole.USER);
        ReflectionTestUtils.setField(todo, "id", todoId);
        ReflectionTestUtils.setField(todo, "user", user);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("User not authorized", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    void 다건_댓글_조회_성공() {
        // Given - 조회에 성공할 댓글들 상황 준비
        Comment comment = new Comment("contetns",user,todo);
        List<Comment> expectedComments = List.of(comment);
        given(commentRepository.findByTodoIdWithUser(todo.getId())).willReturn(expectedComments);

        // When - 댓글들 조회 시도
        List<CommentResponse> actualComments = commentService.getComments(user.getId());

        // Then - 서로의 ID가 일치하는지 확인
        assertEquals(
                expectedComments.get(0).getId(),
                actualComments.get(0).getId()
        );
    }
}