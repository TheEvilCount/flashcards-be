package cz.cvut.fel.poustka.daniel.flashcards_backend.util;


import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Role;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.LocalDate;

@Component
public class SystemInitializer
{

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    private final UserService userService;

    private final PlatformTransactionManager txManager;

    @Autowired
    public SystemInitializer(UserService userService, PlatformTransactionManager txManager)
    {
        this.txManager = txManager;
        this.userService = userService;
    }

    @PostConstruct
    private void initSystem()
    {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.execute((status) -> {
            System.out.println("_________INIT___________");
            generateAdmin1();
            return null;
        });
    }

    /**
     * Generates an admin account if it does not already exist.
     */
    private void generateAdmin1()
    {
        User admin = new User();
        admin.setEmail("*****");
        admin.setPassword("*****");
        admin.setUsername("Admin");
        admin.setRegistrationDate(Date.valueOf(LocalDate.now()));
        admin.setRole(Role.ADMIN);
        admin.setIsActivated(true);

        try
        {
            userService.persist(admin);
            LOG.info("Generated admin user with credentials " + admin.getUsername() + "/" + admin.getEmail() + "/" + admin.getPassword());
        }
        catch (EntityAlreadyExistsException ignored)
        {
        }
        catch (BadRequestException | ValidationException e)
        {
            LOG.warn("Admin Generation error: " + e.getMessage());
        }
    }

}