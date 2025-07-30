package com.example.kotlin.jwt

import com.example.kotlin.member.MemberRepository
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger { }

class JwtFilter(
    private val jwtUtil: JwtUtil,
    private val memberRepository: MemberRepository
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val accessToken = request.getHeader("access")
            ?: run {
            filterChain.doFilter(request, response)
            return
        }

        try {
            if (jwtUtil.isExpired(accessToken)) {
                throw ReserveException(HttpStatus.UNAUTHORIZED, ErrorCode.ACCESSTOKEN_ISEXPIRED)
            }
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_TOKEN)
        }

        val category = jwtUtil.getCategory(accessToken)
        if (category != "access") {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.IS_NOT_ACCESSTOKEN)
        }

        val username: String = jwtUtil.getUsername(accessToken)

        val member = memberRepository.findByUsername(username)
            ?: run {
                log.warn { "회원 정보를 찾을 수 없습니다. username: $username" }
                throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_MEMBER_INFO)
            }

        val customUserDetails = CustomUserDetails(member)

        val authToken = UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.authorities
        )

        SecurityContextHolder.getContext().authentication = authToken

        filterChain.doFilter(request, response)
    }
}