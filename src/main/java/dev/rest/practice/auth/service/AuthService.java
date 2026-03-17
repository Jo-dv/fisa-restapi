package dev.rest.practice.auth.service;

import dev.rest.practice.auth.dto.LoginReqDto;
import dev.rest.practice.auth.dto.SignupReqDto;
import dev.rest.practice.auth.dto.TokenResDto;
import dev.rest.practice.security.JwtTokenProvider;
import dev.rest.practice.user.entity.User;
import dev.rest.practice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupReqDto reqDto) {
        if (userRepository.existsByUsername(reqDto.username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.builder()
                .username(reqDto.username())
                .password(passwordEncoder.encode(reqDto.password()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResDto login(LoginReqDto reqDto) {
        User user = userRepository.findByUsername(reqDto.username())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

        if (!passwordEncoder.matches(reqDto.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
        return new TokenResDto(token);
    }
}