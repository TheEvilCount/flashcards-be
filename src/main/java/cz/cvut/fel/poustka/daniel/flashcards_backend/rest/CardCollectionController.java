package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.*;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto.CollectionDTO;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto.CollectionUpdateDTO;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.util.RestUtils;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CardCollectionService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CardService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.CollectionCategoryService;
import cz.cvut.fel.poustka.daniel.flashcards_backend.service.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.text.ParseException;
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
    private final CollectionCategoryService collectionCategoryService;

    private final ModelMapper modelMapper;

    @Autowired
    public CardCollectionController(CardCollectionService cardCollectionService, UserService userService, CardService cardService, CollectionCategoryService collectionCategoryService, ModelMapper modelMapper)
    {
        this.cardCollectionService = cardCollectionService;
        this.userService = userService;
        this.cardService = cardService;
        this.collectionCategoryService = collectionCategoryService;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardCollection getCollection(@PathVariable Long id)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection cardCollection = cardCollectionService.getById(id);

        if (!cardCollection.getOwner().equals(currentUser) ||
                !cardCollection.getVisibility().equals(CardCollectionVisibility.PUBLIC))
        {
            throw new SecurityException("User is not an owner of this collection or collection is not public!");
        }
        return cardCollection;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionDTO> getCollections(
            @RequestParam Integer type,
            @RequestParam(required = false) Integer p,
            @RequestParam(required = false) Integer ps)
    {
        User currentUser = userService.getCurrentUser();

        CardCollectionVisibility requestedType;
        //Check if requested type of collections exists

        requestedType = CardCollectionVisibility.getByValue(type);
        LOG.debug("get collections - type: " + requestedType);

        Pageable pagination;
        if (p == null || ps == null)
            pagination = Pageable.unpaged();
        else
            pagination = PageRequest.of(p, ps);
        LOG.debug("pagination: " + pagination);
        List<CardCollection> toReturn;
        switch (requestedType)
        {
            default:
            case PUBLIC:
                toReturn = cardCollectionService.getAllPublic(pagination);
                break;
            case PRIVATE:
                //toReturn = cardCollectionService.getAllPrivate(currentUser, pagination); //this return all owned private
                toReturn = cardCollectionService.getAllOwned(currentUser, pagination); //this return all owned private
                break;
            case LINKED:
                toReturn = cardCollectionService.getAllFavourite(currentUser, pagination);
                break;
        }
        return toReturn.parallelStream().map(this::convertToDto).toList();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/discover")
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionDTO> discoverCollections(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer p,
            @RequestParam(required = false) Integer ps)
    {

        Pageable pagination;
        if (p == null || ps == null)
            pagination = Pageable.unpaged();
        else
            pagination = PageRequest.of(p, ps);

        List<CardCollection> toReturn = cardCollectionService.getPublicByTitle(title, pagination);
        return toReturn.parallelStream().map(this::convertToDto).toList();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCardCollection(@RequestBody CardCollection cardCollection) throws EntityAlreadyExistsException, ValidationException, BadRequestException
    {
        User currentUser = userService.getCurrentUser();

        cardCollectionService.persist(cardCollection, currentUser);

        LOG.debug("Collection {} successfully added.", cardCollection);
        HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", cardCollection.getId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    //delete
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCollection(@PathVariable Long id)
    {
        CardCollection toRemove = cardCollectionService.getById(id);
        if (toRemove == null)
        {
            throw new EntityNotFoundException("Collection can't be removed!");
        }

        User currentUser = userService.getCurrentUser();

        if (!toRemove.getOwner().equals(currentUser))
            throw new SecurityException("User is not an owner of this collection!");

        cardCollectionService.delete(toRemove);
        LOG.debug("Removed Collection {}.", toRemove);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{id}/fav")
    @ResponseStatus(HttpStatus.OK)
    public void addCollectionToFavourite(@PathVariable Long id) throws ValidationException
    {
        User currentUser = userService.getCurrentUser();
        CardCollection collection = cardCollectionService.getById(id);

        cardCollectionService.addToFavorite(collection, currentUser);

        LOG.debug("Collection {} successfully added to favourite.", collection);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{id}/unfav")
    @ResponseStatus(HttpStatus.OK)
    public void removeCollectionFromFavourite(@PathVariable Long id) throws EntityNotFoundException, NullPointerException
    {
        User currentUser = userService.getCurrentUser();

        LinkedCollection linkedCollection = cardCollectionService.getLinkedCollection(currentUser, id);

        if (linkedCollection == null)
            throw new EntityNotFoundException("Favourite collection not found!");

        cardCollectionService.delete(linkedCollection);
        LOG.debug("Collection {} successfully removed from favourite.", linkedCollection);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{id}/publish")
    @ResponseStatus(HttpStatus.OK)
    public void publishCollection(@PathVariable Long id)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection collection = cardCollectionService.getById(id);

        if (!collection.getOwner().equals(currentUser))
        {
            throw new SecurityException("User is not an owner of this collection!");
        }

        cardCollectionService.changeVisibilityToPublic(collection);
        LOG.debug("CollectionCategory {} successfully published.", collection);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{id}/privatize")
    @ResponseStatus(HttpStatus.OK)
    public void privatizeCollection(@PathVariable Long id)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection collection = cardCollectionService.getById(id);

        if (!collection.getOwner().equals(currentUser))
        {
            throw new SecurityException("User is not an owner of this collection!");
        }

        cardCollectionService.changeVisibilityToPrivate(collection);
        LOG.debug("Collection {} successfully privatized.", collection);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{id}/duplicate")
    @ResponseStatus(HttpStatus.OK)
    public void duplicateCollection(@PathVariable Long id) throws ValidationException, EntityAlreadyExistsException, BadRequestException
    {
        User currentUser = userService.getCurrentUser();
        CardCollection collection = cardCollectionService.getById(id);

        if (collection.getVisibility().equals(CardCollectionVisibility.PRIVATE) && !collection.getOwner().equals(currentUser))
        {
            throw new SecurityException("User is not an owner of this collection!");
        }

        cardCollectionService.duplicateCollectionForUser(collection, currentUser);
        LOG.debug("Collection {} successfully duplicated.", collection);
    }

    //TODO update
    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateCollection(@PathVariable Long id, @RequestBody CollectionUpdateDTO collection) throws ValidationException, EntityAlreadyExistsException, BadRequestException
    {
        CardCollection toUpdate = cardCollectionService.getById(id);
        if (toUpdate == null)
        {
            throw new EntityNotFoundException("Collection not found!");
        }

        User currentUser = userService.getCurrentUser();

        if (!toUpdate.getOwner().equals(currentUser))
            throw new SecurityException("User is not an owner of this collection!");

        if (!toUpdate.getCategory().getTitle().equals(collection.getCategory()))
        {
            CollectionCategory cat = collectionCategoryService.getByTitle(collection.getCategory());
            if (cat == null)
                throw new EntityNotFoundException("CollectionCategory not found!");
            toUpdate.setCategory(cat);
        }
        toUpdate.setTitle(collection.getTitle());
        toUpdate.setCollectionColor(collection.getCollectionColor());

        cardCollectionService.persist(toUpdate);
        LOG.debug("Updated Collection {}.", toUpdate);
    }


    //TODO --------- cards within collection

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{collectionId}/cards")
    public List<Card> getCardsInCardCollection(@PathVariable Long collectionId)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection cardCollection = cardCollectionService.getById(collectionId);

        if (cardCollection.getOwner().equals(currentUser) || cardCollection.getVisibility().equals(CardCollectionVisibility.PUBLIC))
            return cardCollectionService.getById(collectionId).getCardList();
        else
            throw new SecurityException("User is not owner of the collection or collection is not public");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{collectionId}/cards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCardInCardCollection(@PathVariable Long collectionId, @RequestBody Card card) throws EntityAlreadyExistsException, ValidationException, BadRequestException
    {
        User currentUser = userService.getCurrentUser();
        CardCollection collection = cardCollectionService.getById(collectionId);

        Card cardToPersist = new Card();
        if (collection.getOwner().equals(currentUser))
        {
            cardToPersist.setFrontText(card.getFrontText());
            cardToPersist.setBackText(card.getBackText());
            cardToPersist.setCollection(collection);
            cardService.persist(cardToPersist);
        }
        else
            throw new SecurityException("User is not owner of the collection");

        LOG.debug("CollectionCategory {} successfully added.", cardToPersist);
        HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{collectionId}/cards/{id}", collection.getId(), cardToPersist.getId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{collectionId}/cards/{cardId}")
    public Card getCardInCardCollection(@PathVariable Long collectionId, @PathVariable Long cardId)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection cardCollection = cardCollectionService.getById(collectionId);

        if (cardCollection.getOwner().equals(currentUser) || cardCollection.getVisibility().equals(CardCollectionVisibility.PUBLIC))
            return cardCollection.getCardById(cardId);
        else
            throw new SecurityException("User is not owner of the collection or collection is not public");
    }


    //________DTOs_________
    private CollectionDTO convertToDto(CardCollection cardCollection)
    {
        CollectionDTO collectionDTO = modelMapper.map(cardCollection, CollectionDTO.class);
        collectionDTO.setCategory(cardCollection.getCategory().getTitle());
        collectionDTO.setCardNum(cardCollection.getCardCount());
        collectionDTO.setVisibility(cardCollection.getVisibility());
        return collectionDTO;
    }

    private CardCollection convertToEntity(CollectionDTO collectionDTO) throws ParseException
    {
        CardCollection cardCollection = modelMapper.map(collectionDTO, CardCollection.class);

        CollectionCategory category = collectionCategoryService.getByTitle(collectionDTO.getCategory());
        cardCollection.setCategory(category);

        if (collectionDTO.getId() != null)
        {
            CardCollection collection = cardCollectionService.getById(collectionDTO.getId());
            //TODO update??
        }
        return cardCollection;
    }
}
