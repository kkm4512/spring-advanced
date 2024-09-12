package org.example.expert;

import org.example.expert.domain.user.enums.UserRole;

public enum TestUserInfo {
    MEMBER_ONE("test1@naver.com", "!@Skdud340", UserRole.USER.name()),
    MEMBER_TWO("test2@naver.com", "!@Skdud340", UserRole.USER.name()),
    MEMBER_THREE("test3@naver.com", "!@Skdud340", UserRole.USER.name()),
    MEMBER_FOUR("test4@naver.com", "!@Skdud340", UserRole.USER.name()),
    MEMBER_FIVE("test5@naver.com", "!@Skdud340", UserRole.USER.name()),
    ADMIN_ONE("admin@naver.com", "!@Skdud340", UserRole.ADMIN.name());

    private final String email;
    private final String password;
    private final String role;

    TestUserInfo(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
