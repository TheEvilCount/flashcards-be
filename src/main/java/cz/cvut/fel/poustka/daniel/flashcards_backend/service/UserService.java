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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
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
        /*if (user == null)
        {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }*/
        return user;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser()
    {
        return SecurityUtils.getCurrentUser();
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
        user.setRegistrationDate(Date.valueOf(LocalDate.now()));

        user.encodePassword(passwordEncoder);

        userDao.persist(user);
    }

    @Transactional
    public void update(User user) throws BadRequestException, EntityAlreadyExistsException
    {

        try
        {
            Objects.requireNonNull(user);
            checkIfAccountAlreadyExists(user);
            user.encodePassword(passwordEncoder);
            userDao.update(user);
        }
        catch (DataIntegrityViolationException e)
        {
            throw new BadRequestException("Invalid account details");
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
        /*if (getAccountByUsername(user.getUsername()) != null) {
            throw new EntityAlreadyExistsException("User name already taken!");
        } else*/
        if (getUserByEmail(user.getEmail()) != null)
        {
            throw new EntityAlreadyExistsException("Email address " + user.getEmail() + " already taken!");
        }/*
        try
        {
            getUserByEmail(user.getEmail());
            throw new EntityAlreadyExistsException("Email address already taken!");
        }
        catch (UsernameNotFoundException e)
        {
            return;
        }*/

    }

    private void userValidation(User user) throws BadRequestException
    {
        Objects.requireNonNull(user);

        //if(user.getFirstName() == null) throw new BadRequestException("First name cannot be null");
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
        passwordValidation(newPassword);

        User user = getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new ValidationException("Old password is not correct!");
        user.setPassword(passwordEncoder.encode(newPassword));
        userDao.update(user);

        LOG.debug("Changed password for user {}", user.getUsername());
        return user;
    }

    @Transactional
    public User updateUserPreferences(String preferences) throws ValidationException
    {
        preferencesValidation(preferences);

        User user = getCurrentUser();

        user.setPreferences(preferences);
        userDao.update(user);

        LOG.debug("Changed preferences for user {}", user.getUsername());
        return user;
    }

    private void preferencesValidation(String preferences) throws ValidationException
    {
        if (Toolbox.isStringValidJSON(preferences))
        {
            throw new ValidationException("UserPreferences validation exception!");
        }
    }

    private void passwordValidation(String password) throws ValidationException
    {
        if (password.length() <= 5 || password.length() > 100 || password.equals(password.toUpperCase()) || password.equals(password.toLowerCase()))
        {
            throw new ValidationException("Password must be between 5 and 100 characters and must contains at least one lower and one upper case letter!");
        }
    }

    private void emailValidation(String email) throws ValidationException
    {
        //if (!email.matches("/^[^\s@]+@[^\s@]+$/"))
        if (!email.contains("@"))
        {
            throw new ValidationException("Please provide a valid email addrerss!");
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