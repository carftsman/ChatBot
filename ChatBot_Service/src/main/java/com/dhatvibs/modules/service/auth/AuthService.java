package com.dhatvibs.modules.service.auth;


import com.dhatvibs.modules.dto.auth.*;

public interface AuthService {

    LoginResponse  login(LoginRequest request);

    LogoutResponse logout(String token);
}
