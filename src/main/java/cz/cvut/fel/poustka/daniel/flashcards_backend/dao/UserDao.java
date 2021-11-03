package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class UserDao extends BaseDao<User>
{

    public UserDao()
    {
        super(User.class);
    }

    public User findByEmail(String email)
    {
        try
        {
            //System.out.println(em.createQuery("select u from User u").getResultList());
            return em.createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }
}
