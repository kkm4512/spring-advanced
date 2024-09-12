package org.example.expert.domain.manager.service;

import org.example.expert.TestUserInfo;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private ManagerService managerService;

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
    public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {

        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
        // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);

        long todoId = 1L;

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void 일정작성자는_본인을_등록할수없음() {
        // Given - 일정 작성자, 담당자, 일정 생성 준비
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
        User managerUser = user;
        ReflectionTestUtils.setField(todo, "id", 1L);
        String expectedExceptionMessage = "일정 작성자는 본인을 담당자로 등록할 수 없습니다.";

        // Given - 담당자 생성
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUser.getId());

        // Given - 일정 조회 성공 상황 준비
        given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));


        // Given - 등록하려는 담당자 조회 성공 상황 준비
        given(userRepository.findById(managerUser.getId())).willReturn(Optional.of(managerUser));

        // When - 매니저 저장 시도
        InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todo.getId(), managerSaveRequest)
        );

        // Then - 예외 처리 문구 확인
        assertEquals(
                expectedExceptionMessage,
                actualExceptionMessage.getMessage()
        );
    }

    @Nested
    class 다건_담당자_조회_테스트 {
        @Test
        void 다건_담당자_조회_성공() {

            // Given - 여러개 존재할, 담당자 리스트 생성 준비
            Manager manager = new Manager(user, todo);
            List<Manager> expectedManagers = List.of(manager);
            ReflectionTestUtils.setField(todo, "id", 1L);

            // Given 일정 조회 성공 상황 준비
            given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));

            // Given - 여러명의 담당자 조회 성공 상황 준비
            given(managerRepository.findByTodoIdWithUser(todo.getId())).willReturn(expectedManagers);

            // When - 담당자 조회 시도
            List<ManagerResponse> actualManagers = managerService.getManagers(todo.getId());

            // Then - 담당자 ID 일치 확인
            assertEquals(
                    expectedManagers.get(0).getId(),
                    actualManagers.get(0).getId()
            );
        }

        @Test
        void 다건_담당자_조회_실패_일정존재하지않음() {
            // Given - 예상하는 예외 문구 준비
            String expectedExceptionMessage = "Todo not found";

            // Given - 여러개 존재할, 담당자 리스트 생성 준비
            Manager manager = new Manager(user, todo);
            ReflectionTestUtils.setField(todo, "id", 1L);

            // Given 일정 조회 성공 실패 준비
            given(todoRepository.findById(todo.getId())).willReturn(Optional.empty());

            // When - 담당자 조회 시도
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    managerService.getManagers(todo.getId())
            );


            // Then - 담당자 ID 일치 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }
    }

    @Nested
    class 담당자_제거_테스트 {
        @Test
        void 담당자_제거_성공() {
            AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
            Manager manager = new Manager(user, todo);
            ReflectionTestUtils.setField(todo, "id", 1L);
            ReflectionTestUtils.setField(manager, "id", 1L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));
            given(managerRepository.findById(manager.getId())).willReturn(Optional.of(manager));

            // When
            managerService.deleteManager(authUser, todo.getId(), manager.getId());

            // Then
            then(managerRepository).should().delete(manager);
        }

        @Test
        void 담당자_제거_실패_유저존재하지않음() {
            // Given - 로그인 유저 없는 상항 준비
            String expectedExceptionMessage = "User not found";
            AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
            Manager manager = new Manager(user, todo);
            ReflectionTestUtils.setField(todo, "id", 1L);
            ReflectionTestUtils.setField(manager, "id", 1L);
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            // When - 담당자 제거 시도
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(authUser, todo.getId(), manager.getId())
            );

            // Then - 예외 문구 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }

        @Test
        void 담당자_제거_실패_일정존재하지않음() {
            // Given - 일정 없는 상항 준비
            String expectedExceptionMessage = "Todo not found";
            AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
            Manager manager = new Manager(user, todo);
            ReflectionTestUtils.setField(todo, "id", 1L);
            ReflectionTestUtils.setField(manager, "id", 1L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(todoRepository.findById(todo.getId())).willReturn(Optional.empty());

            // When - 담당자 제거 시도
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(authUser, todo.getId(), manager.getId())
            );

            // Then - 예외 문구 확인
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }

        @Test
        void 담당자_제거_실패_담당자존재하지않음() {
            // Given - 담당자 없는 상항 준비
            String expectedExceptionMessage = "Manager not found";
            AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getUserRole());
            Manager manager = new Manager(user, todo);
            ReflectionTestUtils.setField(todo, "id", 1L);
            ReflectionTestUtils.setField(manager, "id", 1L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));
            given(managerRepository.findById(manager.getId())).willReturn(Optional.empty());

            // When - 담당자 제거 시도
            InvalidRequestException actualExceptionMessage = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(authUser, todo.getId(), manager.getId())
            );

            // Then - 예외문구 비교
            assertEquals(
                    expectedExceptionMessage,
                    actualExceptionMessage.getMessage()
            );
        }

    }
}