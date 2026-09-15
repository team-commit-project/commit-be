package com.receiptmate.auth.filter;

import com.receiptmate.auth.provider.JwtProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = getAccessToken(request);

        // Access Token이 없는 요청은 그대로 다음 필터로 전달
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userId = jwtProvider.validateAccessToken(accessToken);

            // todo: 실제 존재하는 회원인지 확인

            setContext(userId, request);
        } catch (JwtException | IllegalArgumentException e) {
            // JWT 만료, 위조, 형식 오류 등이 경우 인증 정보를 등록하지 않음
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // Authorization Header에서 Access Token 추출
    private String getAccessToken(HttpServletRequest request) {

        // Request 객체에서 Authorization header 값 추출
        String authorization = request.getHeader("Authorization");
        boolean hasAuthorization = StringUtils.hasText(authorization);
        if (!hasAuthorization) return null;

        // Bearer 인증 방식인지 확인
        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        // Authorization 필드 값에서 Token 추출
        return authorization.substring(7);
    }

    // 인증된 사용자 정보를 SecurityContext에 등록
    private void setContext(String userId, HttpServletRequest request) {
        // 접근 주체의 정보가 담길 인증 토큰 생성
        AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.NO_AUTHORITIES);

        // 생성한 인증 토큰이 어떤 요청의 정보인지 상세 내역 추가
        authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // 빈 Security Context 생성
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        // 생성한 Security Context에 접근 주체 정보 주입
        securityContext.setAuthentication(authenticationToken);

        // 생성한 Security Context 등록
        SecurityContextHolder.setContext(securityContext);
    }
}
