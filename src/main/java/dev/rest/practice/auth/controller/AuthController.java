package dev.rest.practice.auth.controller;

import dev.rest.practice.auth.dto.LoginReqDto;
import dev.rest.practice.auth.dto.SignupReqDto;
import dev.rest.practice.auth.dto.TokenResDto;
import dev.rest.practice.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupReqDto reqDto) {
        authService.signup(reqDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResDto> login(@Valid @RequestBody LoginReqDto reqDto) {
        TokenResDto token = authService.login(reqDto);
        return ResponseEntity.ok(token);
    }
}