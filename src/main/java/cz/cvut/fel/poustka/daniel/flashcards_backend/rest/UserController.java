package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.CurrentUser;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@PreAuthorize("permitAll()")
public class UserController
{
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService)
    {
        this.userService = userService;
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

        LOG.debug("User {} successfully registered.", user);
        //final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/current", user);
        //final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/login");
        //return new ResponseEntity<>(headers, HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.CREATED);
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