package com.project.demo.api.config;

import com.project.demo.logic.entity.auth.JwtAuthenticationFilter;
import com.project.demo.logic.entity.auth.JwtService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Clase base que provee los @MockBean necesarios para inicializar el contexto
 * de Spring Security en pruebas @WebMvcTest.
 * <p>
 * El filtro JWT y la configuración OAuth2 requieren beans que no son parte del
 * slice de @WebMvcTest. Extender esta clase evita duplicar las declaraciones
 * en cada clase de prueba.
 */
public abstract class SecurityMockBeans {

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    protected UserDetailsService userDetailsService;

    @MockBean
    protected ClientRegistrationRepository clientRegistrationRepository;
}
