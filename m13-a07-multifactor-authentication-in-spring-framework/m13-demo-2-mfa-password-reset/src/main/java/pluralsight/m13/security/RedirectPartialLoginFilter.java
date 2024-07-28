package pluralsight.m13.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static pluralsight.m13.security.OtpRedirectAuthenticationSuccessHandler.ROLE_PARTIAL_LOGIN_OTP;

@Component
public class RedirectPartialLoginFilter extends OncePerRequestFilter {

    public static final AntPathRequestMatcher
            ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/images/**");

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            boolean partialLogin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals(ROLE_PARTIAL_LOGIN_OTP));

            if (partialLogin && !request.getRequestURI().equals("/login/otp") &&
                !isPublic(request)) {
                response.sendRedirect("/login/otp");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPublic(final HttpServletRequest r) {
        return ANT_PATH_REQUEST_MATCHER.matches(r);
    }
}
