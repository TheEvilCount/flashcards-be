package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import cz.cvut.fel.poustka.daniel.flashcards_backend.config.OtherConstants;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.PasswordResetToken;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.VerificationToken;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto.RegisterDTO;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.CurrentUser;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.EmailService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.PasswordResetTokenService;
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

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@PreAuthorize("permitAll()")
public class UserController
{
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, VerificationTokenService verificationTokenService, PasswordResetTokenService passwordResetTokenService, EmailService emailService)
    {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
    }

    //getAllUsers
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getAllUsers()
    {
        List<User> result = userService.getAll();

        if (result == null)
        {
            throw new EntityNotFoundException("No User was found");
        }

        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getCurrentUser(@CurrentUser UserDetailsImpl userDetailsImpl)
    {
        return userDetailsImpl.getUser();
    }

    /**
     * Registers a new user.
     * @param userReq RegisterDTO data
     */
    @PreAuthorize("anonymous")//@PreAuthorize("(!#user.isAdmin() && anonymous) || hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerAccount(@RequestBody RegisterDTO userReq) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        LOG.debug("registering...{}", userReq.getUsername());
        User user = new User();
        user.setPassword(userReq.getPassword());
        user.setUsername(userReq.getUsername());
        user.setEmail(userReq.getEmail());

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
                verificationTokenService.update(user.getVerificationToken());
                LOG.debug("Verification token updated for user {}.", user);
            }
            else
            {
                VerificationToken vToken = new VerificationToken(user);
                verificationTokenService.persist(vToken);
                sendVerificationEmail(user, vToken.getToken());
                LOG.debug("Verification token created for user {}.", user);
            }
            LOG.debug("Verification token resent for user {}.", user);
        }
        else
        {
            throw new BadRequestException("User account with this email does not exists or its already activated!");
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
        if (!Objects.equals(token, ""))
        {
            final VerificationToken verificationToken = verificationTokenService.getByToken(token);
            if (verificationToken != null && !verificationToken.isTokenExpired())
            {
                User user = userService.getUserByEmail(verificationToken.getUser().getEmail());
                if (user.getIsActivated())
                    throw new ValidationException("User account " + user.getEmail() + " is already verified");
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

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/changepass")
    @ResponseStatus(HttpStatus.OK)
    public User changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) throws ValidationException
    {
        return userService.changePassword(oldPassword, newPassword);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/updateprefs", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public User updatePreferences(@RequestBody String preferences) throws ValidationException
    {
        User currentUser = userService.getCurrentUser();
        return userService.updateUserPreferences(currentUser, preferences);
    }

    @PreAuthorize("(anonymous)")
    @PostMapping(value = "/lostpass")
    @ResponseStatus(HttpStatus.OK)
    public void lostPassword(@RequestParam String email) throws BadRequestException, ValidationException, EntityAlreadyExistsException
    {
        User user = userService.getUserByEmail(email);
        if (user != null && user.getIsActivated())
        {
            if (user.getPasswordResetToken() != null) //check if already exist
            {// if yes -> reset expire date
                user.getPasswordResetToken().setCreatedDate(new Date()); //reset creation date for expire check
                sendResetEmail(user, user.getPasswordResetToken().getToken());
                passwordResetTokenService.update(user.getPasswordResetToken());
                LOG.debug("Verification token updated for user {}.", user);
            }
            else
            {// if not -> create new
                PasswordResetToken rToken = new PasswordResetToken(user);
                passwordResetTokenService.persist(rToken);
                sendResetEmail(user, rToken.getToken());
                LOG.debug("Reset token created for user {}.", user);
            }
            LOG.debug("Reset token sent for user {}.", user);
        }
        else
        {
            throw new BadRequestException("User account with this email does not exists or it is not activated!");
        }
    }

    /**
     * Activates newly registered account.
     * @param token String verification token
     */
    @PreAuthorize("(anonymous)")
    @PostMapping(value = "/resetpass")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword) throws ValidationException
    {
        if (!Objects.equals(token, ""))
        {
            final PasswordResetToken passwordResetToken = passwordResetTokenService.getByToken(token);
            if (passwordResetToken != null && !passwordResetToken.isTokenExpired())
            {
                User user = userService.getUserByEmail(passwordResetToken.getUser().getEmail());
                if (!user.getIsActivated())
                {
                    throw new ValidationException("User account " + user.getEmail() + " is not verified");
                }
                else
                {
                    userService.resetPassword(newPassword, user);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
            else
                throw new ValidationException("Token {" + token + "} is invalid or expired reset token!");
        }
        else
            throw new ValidationException("Token cannot be empty");
    }


    private void sendVerificationEmail(User user, String token) throws MailSendException
    {
        try
        {
            emailService.sendMail(user.getEmail(),
                                  "Registration",
                                  "To activate your account please click here: "
                                          + OtherConstants.FLASHCARDS_FE_URL + OtherConstants.FLASHCARDS_FE_PATHS_VERIFY +
                                          "?token=" + token);
        }
        catch (MailException e)
        {
            throw new MailSendException("Email sending error!");
        }
    }

    private void sendResetEmail(User user, String token) throws MailSendException
    {
        try
        {
            emailService.sendMail(user.getEmail(),
                                  "Password reset",
                                  "To reset your password please click here: "
                                          + OtherConstants.FLASHCARDS_FE_URL + OtherConstants.FLASHCARDS_FE_PATHS_RESET +
                                          "?token=" + token);
        }
        catch (MailException e)
        {
            throw new MailSendException("Email sending error!");
        }
    }


}