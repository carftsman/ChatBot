/*
 * package com.dhatvibs.modules.rider.serviceImpl;
 * 
 * 
 * import com.dhatvibs.modules.rider.client.RiderApiClient; import
 * com.dhatvibs.modules.rider.dto.LoginResponse; import
 * com.dhatvibs.modules.rider.service.RiderAuthService; import
 * lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.stereotype.Service; import
 * org.springframework.web.server.ResponseStatusException;
 * 
 * import java.util.Map;
 * 
 * @Slf4j
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class RiderAuthServiceImpl implements
 * RiderAuthService {
 * 
 * private final RiderApiClient riderApiClient;
 * 
 * @Override public String sendOtp(String phone) { Map result =
 * riderApiClient.sendOtp(phone); if (result == null) { throw new
 * ResponseStatusException( HttpStatus.BAD_REQUEST, "Failed to send OTP"); }
 * return (String) result.getOrDefault( "message", "OTP sent successfully"); }
 * 
 * @Override public LoginResponse verifyOtp( String phone, String otp) { Map
 * result = riderApiClient .verifyOtp(phone, otp);
 * 
 * if (result == null) { throw new ResponseStatusException(
 * HttpStatus.UNAUTHORIZED, "Invalid OTP"); }
 * 
 * // Node.js returns access_token String token = (String)
 * result.get("access_token");
 * 
 * if (token == null) { throw new ResponseStatusException(
 * HttpStatus.UNAUTHORIZED, "Login failed. Please try again."); }
 * 
 * log.info("Rider login successful: {}", phone);
 * 
 * return LoginResponse.builder() .token(token) .message("Login successful")
 * .build(); } }
 */  

package com.dhatvibs.modules.rider.serviceImpl;

import com.dhatvibs.modules.rider.client.RiderApiClient;
import com.dhatvibs.modules.rider.dto.LoginResponse;
import com.dhatvibs.modules.rider.service.RiderAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderAuthServiceImpl
        implements RiderAuthService {

    private final RiderApiClient riderApiClient;

    @Override
    public String sendOtp(String phone) {
        Map result = riderApiClient.sendOtp(phone);
        log.info("sendOtp response: {}", result);
        if (result == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed to send OTP");
        }
        return result.getOrDefault(
            "message", "OTP sent successfully")
            .toString();
    }

    @Override
    public LoginResponse verifyOtp(
            String phone, String otp) {

        Map result = riderApiClient
            .verifyOtp(phone, otp);

        // ── Log full response to see field names ──
        log.info("verifyOtp full response: {}", result);

        if (result == null) {
            log.error("verifyOtp returned null");
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid OTP");
        }

        // ── Try all possible token field names ────
        // Node.js apps use different field names
        String token = null;

        if (result.get("access_token") != null) {
            token = result.get("access_token")
                          .toString();
            log.info("Found token in: access_token");

        } else if (result.get("accessToken") != null) {
            token = result.get("accessToken")
                          .toString();
            log.info("Found token in: accessToken");

        } else if (result.get("token") != null) {
            token = result.get("token").toString();
            log.info("Found token in: token");

        } else if (result.get("data") != null) {
            // Sometimes token is nested in data object
            Object data = result.get("data");
            if (data instanceof Map) {
                Map dataMap = (Map) data;
                if (dataMap.get("access_token")
                        != null) {
                    token = dataMap
                        .get("access_token")
                        .toString();
                    log.info("Found token in: "
                           + "data.access_token");
                } else if (dataMap.get("accessToken")
                        != null) {
                    token = dataMap
                        .get("accessToken")
                        .toString();
                    log.info("Found token in: "
                           + "data.accessToken");
                } else if (dataMap.get("token")
                        != null) {
                    token = dataMap.get("token")
                                   .toString();
                    log.info("Found token in: "
                           + "data.token");
                }
            }
        }

        if (token == null) {
            log.error(
                "No token found in response. "
              + "Keys: {}", result.keySet());
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Login failed — token not found "
              + "in response: " + result.keySet());
        }

        log.info("Rider login success for: {}",
                 phone);

        return LoginResponse.builder()
            .token(token)
            .message("Login successful")
            .build();
    }
}