package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Role;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class UserDao extends BaseDao<User>
{
    public UserDao()
    {
        super(User.class);
    }

    public static Specification<User> ByRole(Role role)
    {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get(User_.ROLE), role);
        };
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

    public User findByUsername(String username)
    {
        try
        {
            //System.out.println(em.createQuery("select u from User u").getResultList());
            return em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }
}
