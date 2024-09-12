package org.example.expert.domain.todo.service;

import org.example.expert.TestUserInfo;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    TodoRepository todoRepository;

    @Mock
    WeatherClient weatherClient;

    @InjectMocks
    TodoService todoService;

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
    class 일정_저장_테스트 {
        @Test
        void 일정_저장_성공() {
            // Given - 로그인한 사용자의 정보 준비
            AuthUser authUser = new AuthUser(user);

            // Given - 날씨 정보 조회 성공 준비
            given(weatherClient.getTodayWeather()).willReturn("오늘의 날씨");

            // Given - 생성될 게시글 정보 준비
            TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contetns");
            Todo expectedTodo = new Todo();
            ReflectionTestUtils.setField(expectedTodo, "id", 1L);
            given(todoRepository.save(any())).willReturn(expectedTodo);

            // When - 게시글 저장 시도
            TodoSaveResponse actualTodo = todoService.saveTodo(authUser, todoSaveRequest);

            // Then - 처음 지정된 ID와, 반환받은 ID의 값을 비교
            assertEquals(
                    expectedTodo.getId(),
                    actualTodo.getId()
            );
        }
    }

    @Nested
    class 일정_조회_테스트 {
        @Test
        void 다건_일정_조회_성공() {
            // Given - 조회할 페이지 정보 및 페이징 설정 준비
            int page = 1;
            int size = 10;
            Pageable pageable = PageRequest.of(page - 1, size);

            // Given - 일정 생성 준비
            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo, "id", 1L);

            // Given - 일정 찾을때, 우리가 정한 일정이 나오게 준비
            Page<Todo> expectedTodos = new PageImpl<>(Arrays.asList(todo), pageable, 1);
            given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(expectedTodos);

            // When - 일정 조회 시도
            Page<TodoResponse> actualTodos = todoService.getTodos(page, size);

            // Then - 서로 일정의 ID가 동일한지 확인
            assertEquals(
                    expectedTodos.getContent().get(0).getId(),
                    actualTodos.getContent().get(0).getId()
            );
        }

        @Test
        void 단건_일정_조회_성공() {
            // Given - 조회할 게시글 준비
            Todo expectedTodo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(expectedTodo, "id", 1L);

            // Given - 게시글 조회 실패 성공 준비
            given(todoRepository.findByIdWithUser(expectedTodo.getId())).willReturn(Optional.of(expectedTodo));

            // When - 게시글 조회
            TodoResponse actualTodo = todoService.getTodo(expectedTodo.getId());

            // Then - 서로의 ID가 동일한지 확인
            assertEquals(
                    expectedTodo.getId(),
                    actualTodo.getId()
            );
        }

        @Test
        void 단건_일정_조회_실패_일정못찾음() {
            // Given - 조회할 게시글 준비
            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo, "id", 1L);
            String expectedExceptionMessage = "Todo not found";

            // Given - 게시글 조회 실패 상황 준비
            given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.empty());

            // When - 게시글 조회
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    todoService.getTodo(todo.getId())
            );

            // Then - 예외문구가 일치하는지 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }
    }
}