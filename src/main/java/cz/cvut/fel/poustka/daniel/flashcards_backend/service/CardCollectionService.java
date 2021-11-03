package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.CardCollectionDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Sorting;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CardCollectionService(CardCollectionDao cardCollectionDao)
    {
        this.cardCollectionDao = cardCollectionDao;
    }

    @Transactional(readOnly = true)
    public CardCollection getById(int id)
    {
        return cardCollectionDao.find(id);
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getAll()
    {
        return this.cardCollectionDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<CardCollection> getByTitle(String title) throws UsernameNotFoundException
    {
        return cardCollectionDao.findAll(new Sorting(), CardCollectionDao.ByTitle(title));
    }

    //TODO add filtering by category

    @Transactional
    public void addToFavorite(CardCollection cardCollection, User user)
    {
        //TODO
    }

    @Transactional
    public void duplicateCollectionForUser(CardCollection cardCollection, User user)
    {
        //TODO
    }

    @Transactional
    public void changeVisibilityToPrivate(CardCollection cardCollection)
    {
        //TODO check if no other user is linked. if yes then duplicate as private for all users
        changeVisibility(cardCollection, CardCollectionVisibility.PRIVATE);
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
        //TODO validation

        cardCollection.setCreationDate(Date.valueOf(LocalDate.now()));

        cardCollectionValidation(cardCollection);
        cardCollectionDao.persist(cardCollection);
    }

    @Transactional
    public void update(CardCollection cardCollection) throws BadRequestException, EntityAlreadyExistsException
    {
        Objects.requireNonNull(cardCollection);

        //TODO validation

        cardCollectionDao.update(cardCollection);
    }

    @Transactional
    public void delete(CardCollection cardCollection)
    {
        Objects.requireNonNull(cardCollection);
        //TODO check if no other user is linked. if yes then duplicate as private for all users

        cardCollectionDao.remove(cardCollection);
    }

    // validation
    private void cardCollectionValidation(CardCollection cardCollection) throws BadRequestException
    {
        Objects.requireNonNull(cardCollection);

        if (cardCollection.getTitle() == null)
            throw new BadRequestException("Title cannot be null");
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