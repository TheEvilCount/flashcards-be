package cz.cvut.fel.poustka.daniel.flashcards_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.LoginStatus;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

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
        addSameSiteCookieAttribute(httpServletResponse);
        LoginStatus loginStatus = new LoginStatus(true, authentication.isAuthenticated(), getUser(authentication), null);

        httpServletResponse.setHeader("Content-Type", "application/json");
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    private void addSameSiteCookieAttribute(HttpServletResponse response)
    {
        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String header : headers)
        { // there can be multiple Set-Cookie attributes
            if (firstHeader)
            {
                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));//Strict
                firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));//Strict
        }
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
        httpServletResponse.setHeader("Content-Type", "application/json");
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
