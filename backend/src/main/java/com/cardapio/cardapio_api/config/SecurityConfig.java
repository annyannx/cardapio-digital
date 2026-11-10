package com.cardapio.cardapio_api.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Regras de segurança da API (RF005).
 *
 * Públicas (sem login):
 *  - consulta do cardápio (GET /api/cardapio/**);
 *  - imagens guardadas na pasta local (GET /uploads/**);
 *  - envio de pedidos pelo cliente (POST /api/pedidos);
 *  - acompanhamento de um pedido pelo número (GET /api/pedidos/**);
 *  - login (POST /api/auth/login);
 *  - documentação Swagger.
 *
 * Somente administrador (token JWT com perfil ADMIN):
 *  - tudo em /api/admin/**.
 *
 * Qualquer outra rota exige um token JWT válido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                // API "sem sessão": a cada requisição, o token JWT prova quem é o usuário.
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/cardapio", "/api/cardapio/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        // Sem token ou token inválido/expirado -> 401
                        .authenticationEntryPoint((request, response, ex) ->
                                escreverErro(response, 401,
                                        "Autenticação necessária: token ausente, inválido ou expirado"))
                        // Token válido, mas sem permissão para a rota -> 403
                        .accessDeniedHandler((request, response, ex) ->
                                escreverErro(response, 403,
                                        "Você não tem permissão para acessar este recurso")));

        return http.build();
    }

    /** Codifica senhas com BCrypt (o banco guarda só o hash). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS: define quais sites (origens) podem chamar a API pelo navegador.
     * Por padrão libera o servidor de desenvolvimento do frontend (porta 5173 e 3000).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origens}") List<String> origens) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origens);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    /** Escreve um erro em JSON no mesmo formato do restante da API (ErroResponse). */
    private static void escreverErro(HttpServletResponse response, int status, String mensagem)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":" + status + ",\"mensagem\":\"" + mensagem + "\",\"detalhes\":[]}");
    }
}
