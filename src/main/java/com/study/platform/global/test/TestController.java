package com.study.platform.global.test;

import com.study.platform.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Profile("local")
public class TestController {

    private final JwtProvider jwtProvider;

    @GetMapping("/token")
    public String getToken(@RequestParam UUID userId) {
        return jwtProvider.generateAccessToken(userId);
    }
}
