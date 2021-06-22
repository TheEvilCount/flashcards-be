package cz.cvut.fel.poustka.daniel.flashcards_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.LoginStatus;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sends JSON back to client on authentication success.
 */
@Service
public class CredentialsAuthenticationSuccessHandler implements AuthenticationSuccessHandler, LogoutSuccessHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsAuthenticationSuccessHandler.class);

    private final ObjectMapper mapper;

    @Autowired
    public CredentialsAuthenticationSuccessHandler(@Qualifier("objectMapper") ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    // On login
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException
    {
        String username = getEmail(authentication);
        if (LOG.isTraceEnabled())
        {
            LOG.trace("Successfully authenticated user {}", username);
        }
        LoginStatus loginStatus = new LoginStatus(true, authentication.isAuthenticated(), getUser(authentication), null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    // On logout
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException
    {
        if (LOG.isTraceEnabled())
        {
            LOG.trace("Successfully logged out user {}", getEmail(authentication));
        }
        LoginStatus loginStatus = new LoginStatus(false, true, null, null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    // Helper Methods
    private String getEmail(Authentication authentication)
    {
        return authentication != null ? ((UserDetailsImpl) authentication.getPrincipal()).getEmail() : "";
    }

    private User getUser(Authentication authentication)
    {
        return authentication != null ? ((UserDetailsImpl) authentication.getPrincipal()).getUser() : null;
    }
}