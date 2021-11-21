package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.PasswordResetTokenDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.BadRequestException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.ValidationException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.PasswordResetToken;
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
public class PasswordResetTokenService
{

    private static final Logger LOG = LoggerFactory.getLogger(PasswordResetTokenService.class);

    private final PasswordResetTokenDao passwordResetTokenDao;

    @Autowired
    public PasswordResetTokenService(PasswordResetTokenDao passwordResetTokenDao)
    {
        this.passwordResetTokenDao = passwordResetTokenDao;
    }

    public PasswordResetToken getByToken(String token) throws UsernameNotFoundException
    {
        final PasswordResetToken passwordResetToken = passwordResetTokenDao.findByToken(token);
        return passwordResetToken;
    }

    @Transactional(readOnly = true)
    public PasswordResetToken getById(Long id)
    {
        return passwordResetTokenDao.find(id);
    }

    @Transactional(readOnly = true)
    public List<PasswordResetToken> getAll()
    {
        return this.passwordResetTokenDao.findAll();
    }


    @Transactional
    public void persist(PasswordResetToken token) throws EntityAlreadyExistsException, BadRequestException, ValidationException
    {
        passwordResetTokenDao.persist(token);
    }

    @Transactional
    public void update(PasswordResetToken token) throws BadRequestException, EntityAlreadyExistsException
    {
        Objects.requireNonNull(token);

        //TODO validation

        passwordResetTokenDao.update(token);
    }

    @Transactional
    public void delete(PasswordResetToken token)
    {
        Objects.requireNonNull(token);

        passwordResetTokenDao.remove(token);
    }
}
