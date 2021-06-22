package cz.cvut.fel.poustka.daniel.flashcards_backend.service.security;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.UserDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private final UserDao userDao;

    @Autowired
    public UserDetailsServiceImpl(UserDao userDao)
    {
        this.userDao = userDao;
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String email) throws UsernameNotFoundException
    {
        final User user = userDao.findByEmail(email);
        if (user == null)
        {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }
        return new UserDetailsImpl(user);
    }
}
