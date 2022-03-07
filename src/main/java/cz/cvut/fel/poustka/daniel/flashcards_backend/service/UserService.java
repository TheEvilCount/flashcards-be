package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.UserDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Role;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.SecurityUtils;
import cz.cvut.fel.poustka.daniel.flashcards_backend.util.Toolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class UserService
{
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDao userDao, PasswordEncoder passwordEncoder)
    {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByEmail(String email) throws UsernameNotFoundException
    {
        final User user = userDao.findByEmail(email);
        return user;
    }

    public User getUserByUsername(String username) throws UsernameNotFoundException
    {
        final User user = userDao.findByUsername(username);
        return user;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser()
    {
        return SecurityUtils.getCurrentUser();
    }

    @Transactional(readOnly = true)
    public List<User> getAll()
    {
        return this.userDao.findAll();
    }

    @Transactional
    public void persist(User user) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        userValidation(user);
        checkIfAccountAlreadyExists(user);

        if (user.getRole() == null)
        {
            user.setRole(Role.USER);
        }
        passwordValidation(user.getPassword());
        emailValidation(user.getEmail());
        usernameValidation(user.getUsername());
        preferencesValidation(user.getPreferences());
        user.setRegistrationDate(new Date());

        user.encodePassword(passwordEncoder);

        userDao.persist(user);
    }

    @Transactional
    public void update(User user) throws BadRequestException
    {
        try
        {
            Objects.requireNonNull(user);
            userDao.update(user);
        }
        catch (NullPointerException e)
        {
            throw new BadRequestException("Updated user cannot be null");
        }
        catch (Exception e)
        {
            LOG.error("User update exception: " + e);
        }
    }

    @Transactional
    public void setAdmin(User user)
    {
        user.setRole(Role.ADMIN);
        userDao.update(user);
    }

    @Transactional
    public void becomeBasicUser(User user)
    {
        user.setRole(Role.USER);
        userDao.update(user);
    }

    private void checkIfAccountAlreadyExists(User user) throws EntityAlreadyExistsException
    {
        if (getUserByUsername(user.getUsername()) != null)
        {
            throw new EntityAlreadyExistsException("User name %s already taken!".formatted(user.getUsername()));
        }
        else if (getUserByEmail(user.getEmail()) != null)
        {
            throw new EntityAlreadyExistsException("Email address %s already taken!".formatted(user.getEmail()));
        }
    }

    private void userValidation(User user) throws BadRequestException
    {
        Objects.requireNonNull(user);

        if (user.getEmail() == null)
            throw new BadRequestException("Email cannot be null");
        if (user.getUsername() == null)
            throw new BadRequestException("Username cannot be null");
        if (user.getPassword() == null)
            throw new BadRequestException("Password cannot be null");
    }

    @Transactional
    public User changePassword(String oldPassword, String newPassword) throws ValidationException
    {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new ValidationException("Old password is not correct!");
        else
        {
            passwordValidation(newPassword);

            user.setPassword(passwordEncoder.encode(newPassword));
            userDao.update(user);

            LOG.debug("Changed password for user {}", user.getUsername());
            return user;
        }
    }

    @Transactional
    public User resetPassword(String newPassword, User user) throws ValidationException
    {
        passwordValidation(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userDao.update(user);

        LOG.debug("Changed (with reset) password for user {}", user.getUsername());
        return user;
    }

    @Transactional
    public User updateUserPreferences(User user, String preferences) throws ValidationException
    {
        preferencesValidation(preferences);

        user.setPreferences(preferences);
        userDao.update(user);

        LOG.debug("Changed preferences for user {}", user.getUsername());
        return user;
    }

    private void preferencesValidation(String preferences) throws ValidationException
    {
        if (!Toolbox.isStringValidJSON(preferences))
        {
            throw new ValidationException("UserPreferences validation exception!");
        }
    }

    private void passwordValidation(String password) throws ValidationException
    {
        if (password.length() < 8 || password.length() > 100 || password.equals(password.toUpperCase()) || password.equals(password.toLowerCase()))
        {
            throw new ValidationException("Password must be between 8 and 100 characters and must contains at least one lower and one upper case letter!");
        }
    }

    private void emailValidation(String email) throws ValidationException
    {
        //if (!email.matches("/^[^\s@]+@[^\s@]+$/"))
        if (!email.contains("@"))
        {
            throw new ValidationException("Please provide a valid email address!");
        }
    }

    private void usernameValidation(String username) throws ValidationException
    {
        if (username.length() < 3 || username.length() > 20)
        {
            throw new ValidationException("Username must be between 3 and 20 characters!");
        }
    }
}
