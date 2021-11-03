package cz.cvut.fel.poustka.daniel.flashcards_backend.security;

import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.AuthenticationToken;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CredentialsAuthenticationProvider implements AuthenticationProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsAuthenticationProvider.class);

    private final UserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CredentialsAuthenticationProvider(@Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService, PasswordEncoder passwordEncoder)
    {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        final String username = authentication.getPrincipal().toString();
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Authenticating user {}", username);
        }
        final UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
        final String password = (String) authentication.getCredentials();

        //TODO passwords are in plain text :( no encoder is needed
        if (!passwordEncoder.matches(password, userDetailsImpl.getPassword()))
        //if (!password.matches(userDetailsImpl.getPassword()))
        {
            throw new BadCredentialsException("Provided credentials don't match.");
        }

        userDetailsImpl.eraseCredentials();
        return SecurityUtils.setCurrentUser(userDetailsImpl);
    }

    @Override
    public boolean supports(Class<?> aClass)
    {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass) ||
                AuthenticationToken.class.isAssignableFrom(aClass);
    }
}
