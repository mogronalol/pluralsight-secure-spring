package pluralsight.m12.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static pluralsight.m12.config.SecurityConfig.PUBLIC_URL_MATCHERS;

@Component
public class RedirectPartialLoginFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            boolean partialLogin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Roles.PARTIAL_LOGIN_PENDING_OTP.getGrantedAuthorityName()));

            if (partialLogin && !isPublicUrl(request.getRequestURI()) && !request.getRequestURI().equals("/login/otp")) {
                response.sendRedirect("/login/otp");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicUrl(String requestURI) {
        return Arrays.stream(PUBLIC_URL_MATCHERS)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }
}
