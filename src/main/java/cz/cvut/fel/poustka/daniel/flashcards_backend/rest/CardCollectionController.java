package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.*;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto.*;
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
    public CollectionDetailDTO getCollectionDetail(@PathVariable Long id)
    {
        User currentUser = userService.getCurrentUser();
        CardCollection cardCollection = cardCollectionService.getById(id);

        if (!(cardCollection.getOwner().equals(currentUser) || cardCollection.getVisibility().equals(CardCollectionVisibility.PUBLIC)))
        {
            throw new SecurityException("User is not an owner of this collection or collection is not public!");
        }
        return convertToCollectionDetailDto(cardCollection);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    public CollectionWithPgDTO getCollections(
            @RequestParam Integer type,
            @RequestParam(required = false, defaultValue = "0") Integer p,
            @RequestParam(required = false, defaultValue = "0") Integer ps)
    {
        User currentUser = userService.getCurrentUser();

        CardCollectionVisibility requestedType = CardCollectionVisibility.getByValue(type);
        //Check if requested type of collections exists

        boolean isPaginated = (!p.equals(0) && !ps.equals(0));
        Pageable pagination;
        if (isPaginated)
            pagination = PageRequest.of(p, ps);
        else
            pagination = Pageable.unpaged();
        LOG.error("get collections - isPaginated: " + isPaginated + ", type: " + requestedType + ", pagination: " + pagination);
        List<CardCollection> toReturn;
        long totalItems = 0;
        switch (requestedType)
        {
            default:
            case PUBLIC:
                //toReturn = cardCollectionService.getAllPublic(pagination, currentUser); //without owned
                toReturn = cardCollectionService.getAllPublic(pagination); //with owned
                totalItems = isPaginated ? cardCollectionService.getNumberOfAllPublic() : 0;
                break;
            case PRIVATE:
                //toReturn = cardCollectionService.getAllPrivate(currentUser, pagination); //this return all owned private
                toReturn = cardCollectionService.getAllOwned(currentUser, pagination); //this return all owned private
                totalItems = isPaginated ? cardCollectionService.getNumberOfAllOwned(currentUser) : 0;
                break;
            case LINKED:
                toReturn = cardCollectionService.getAllFavourite(currentUser, pagination);
                totalItems = isPaginated ? cardCollectionService.getNumberOfAllFavourite(currentUser) : 0;
                break;
        }

        return getCollectionWithPgDTO(p, ps, isPaginated, toReturn, totalItems);
    }

    private CollectionWithPgDTO getCollectionWithPgDTO(
            @RequestParam(required = false) Integer p, @RequestParam(required = false) Integer ps,
            boolean isPaginated, List<CardCollection> toReturn, Long totalItems)
    {
        CollectionWithPgDTO ret = new CollectionWithPgDTO();
        ret.setCollections(toReturn.parallelStream().map(this::convertToDto).toList());
        ret.setPage(p);
        ret.setPageSize(ps);
        if (isPaginated)
        {
            if (totalItems <= ps)
            {
                ret.setMaxPages(1);
            }
            else if ((totalItems % ps) == 0)
            {
                ret.setMaxPages(Math.toIntExact(totalItems / ps));
            }
            else
            {
                ret.setMaxPages(Math.toIntExact((totalItems / ps) + 1));
            }
        }
        else
            ret.setMaxPages(null);
        return ret;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/discover")
    @ResponseStatus(HttpStatus.OK)
    public CollectionWithPgDTO discoverCollections(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer p,
            @RequestParam(required = false) Integer ps)
    {
        boolean isPaginated = (p == null || ps == null);
        Pageable pagination;
        if (isPaginated)
            pagination = Pageable.unpaged();
        else
            pagination = PageRequest.of(p, ps);

        User currentUser = userService.getCurrentUser();

        //List<CardCollection> toReturn = cardCollectionService.getPublicByTitle(title, pagination); //with owned
        List<CardCollection> toReturn = cardCollectionService.getPublicByTitle(title, pagination, currentUser);
        long totalItems = cardCollectionService.getNumberOfPublicByTitle(title, currentUser);

        return getCollectionWithPgDTO(p, ps, isPaginated, toReturn, totalItems);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCardCollection(@RequestBody CollectionCreateDTO collectionDTO) throws EntityAlreadyExistsException, ValidationException, BadRequestException
    {
        User currentUser = userService.getCurrentUser();

        CollectionCategory collectionCategory = collectionCategoryService.getByTitle(collectionDTO.getCategory());
        if (collectionCategory == null)
            throw new EntityNotFoundException("Category not found");

        CardCollection cardCollection = new CardCollection();
        cardCollection.setTitle(collectionDTO.getTitle());
        cardCollection.setCollectionColor(collectionDTO.getCollectionColor());
        cardCollection.setVisibility(CardCollectionVisibility.valueOf(collectionDTO.getVisibility()));
        cardCollection.setCategory(collectionCategory);

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
    public void removeCollectionFromFavourite(@PathVariable Long id) throws EntityNotFoundException, NullPointerException, BadRequestException
    {
        User currentUser = userService.getCurrentUser();

        LinkedCollection linkedCollection = cardCollectionService.getLinkedCollection(currentUser, id);

        if (linkedCollection == null)
            throw new EntityNotFoundException("Favourite collection not found!");

        cardCollectionService.removeFromFavorite(linkedCollection);
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

    //Only cards
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

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping(value = "/{collectionId}/cards/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCardInCardCollection(@PathVariable Long collectionId, @PathVariable Long cardId) throws EntityAlreadyExistsException, BadRequestException
    {
        CardCollection collectionWithCardToRemove = cardCollectionService.getById(collectionId);
        if (collectionWithCardToRemove == null)
        {
            throw new EntityNotFoundException("Collection of deleting card not found!");
        }

        User currentUser = userService.getCurrentUser();

        if (!collectionWithCardToRemove.getOwner().equals(currentUser))
            throw new SecurityException("User is not an owner of collection where is located card to delete!");

        Card cardToRemove = cardService.getById(cardId);
        cardService.delete(cardToRemove);
        //collectionWithCardToRemove.setCardList(collectionWithCardToRemove.getCardList());
        //cardCollectionService.update(collectionWithCardToRemove);
        LOG.debug("Removed card within Collection {}.", collectionWithCardToRemove);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/{collectionId}/cards/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateCardInCollection(@PathVariable Long collectionId, @PathVariable Long cardId, @RequestBody CardDTO card) throws ValidationException, EntityAlreadyExistsException, BadRequestException
    {
        CardCollection collectionWithCardToRemove = cardCollectionService.getById(collectionId);
        if (collectionWithCardToRemove == null)
        {
            throw new EntityNotFoundException("Collection not found!");
        }

        User currentUser = userService.getCurrentUser();

        if (!collectionWithCardToRemove.getOwner().equals(currentUser))
            throw new SecurityException("User is not an owner of collection where is located card to delete!");

        Card toUpdate = cardService.getById(cardId);

        toUpdate.setBackText(card.getBackText());
        toUpdate.setFrontText(card.getFrontText());

        cardService.update(toUpdate);
        LOG.debug("Updated Card {}.", toUpdate);
    }


    //________DTOs_________
    private CollectionDTO convertToDto(CardCollection cardCollection)
    {
        CollectionDTO collectionDTO = modelMapper.map(cardCollection, CollectionDTO.class);
        collectionDTO.setCategory(cardCollection.getCategory().getTitle());
        collectionDTO.setCardNum(cardCollection.getCardCount());
        collectionDTO.setVisibility(cardCollection.getVisibility());
        collectionDTO.setOwner(cardCollection.getOwner().getUsername());
        return collectionDTO;
    }

    private CollectionDetailDTO convertToCollectionDetailDto(CardCollection cardCollection)
    {
        CollectionDetailDTO dto = modelMapper.map(cardCollection, CollectionDetailDTO.class);
        dto.setCategory(cardCollection.getCategory().getTitle());
        dto.setCardNum(cardCollection.getCardCount());
        dto.setVisibility(cardCollection.getVisibility());
        dto.setOwner(cardCollection.getOwner().getUsername());
        return dto;
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
