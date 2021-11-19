package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.VerificationToken;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class VerificationTokenDao extends BaseDao<VerificationToken>
{
    protected VerificationTokenDao()
    {
        super(VerificationToken.class);
    }

    public VerificationToken findByVerificationToken(String token)
    {
        try
        {
            //System.out.println(em.createQuery("select u from User u").getResultList());
            return em.createNamedQuery("VerificationToken.findByToken", VerificationToken.class)
                    .setParameter("token", token)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }
}
