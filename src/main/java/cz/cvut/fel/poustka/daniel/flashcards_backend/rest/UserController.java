package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.VerificationToken;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.CurrentUser;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.EmailService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/users")
@PreAuthorize("permitAll()")
public class UserController
{
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, VerificationTokenService verificationTokenService, EmailService emailService)
    {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrentUser(@CurrentUser UserDetailsImpl userDetailsImpl)
    {
        return userDetailsImpl.getUser();
    }

    /**
     * Registers a new user.
     * @param user User data
     */
    @PreAuthorize("(!#user.isAdmin() && anonymous) || hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerAccount(@RequestBody User user) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        LOG.debug("registering...{}", user.getUsername());
        userService.persist(user);

        VerificationToken vToken = new VerificationToken(user);
        verificationTokenService.persist(vToken);

        sendVerificationEmail(user, vToken.getToken());

        LOG.debug("User {} successfully registered.", user);
        //final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/current", user);
        //final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/login");
        //return new ResponseEntity<>(headers, HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("(anonymous)")
    @PostMapping(value = "/resend")
    @ResponseStatus(HttpStatus.OK)
    public void resendVerificationToken(@RequestParam String email) throws BadRequestException, ValidationException, EntityAlreadyExistsException
    {
        User user = userService.getUserByEmail(email);
        if (user != null && !user.getIsActivated())
        {
            if (user.getVerificationToken() != null)
            {
                user.getVerificationToken().setCreatedDate(new Date()); //reset creation date for expire check
                sendVerificationEmail(user, user.getVerificationToken().getToken());
            }
            else
            {
                VerificationToken vToken = new VerificationToken(user);
                verificationTokenService.persist(vToken);
                sendVerificationEmail(user, vToken.getToken());
            }
            LOG.debug("Verification token resended for user {}.", user);
        }
        else
        {
            throw new BadRequestException("User account with this email does not exists or its already activated!");
        }
    }

    private void sendVerificationEmail(User user, String token) throws MailSendException
    {
        try
        {
            //TODO send an email with activation url (with token)
            emailService.sendMail(user.getEmail(), "Registration", "To activate your account please click here: " + "http://localhost:8081/flashcards/api/v1/users/verify?token=" + token);
        }
        catch (MailException e)
        {
            throw new MailSendException("Email sending error!");
        }
    }

    /**
     * Activates newly registered account.
     * @param token String verification token
     */
    @PreAuthorize("(anonymous)")
    @PostMapping(value = "/verify")
    public ResponseEntity<Void> verifyAccount(@RequestParam String token) throws ValidationException, BadRequestException
    {
        if (token != "")
        {
            final VerificationToken verificationToken = verificationTokenService.getByToken(token);
            if (verificationToken != null && !verificationToken.isTokenExpired())
            {
                User user = userService.getUserByEmail(verificationToken.getUser().getEmail());
                user.setIsActivated(true);
                userService.update(user);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else
                throw new ValidationException("Token {" + token + "} is invalid or expired verification token!");
        }
        else
            throw new ValidationException("Token cannot be empty");
    }

    @PutMapping("/changepass")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public User changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) throws ValidationException
    {
        return userService.changePassword(oldPassword, newPassword);
    }

    @PutMapping("/updateprefs")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public User updatePreferences(@RequestParam String preferences) throws ValidationException
    {
        return userService.updateUserPreferences(preferences);
    }

}