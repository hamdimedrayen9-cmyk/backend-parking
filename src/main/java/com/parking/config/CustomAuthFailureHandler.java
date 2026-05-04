package com.parking.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_SECONDS = 30;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        HttpSession session = request.getSession();

        if (exception instanceof DisabledException) {
            session.setAttribute("error", "Votre compte n'est pas encore activé. Veuillez vérifier vos emails (Mailpit).");
            response.sendRedirect("/login?error=true");
            return;
        }

        Long lockTime = (Long) session.getAttribute("lockTime");
        if (lockTime != null) {
            long elapsed = (System.currentTimeMillis() - lockTime) / 1000;
            if (elapsed < LOCK_DURATION_SECONDS) {
                long remaining = LOCK_DURATION_SECONDS - elapsed;
                session.setAttribute("error", "Compte temporairement bloqué. Réessayez dans " + remaining + " secondes.");
                response.sendRedirect("/login?error=true");
                return;
            } else {
                session.removeAttribute("lockTime");
                session.removeAttribute("failedCount");
                session.removeAttribute("error");
            }
        }

        String email = request.getParameter("username");
        if (email == null || email.isEmpty()) {
            session.setAttribute("error", "Email ou mot de passe incorrect.");
            response.sendRedirect("/login?error=true");
            return;
        }

        Integer failedCount = (Integer) session.getAttribute("failedCount");
        if (failedCount == null) {
            failedCount = 0;
        }
        failedCount++;

        if (failedCount >= MAX_ATTEMPTS) {
            session.setAttribute("failedCount", 0);
            session.setAttribute("lockTime", System.currentTimeMillis());
            session.setAttribute("error", "Trop de tentatives échouées. Veuillez patienter " + LOCK_DURATION_SECONDS + " secondes.");
        } else {
            session.setAttribute("failedCount", failedCount);
            int remaining = MAX_ATTEMPTS - failedCount;
            session.setAttribute("error", "Email ou mot de passe incorrect. Il vous reste " + remaining + " tentative(s).");
        }

        response.sendRedirect("/login?error=true");
    }
}