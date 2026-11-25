package org.example.sogone.security.jwt;

import java.io.IOException;
import java.util.List;

import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.user.entity.type.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	private final static String HEADER_AUTHORIZATION = "Authorization";
	private final static String TOKEN_PREFIX = "Bearer ";
	private final static String AUTH_ERROR = "authError";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

		String token = getAccessToken(authorizationHeader);

		if (token != null) {
			try {
				Claims claims = jwtTokenProvider.parseAccessToken(token);
				
				String roleName = claims.get("role", String.class);
				if (roleName == null) {
					throw new BusinessException(ErrorCode.UNAUTHORIZED);
				}

				Role role = Role.valueOf(roleName);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					Long.valueOf(claims.getSubject()), null,
					List.of(new SimpleGrantedAuthority(role.getAuthority()))
				);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ExpiredJwtException e) {
				request.setAttribute(AUTH_ERROR, ErrorCode.TOKEN_EXPIRED);
			} catch (JwtException | IllegalArgumentException e) {
				request.setAttribute(AUTH_ERROR, ErrorCode.INVALID_TOKEN);
			}
		}

		filterChain.doFilter(request, response);
	}

	private String getAccessToken(String authorization) {
		if (authorization != null && authorization.startsWith(TOKEN_PREFIX)) {
			return authorization.substring(TOKEN_PREFIX.length());
		}

		return null;
	}
}
