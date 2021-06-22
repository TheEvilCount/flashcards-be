package cz.cvut.fel.poustka.daniel.flashcards_backend.security.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;

public class AuthenticationToken extends AbstractAuthenticationToken implements Principal
{
    private UserDetailsImpl userDetailsImpl;

    public AuthenticationToken(Collection<? extends GrantedAuthority> authorities, UserDetailsImpl userDetailsImpl)
    {
        super(authorities);
        this.userDetailsImpl = userDetailsImpl;
        super.setAuthenticated(true);
        super.setDetails(userDetailsImpl);
    }

    @Override
    public String getCredentials()
    {
        return userDetailsImpl.getPassword();
    }

    @Override
    public UserDetailsImpl getPrincipal()
    {
        return userDetailsImpl;
    }
}
