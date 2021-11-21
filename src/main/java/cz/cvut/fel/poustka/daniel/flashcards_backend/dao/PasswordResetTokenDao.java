package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.PasswordResetToken;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class PasswordResetTokenDao extends BaseDao<PasswordResetToken>
{
    protected PasswordResetTokenDao()
    {
        super(PasswordResetToken.class);
    }

    public PasswordResetToken findByToken(String token)
    {
        try
        {
            //System.out.println(em.createQuery("select u from User u").getResultList());
            return em.createNamedQuery("PasswordResetToken.findByToken", PasswordResetToken.class)
                    .setParameter("token", token)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }
}
