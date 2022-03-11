package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.CardCollectionDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.LinkedCollectionDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Sorting;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.LinkedCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CardCollectionService
{
    private static final Logger LOG = LoggerFactory.getLogger(CardCollectionService.class);

    private final CardCollectionDao cardCollectionDao;
    private final LinkedCollectionDao linkedCollectionDao;

    @Autowired
    public CardCollectionService(CardCollectionDao cardCollectionDao, LinkedCollectionDao linkedCollectionDao)
    {
        this.cardCollectionDao = cardCollectionDao;
        this.linkedCollectionDao = linkedCollectionDao;
    }

    @Transactional(readOnly = true)
    public CardCollection getById(Long id)
    {
        return cardCollectionDao.find(id);
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getAll()
    {
        return this.cardCollectionDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getPublicByTitle(String title, Pageable pagination)
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByVisibilityAndTitle(CardCollectionVisibility.PUBLIC, title), pagination);
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getPublicByTitle(String title, Pageable pagination, User user)
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByVisibilityAndTitleAndNotOwnedByUser(CardCollectionVisibility.PUBLIC, title, user), pagination);
    }

    @Transactional(readOnly = true)
    public Long getNumberOfPublicByTitle(String title)
    {
        return cardCollectionDao.findNumberOf(CardCollectionDao.ByVisibilityAndTitle(CardCollectionVisibility.PUBLIC, title));
    }

    @Transactional(readOnly = true)
    /**
     * Returns number of public collections where user is not the owner filtered by title
     */
    public Long getNumberOfPublicByTitle(String title, User user)
    {
        return cardCollectionDao.findNumberOf(CardCollectionDao.ByVisibilityAndTitleAndNotOwnedByUser(CardCollectionVisibility.PUBLIC, title, user));
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getAllPrivate(User user, Pageable pagination)
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByVisibilityUser(CardCollectionVisibility.PRIVATE, user), pagination);
    }

    @Transactional(readOnly = true)
    public Long getNumberOfAllPrivate(User user)
    {
        return cardCollectionDao.findNumberOf(CardCollectionDao.ByVisibilityUser(CardCollectionVisibility.PRIVATE, user));
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getAllOwned(User user, Pageable pagination)
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByUserOwned(user), pagination);
    }

    @Transactional(readOnly = true)
    public Long getNumberOfAllOwned(User user)
    {
        return cardCollectionDao.findNumberOf(CardCollectionDao.ByUserOwned(user));
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getAllFavourite(User user, Pageable pagination)
    {
        return getLinkedCollections(user, pagination).stream().map(LinkedCollection::getCardCollection).toList();
    }

    @Transactional(readOnly = true)
    public Long getNumberOfAllFavourite(User user)
    {
        return linkedCollectionDao.findNumberOf(LinkedCollectionDao.WhereLinkedToUser(user));
    }

    @Transactional(readOnly = true)
    public List<LinkedCollection> getLinkedCollections(User user, Pageable pagination)
    {
        return linkedCollectionDao.findAll(
                new Sorting(),
                LinkedCollectionDao.WhereLinkedToUser(user),
                pagination
        );
    }

    @Transactional(readOnly = true)
    public LinkedCollection getLinkedCollection(User user, Long id)
    {
        return linkedCollectionDao.findAll(
                        new Sorting(),
                        LinkedCollectionDao.WhereLinkedToUser(user),
                        Pageable.unpaged()
                ).stream()
                .parallel()
                .filter(linkedCollection -> linkedCollection.getCardCollection().getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getAllPublic(Pageable pagination) throws UsernameNotFoundException
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByVisibility(CardCollectionVisibility.PUBLIC), pagination);
    }

    @Transactional(readOnly = true)
    /**
     * Return all public collections without public collections created by current user
     */
    public List<CardCollection> getAllPublic(Pageable pagination, User user) throws UsernameNotFoundException
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByVisibilityAndUserNotOwned(CardCollectionVisibility.PUBLIC, user), pagination);
    }

    @Transactional(readOnly = true)
    public Long getNumberOfAllPublic()
    {
        return cardCollectionDao.findNumberOf(CardCollectionDao.ByVisibility(CardCollectionVisibility.PUBLIC));
    }

    //TODO add filtering by category

    @Transactional
    public void addToFavorite(CardCollection cardCollection, User user) throws ValidationException
    {
        //validation if user don't already have this collection in favourite
        List<LinkedCollection> linkedCollectionList = linkedCollectionDao.findAll(new Sorting(), LinkedCollectionDao.ByUserAndCollection(cardCollection, user));
        if (linkedCollectionList.size() > 0)
            throw new ValidationException("User already has this collection in favourites");

        if (cardCollection.getOwner().equals(user))
            throw new ValidationException("User is owner of this collection. Cannot add to favourites");

        //Create new linked collection and assign it to user
        LinkedCollection linkedCollection = new LinkedCollection();
        linkedCollection.setCardCollection(cardCollection);
        linkedCollection.setUser(user);

        linkedCollectionDao.persist(linkedCollection);

        cardCollection.AddCounterFav();
        cardCollectionDao.update(cardCollection);
    }

    @Transactional
    public void removeFromFavorite(LinkedCollection linkedCollection) throws BadRequestException
    {
        CardCollection original = linkedCollection.getCardCollection();
        original.SubstractCounterFav();
        this.update(original);

        this.delete(linkedCollection);
    }

    @Transactional
    public void duplicateCollectionForUser(CardCollection cardCollection, User user) throws ValidationException, EntityAlreadyExistsException, BadRequestException
    {
        cardCollection.AddCounterDup();

        CardCollection collection = cardCollection.duplicate();
        collection.setVisibility(CardCollectionVisibility.PRIVATE);
        collection.setOwner(user);
        collection.setCounterDup(0);
        collection.setCounterFav(0);

        collection.setId(null);
        cardCollectionDao.persist(collection);

        this.persist(collection, user);
    }

    @Transactional
    public void changeVisibilityToPrivate(CardCollection cardCollection)
    {
        List<LinkedCollection> linkedCollectionList = linkedCollectionDao.findAll(new Sorting(), LinkedCollectionDao.ByCollection(cardCollection));

        changeVisibility(cardCollection, CardCollectionVisibility.PRIVATE);
        long duplicationNumBCP = cardCollection.getCounterDup();
        if (linkedCollectionList.size() > 0)
        {
            linkedCollectionList.forEach(linkedCollection -> {
                try
                {
                    duplicateCollectionForUser(linkedCollection.getCardCollection(), linkedCollection.getUser());
                }
                catch (ValidationException | EntityAlreadyExistsException | BadRequestException e)
                {
                    e.printStackTrace();
                }
            });
        }
        cardCollection.setCounterDup(duplicationNumBCP);
    }

    @Transactional
    public void changeVisibilityToPublic(CardCollection cardCollection)
    {
        changeVisibility(cardCollection, CardCollectionVisibility.PUBLIC);
    }

    private void changeVisibility(CardCollection cardCollection, CardCollectionVisibility visibility)
    {
        cardCollection.setVisibility(visibility);
    }

    @Transactional
    public void persist(CardCollection cardCollection) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        cardCollection.setCreationDate(Date.valueOf(LocalDate.now()));

        cardCollectionValidation(cardCollection);
        cardCollectionDao.persist(cardCollection);
    }

    @Transactional
    public void persist(CardCollection cardCollection, User currentUser) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        cardCollection.setCreationDate(Date.valueOf(LocalDate.now()));
        cardCollection.setOwner(currentUser);

        cardCollectionValidation(cardCollection);
        cardCollectionDao.persist(cardCollection);
    }

    @Transactional
    public void update(CardCollection cardCollection) throws BadRequestException
    {
        Objects.requireNonNull(cardCollection);
        cardCollectionValidation(cardCollection);
        cardCollectionDao.update(cardCollection);
    }

    @Transactional
    public void delete(CardCollection cardCollection)
    {
        Objects.requireNonNull(cardCollection);

        if (cardCollection.getVisibility().equals(CardCollectionVisibility.PRIVATE))
        {
            cardCollectionDao.remove(cardCollection);
        }
        else if (cardCollection.getVisibility().equals(CardCollectionVisibility.PUBLIC))
        {
            //if there is more than 0 user favourite this collection duplicate this collection as private
            if (cardCollection.getLinkedCollectionList().size() > 0)
            {
                cardCollection.getLinkedCollectionList().forEach(linkedCollection -> {
                    CardCollection toPrivate = linkedCollection.getCardCollection().duplicate();
                    toPrivate.setOwner(linkedCollection.getUser());
                    toPrivate.setId(null);
                    try
                    {
                        this.persist(toPrivate);
                    }
                    catch (EntityAlreadyExistsException | BadRequestException | ValidationException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
            cardCollectionDao.remove(cardCollection);
        }
    }

    @Transactional
    public void delete(LinkedCollection linkedCollection)
    {
        Objects.requireNonNull(linkedCollection);
        linkedCollectionDao.remove(linkedCollection);
    }

    // validation
    private void cardCollectionValidation(CardCollection cardCollection) throws BadRequestException
    {
        Objects.requireNonNull(cardCollection);

        if (cardCollection.getTitle() == null)
            throw new BadRequestException("Title cannot be null");
        if (cardCollection.getCollectionColor() == null)
            throw new BadRequestException("Color cannot be null");
        if (cardCollection.getCreationDate() == null)
            throw new BadRequestException("CreationDateTime cannot be null");
        if (cardCollection.getCategory() == null)
            throw new BadRequestException("Category cannot be null");
        if (cardCollection.getVisibility() == null)
            throw new BadRequestException("Visibility cannot be null");
        if (cardCollection.getOwner() == null)
            throw new BadRequestException("Owner cannot be null");
    }

}
