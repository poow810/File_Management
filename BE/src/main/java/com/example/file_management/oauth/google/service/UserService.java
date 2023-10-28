package com.example.file_management.oauth.google.service;

import com.example.file_management.oauth.google.model.entity.GoogleUser;
import com.example.file_management.oauth.google.repository.GoogleUserRepository;
import com.example.file_management.oauth.model.entity.RefreshToken;
import com.example.file_management.oauth.repository.RefreshTokenRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class UserService {
    private final WebClient webClient;
    private final GoogleUserRepository googleUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(GoogleUserRepository googleUserRepository, RefreshTokenRepository refreshTokenRepository) {
        this.googleUserRepository = googleUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.webClient = WebClient.builder().filter(logRequest()).build();
    }

    public void saveRefreshToken(String email, String refreshToken) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByEmail(email);

        RefreshToken token;
        if (existingToken.isPresent()) {
            // 이미 존재하는 토큰이라면 가져옴
            token = existingToken.get();
            log.info("존재하는 리프레시 토큰입니다. 업데이트 info.");
        } else {
            // 새로운 토큰이라면 새 RefreshToken 객체를 생성
            token = new RefreshToken();
            log.info("새로운 리프레시 토큰 생성");
        }

        // 토큰 정보 설정
        token.setEmail(email);
        token.setRefreshToken(refreshToken);

        // DB에 토큰 정보 저장 (새로운 토큰이면 생성, 기존 토큰이면 업데이트)
        refreshTokenRepository.save(token);

    }

    // 로그 출력용 ExchangeFilterFunction
    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info(name + ":" + value)));
            return next.exchange(clientRequest);
        };
    }

    public GoogleUser getUserInfoAndSave(String accessToken) {
        String userEndpointUrl="https://www.googleapis.com/oauth2/v3/userinfo";

        String userInfoResponse = webClient.get()
                .uri(userEndpointUrl)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {  // 에러 발생 시 로그 출력 및 빈 Mono 반환
                    log.error("WebClient request error: ", e);
                    return Mono.empty();
                })
                .block();

        // 구글 서버에서 받아온 JSON 문자열 출력
        log.info(userInfoResponse);

        try {
            JSONObject jsonObject = new JSONObject(userInfoResponse);
            String email = jsonObject.getString("email");
            Optional<GoogleUser> existingUserOptional = googleUserRepository.findByEmail(email);

            GoogleUser googleUser;
            if (existingUserOptional.isPresent()) {
                // 이미 존재하는 사용자라면 가져옴
                googleUser = existingUserOptional.get();
                log.info("Existing user found. Updating info.");
            } else {
                // 새로운 사용자라면 새 User 객체를 생성
                googleUser = new GoogleUser();
                log.info("Creating new user.");
            }

            // 사용자 정보 설정
            googleUser.setEmail(email);
            String name = jsonObject.getString("name");
            googleUser.setName(name);

            try {
                // DB에 사용자 정보 저장 (새로운 사용자면 생성, 기존 생성자면 업데이트)
                googleUserRepository.save(googleUser);
                log.info("사용자 정보 저장 성공");

                return googleUser;

            } catch (Exception e) {
                log.error("사용자 정보 저장 오류 : ", e);
            }
        } catch (JSONException e) {
            log.error("JSON 파싱 오류 : ", e);
        } catch (Exception e) {
            log.error("기타 오류 : ", e);
        }

        return null;  // 에러 발생 시 null 반환.
    }
}