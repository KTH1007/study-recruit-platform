package com.study.platform.global.test

import com.study.platform.global.jwt.JwtProvider
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/test")
@Profile("local")
class TestController(
    private val jwtProvider: JwtProvider
) {

    @GetMapping("/token")
    fun getToken(@RequestParam userId: UUID): String =
        jwtProvider.generateAccessToken(userId)
}
