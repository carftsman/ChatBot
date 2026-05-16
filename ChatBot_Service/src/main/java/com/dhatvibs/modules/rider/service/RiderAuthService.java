package com.dhatvibs.modules.rider.service;


import com.dhatvibs.modules.rider.dto.*;

public interface RiderAuthService {
    String       sendOtp(String phone);
    LoginResponse verifyOtp(String phone, String otp);
}
