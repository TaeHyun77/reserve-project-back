package com.example.kotlin.jwt

import com.example.kotlin.refresh.RefreshRepository
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException

class CustomLogoutFilter(
    private val jwtUtil: JwtUtil,
    private val refreshRepository: RefreshRepository
) : GenericFilterBean() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        doFilter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    @Throws(IOException::class, ServletException::class)
    private fun doFilter(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val requestUri = request.requestURI

        if (!requestUri.matches(Regex("^/logout$"))) {
            filterChain.doFilter(request, response)
            return
        }

        if (request.method != "POST") {
            filterChain.doFilter(request, response)
            return
        }

        val refresh = request.cookies?.firstOrNull { it.name == "refresh" }?.value

        if (refresh == null) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        try {
            jwtUtil.isExpired(refresh)
        } catch (e: ExpiredJwtException) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        val category = jwtUtil.getCategory(refresh)
        if (category != "refresh") {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        val isExist = refreshRepository.existsByRefresh(refresh)
        if (!isExist) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        // 로그아웃 처리
        refreshRepository.deleteByRefresh(refresh)

        //  refresh 쿠기 무효화
        val cookie = Cookie("refresh", null).apply {
            maxAge = 0
            path = "/"
        }

        response.addCookie(cookie)
        response.status = HttpServletResponse.SC_OK
    }
}