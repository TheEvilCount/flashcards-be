package cz.cvut.fel.poustka.daniel.flashcards_backend.util;


import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CollectionCategory;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Role;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CollectionCategoryService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class SystemInitializer
{
    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    private final UserService userService;
    private final CollectionCategoryService categoryService;

    @Autowired
    public SystemInitializer(UserService userService, CollectionCategoryService categoryService)
    {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @PostConstruct
    private void init()
    {
        LOG.info("_________INIT___________");
        generateAdmin1();
        generateDefaultCategories();
        LOG.info("_________INIT-DONE___________");
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
            LOG.info("Default admin alerady exists");
        }
        catch (BadRequestException | ValidationException e)
        {
            LOG.warn("Admin Generation error: " + e.getMessage());
        }
        catch (Exception e)
        {
            LOG.error("Initializer admin exception: " + e.getMessage());
        }
    }

    private void generateDefaultCategories()
    {
        List<String> list = List.of("Language", "Medical", "Computers", "Education", "History", "Other", "Fun", "Exam", "Military", "Law");
        CollectionCategory cat = categoryService.getByTitle(list.get(0));
        if (cat != null)
        {
            LOG.info("Generate categories SKIP");
        }
        else
        {
            try
            {
                for (String el : list)
                {
                    categoryService.persist(new CollectionCategory(el));
                }
                LOG.info("Generated default categories");
            }
            catch (EntityAlreadyExistsException ignored)
            {
                LOG.info("Default categories already exists");
            }
            catch (Exception e)
            {
                LOG.error("Initializer categories exception: " + e.getMessage());
            }
        }
    }
}