package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

//import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Specification;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.LinkedCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.LinkedCollection_;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class LinkedCollectionDao extends BaseDao<LinkedCollection>
{
    public LinkedCollectionDao()
    {
        super(LinkedCollection.class);
    }


    public static Specification<LinkedCollection> WhereLinkedToUser(User user)
    {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.and(cb.equal(root.get(LinkedCollection_.USER), user));
        };
    }

    public static Specification<LinkedCollection> ByCollection(CardCollection cardCollection)
    {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get(LinkedCollection_.cardCollection), cardCollection);
        };
    }

    /**
     * For checking whether user already added collection to favourite
     * @return
     */
    public static Specification<LinkedCollection> ByUserAndCollection(CardCollection cardCollection, User user)
    {
        return Specification.where(ByCollection(cardCollection)).and(WhereLinkedToUser(user));
    }
}
