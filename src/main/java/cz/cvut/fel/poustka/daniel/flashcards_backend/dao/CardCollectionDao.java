package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Specification;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection_;
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
        return (Specification<CardCollection>) (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get(CardCollection_.TITLE), title);
        };
    }
}
