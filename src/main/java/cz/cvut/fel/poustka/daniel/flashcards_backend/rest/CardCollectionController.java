package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Card;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.util.RestUtils;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CardCollectionService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CardService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
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
public class CardCollectionController
{
    private static final Logger LOG = LoggerFactory.getLogger(CardCategoryController.class);

    private final UserService userService;
    private final CardCollectionService cardCollectionService;
    private final CardService cardService;

    @Autowired
    public CardCollectionController(CardCollectionService cardCollectionService, UserService userService, CardService cardService)
    {
        this.cardCollectionService = cardCollectionService;
        this.userService = userService;
        this.cardService = cardService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    public List<CardCollection> getPublicCollections(@RequestParam Integer type)
    {
        User currentUser = userService.getCurrentUser();

        CardCollectionVisibility requestedType;
        //Check if requested type of collections exists

        requestedType = CardCollectionVisibility.getByValue(type);
        LOG.debug("get collections - type: " + requestedType);

        switch (requestedType)
        {
            default:
            case PUBLIC:
                return cardCollectionService.getAllPublic();
            case PRIVATE:
                return cardCollectionService.getAllPrivate(currentUser);
            case LINKED:
                return cardCollectionService.getAllFavourite(currentUser);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCardCollection(@RequestBody CardCollection cardCollection) throws EntityAlreadyExistsException, ValidationException, BadRequestException
    {
        User currentUser = userService.getCurrentUser();

        cardCollectionService.persist(cardCollection, currentUser);

        LOG.debug("CollectionCategory {} successfully added.", cardCollection);
        HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", cardCollection.getId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    //delete
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCollectionCategory(@PathVariable Long id)
    {
        CardCollection toRemove = cardCollectionService.getById(id);
        if (toRemove == null)
        {
            throw new EntityNotFoundException("CollectionCategory can't be removed!");
        }

        User currentUser = userService.getCurrentUser();

        if (toRemove.getOwner().equals(currentUser))
        {
            cardCollectionService.delete(toRemove);
            LOG.debug("Removed CollectionCategory {}.", toRemove);
        }
        else
        {
            throw new SecurityException("User is not an owner of this collection!");
        }
    }

    //TODO getbyid,name...
    //TODO update delete, make public?, delete


    //TODO --------- cards within collection

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{collectionId}/cards")
    public List<Card> createCardInCardCollection(@PathVariable Long collectionId)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection cardCollection = cardCollectionService.getById(collectionId);
        if (cardCollection.getOwner().equals(currentUser) || cardCollection.getVisibility().equals(CardCollectionVisibility.PUBLIC))
            return cardCollectionService.getById(collectionId).getCardList();
        else
            throw new SecurityException("User is not owner of collection or collection is not public");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{collectionId}/cards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCardInCardCollection(@PathVariable Long collectionId, @RequestBody Card card) throws EntityAlreadyExistsException, ValidationException, BadRequestException
    {
        cardService.persist(card);

        LOG.debug("CollectionCategory {} successfully added.", card);
        HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{collectionId}/cards/{id}", card.getId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

}
