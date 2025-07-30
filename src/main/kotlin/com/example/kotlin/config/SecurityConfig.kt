package com.example.kotlin.config

import com.example.kotlin.jwt.CustomLogoutFilter
import com.example.kotlin.jwt.JwtFilter
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.jwt.LoginFilter
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.refresh.RefreshRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import kotlin.jvm.java

@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val authenticationConfiguration: AuthenticationConfiguration,
    private val jwtUtil: JwtUtil,
    private val refreshRepository: RefreshRepository,
    private val memberRepository: MemberRepository
) {

    @Bean
    fun authenticationManager(): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/", "/member/**", "/venue/**", "/performance/**", "/screenInfo/**", "/seat/**", "/reToken", "/reserve/**"
                    ).permitAll()
                    .requestMatchers("/admin").hasRole("ADMIN")
                    .requestMatchers("/login", "/logout").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtFilter(jwtUtil, memberRepository), LoginFilter::class.java)
            .addFilterAt(LoginFilter(authenticationManager(), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(CustomLogoutFilter(jwtUtil, refreshRepository), JwtFilter::class.java)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080")
        configuration.allowedMethods = listOf("*")
        configuration.allowCredentials = true
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("Authorization")
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}