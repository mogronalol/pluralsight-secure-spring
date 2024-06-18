package pluralsight.m12.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.List;

public class PartialAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
            IOException {

        final Authentication updatedAuth =
                new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(), List.of(new SimpleGrantedAuthority(
                                "ROLE_REQUIRES_OTP")));

        SecurityContextHolder.getContext().setAuthentication(updatedAuth);

        response.sendRedirect("/otp");
    }
}