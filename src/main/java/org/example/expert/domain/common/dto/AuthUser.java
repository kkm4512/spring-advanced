package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    public AuthUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.userRole = user.getUserRole();
    }

}
