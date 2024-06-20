package pluralsight.m12.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FailedLoginAttemptHandler extends SimpleUrlAuthenticationFailureHandler {

    public FailedLoginAttemptHandler(final MessageSource messageSource) {
        super("/login?error");  // Set the default failure URL in the constructor
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final AuthenticationException exception)
            throws IOException, ServletException {

        if (exception instanceof CompromisedPasswordException) {
            request.getSession().setAttribute("compromisedPassword", true);
            getRedirectStrategy().sendRedirect(request, response, "/reset-password");
        } else {
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
