package pluralsight.m13.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class RequestAndContextLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.info("Request {} {} from {}", request.getMethod(), request.getRequestURI(),
                    request.getRemoteAddr());

            String traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);

            String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
            MDC.put("username", username);

            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

            filterChain.doFilter(request, response);

            log.info("Response with status: {}", response.getStatus());

            wrappedResponse.copyBodyToResponse();
        } finally {
            // Clear MDC data to avoid leakage between requests
            MDC.clear();
        }
    }
}