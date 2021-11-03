package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;


import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CollectionCategory;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.util.RestUtils;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CollectionCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/collections")
@PreAuthorize("permitAll()")
public class CardCategoryController
{
    private static final Logger LOG = LoggerFactory.getLogger(CardCategoryController.class);

    private final CollectionCategoryService collectionCategoryService;

    @Autowired
    public CardCategoryController(CollectionCategoryService collectionCategoryService)
    {
        this.collectionCategoryService = collectionCategoryService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionCategory getGenre(@PathVariable int id)
    {
        CollectionCategory result = collectionCategoryService.getById(id);

        if (result == null)
        {
            throw new EntityNotFoundException("CollectionCategory identified by " + id);
        }

        return result;
    }

    //getAllGenres
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CollectionCategory> getAllCollectionCategories()
    {
        List<CollectionCategory> result = collectionCategoryService.getAll();

        if (result == null)
        {
            throw new EntityNotFoundException("No CollectionCategory was found");
        }

        return result;
    }

    //add category
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addNewCollectionCategory(@RequestBody CollectionCategory collectionCategory) throws EntityAlreadyExistsException
    {
        collectionCategoryService.persist(collectionCategory);

        LOG.debug("CollectionCategory {} successfully added.", collectionCategory);
        HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", collectionCategory.getId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    //delete genre
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCollectionCategory(@PathVariable Integer id)
    {
        CollectionCategory toRemove = this.getGenre(id);
        if (toRemove == null)
        {
            throw new EntityNotFoundException("CollectionCategory can't be removed");
        }
        collectionCategoryService.delete(toRemove);
        LOG.debug("Removed CollectionCategory {}.", toRemove);
    }

    /*
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public User changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) throws ValidationException
    {
        return userService.changePassword(oldPassword, newPassword);
    }*/
}
