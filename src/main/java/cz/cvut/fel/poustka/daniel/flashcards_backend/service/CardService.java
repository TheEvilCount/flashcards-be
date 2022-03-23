package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.CardDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CardService
{
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);

    private final CardDao cardDao;

    @Autowired
    public CardService(CardDao cardDao)
    {
        this.cardDao = cardDao;
    }

    @Transactional(readOnly = true)
    public Card getById(Long id)
    {
        return cardDao.find(id);
    }

    @Transactional(readOnly = true)
    public List<Card> getAll()
    {
        return this.cardDao.findAll();
    }


    @Transactional
    public void persist(Card card) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        cardValidation(card);
        cardDao.persist(card);
    }

    @Transactional
    public void update(Card card) throws BadRequestException, EntityAlreadyExistsException
    {
        Objects.requireNonNull(card);
        cardValidation(card);
        cardDao.update(card);
    }

    @Transactional
    public void delete(Card card)
    {
        Objects.requireNonNull(card);
        cardDao.remove(card);
    }

    // validation
    private void cardValidation(Card card) throws BadRequestException
    {
        Objects.requireNonNull(card);

        if (card.getBackText() == null)
            throw new BadRequestException("BackText cannot be null");
        if (card.getFrontText() == null)
            throw new BadRequestException("FrontText cannot be null");
        if (card.getCollection() == null)
            throw new BadRequestException("Collection cannot be null");
    }
}
