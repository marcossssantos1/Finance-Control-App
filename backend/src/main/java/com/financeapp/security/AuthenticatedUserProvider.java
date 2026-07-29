package com.financeapp.security;

import com.financeapp.domain.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Ponto único para obter o usuário autenticado a partir do SecurityContext.
 * Todo service que precisa saber "quem está logado" deve usar esta classe,
 * em vez de confiar em qualquer usuarioId vindo do corpo da requisição.
 */
@Component
public class AuthenticatedUserProvider {

    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto de segurança");
        }

        return userDetails.getUsuario();
    }

    public Long getUsuarioIdAutenticado() {
        return getUsuarioAutenticado().getId();
    }
}
