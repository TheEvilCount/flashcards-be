package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.VerificationTokenDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.VerificationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class VerificationTokenService
{

    private static final Logger LOG = LoggerFactory.getLogger(VerificationTokenService.class);

    private final VerificationTokenDao verificationTokenDao;

    @Autowired
    public VerificationTokenService(VerificationTokenDao verificationTokenDao)
    {
        this.verificationTokenDao = verificationTokenDao;
    }

    public VerificationToken getByToken(String token) throws UsernameNotFoundException
    {
        final VerificationToken verificationToken = verificationTokenDao.findByVerificationToken(token);
        return verificationToken;
    }

    @Transactional(readOnly = true)
    public VerificationToken getById(Long id)
    {
        return verificationTokenDao.find(id);
    }

    @Transactional(readOnly = true)
    public List<VerificationToken> getAll()
    {
        return this.verificationTokenDao.findAll();
    }


    @Transactional
    public void persist(VerificationToken token) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        verificationTokenDao.persist(token);
    }

    @Transactional
    public void update(VerificationToken token) throws BadRequestException, EntityAlreadyExistsException
    {
        Objects.requireNonNull(token);

        //TODO validation

        verificationTokenDao.update(token);
    }

    @Transactional
    public void delete(VerificationToken token)
    {
        Objects.requireNonNull(token);

        verificationTokenDao.remove(token);
    }
}
