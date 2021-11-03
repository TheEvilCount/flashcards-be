package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import org.springframework.stereotype.Repository;

@Repository
public class CardCollectionVisibilityDao extends BaseDao<CardCollectionVisibility>
{
    public CardCollectionVisibilityDao()
    {
        super(CardCollectionVisibility.class);
    }
}
