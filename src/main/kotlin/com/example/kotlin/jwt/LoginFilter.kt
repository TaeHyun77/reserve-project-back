package com.example.kotlin.jwt

import com.example.kotlin.util.createCookie
import com.example.kotlin.refresh.Refresh
import com.example.kotlin.refresh.RefreshRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

private val log = KotlinLogging.logger {}

class LoginFilter(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val refreshRepository: RefreshRepository
): UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val username = request.getParameter("username")
        val password = request.getParameter("password")

        log.info { "username: $username" }
        log.info { "password : $password" }

        // 인증 객체 생성
        val authToken = UsernamePasswordAuthenticationToken(username, password, null)

        // Security가 이 토큰을 UserDetailsService 등과 연계해서 검증 진행
        return authenticationManager.authenticate(authToken)
    }

    override fun successfulAuthentication(request: HttpServletRequest,
                                          response: HttpServletResponse,
                                          chain: FilterChain,
                                          authentication: Authentication) {

        log.info { "Login Success" }

        val userDetails: CustomUserDetails = authentication.principal as CustomUserDetails

        val username = userDetails.username
        val name = userDetails.getName()
        val email = userDetails.getEmail()
        val role = authentication.authorities.first().authority

        val accessToken = jwtUtil.createToken(username, name, email, role,"access", 30 * 60 * 1000)
        val refresh = jwtUtil.createToken(username, name, email, role,"refresh", 60 * 60 * 1000)

        log.info { "accessToken $accessToken" }
        log.info { "refresh $refresh" }

        createRefresh(username, refresh, 60 * 60 * 1000)

        response.setHeader("access", accessToken)
        response.addCookie(createCookie("refresh", refresh))
        response.status = HttpStatus.OK.value()
    }

    fun createRefresh(username: String, refresh: String, expired: Long) {
        val refresh = Refresh(username = username, refresh = refresh, expiration = expired)

        refreshRepository.save(refresh)
    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        failed: AuthenticationException
    ) {
        log.error(failed) { "로그인 실패: ${failed.message}" }
        response.status = HttpServletResponse.SC_UNAUTHORIZED
    }
}