// myvet-auth/src/main/java/com/myvet/auth/dto/AuthResponse.java
package com.myvet.auth.dto.response;

import com.myvet.dataaccess.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Integer id;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
    }
}