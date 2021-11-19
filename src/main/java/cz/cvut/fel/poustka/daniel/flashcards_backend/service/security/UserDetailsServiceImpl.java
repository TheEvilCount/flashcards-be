package cz.cvut.fel.poustka.daniel.flashcards_backend.service.security;

import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.AccountNotActivatedException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String email) throws UsernameNotFoundException
    {
        User user = userService.getUserByEmail(email);
        if (user != null)
            if (user.getIsActivated())
                return new UserDetailsImpl(user);
            else
                throw new AccountNotActivatedException("User account with email " + email + " is not activated.");
        else
            throw new UsernameNotFoundException("User account with email " + email + " not found.");
    }
}
