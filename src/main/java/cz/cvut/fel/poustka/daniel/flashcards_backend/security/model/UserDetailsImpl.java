package cz.cvut.fel.poustka.daniel.flashcards_backend.security.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserDetailsImpl implements UserDetails
{
    private final Set<GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private User user;

    public UserDetailsImpl(User user)
    {
        Objects.requireNonNull(user);
        this.user = user;
        this.authorities = new HashSet<>();
        this.authorities.add(new SimpleGrantedAuthority(this.user.getRole().toString()));
    }

    public User getUser()
    {
        return user;
    }

    public void eraseCredentials()
    {
        this.user.erasePassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return Collections.unmodifiableCollection(authorities);
    }

    @Override
    public String getPassword()
    {
        return user.getPassword();
    }

    @Override
    public String getUsername()
    {
        return user.getUsername();
    }

    public String getEmail()
    {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
