package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Specification;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection_;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class CardCollectionDao extends BaseDao<CardCollection>
{
    public CardCollectionDao()
    {
        super(CardCollection.class);
    }

    public static Specification<CardCollection> ByTitle(String title)
    {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get(CardCollection_.TITLE), title);
        };
    }

    public static Specification<CardCollection> ByVisibility(CardCollectionVisibility visibility)
    {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get(CardCollection_.VISIBILITY), visibility.ordinal());
        };
    }

    public static Specification<CardCollection> ByVisibility(CardCollectionVisibility visibility, User user)
    {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.and(cb.equal(root.get(CardCollection_.VISIBILITY), visibility.ordinal()),
                          cb.equal(root.get(CardCollection_.OWNER), user));
        };
    }
}
