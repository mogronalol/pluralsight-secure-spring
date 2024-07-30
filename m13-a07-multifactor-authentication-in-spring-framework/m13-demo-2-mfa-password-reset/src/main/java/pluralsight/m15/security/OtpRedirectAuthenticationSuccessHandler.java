package pluralsight.m15.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class OtpRedirectAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler {

    public static final String ROLE_PARTIAL_LOGIN_OTP = "ROLE_PARTIAL_LOGIN_OTP";

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
                                                  final HttpServletResponse response,
                                                  final Authentication authentication)
            throws IOException, ServletException {
        getRedirectStrategy().sendRedirect(request, response, "/login/otp");
        final UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(),
                        List.of(new SimpleGrantedAuthority(ROLE_PARTIAL_LOGIN_OTP)));
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
