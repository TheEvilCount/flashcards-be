package cz.cvut.fel.poustka.daniel.flashcards_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.poustka.daniel.flashcards_backend.config.SecurityConstants;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.LoginStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sends JSON back to client on authentication failure.
 */
@Service
public class CredentialsAuthenticationFailureHandler implements AuthenticationFailureHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsAuthenticationFailureHandler.class);

    private final ObjectMapper mapper;

    @Autowired
    public CredentialsAuthenticationFailureHandler(@Qualifier("objectMapper") ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        AuthenticationException e) throws IOException
    {
        LOG.debug("Login failed for user {}.", httpServletRequest.getParameter(SecurityConstants.EMAIL_PARAM));
        LoginStatus status = new LoginStatus(false, false, null, e.getMessage());
        mapper.writeValue(httpServletResponse.getOutputStream(), status);
    }
}
