package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Card;
import org.springframework.stereotype.Repository;

@Repository
public class CardDao extends BaseDao<Card>
{
    public CardDao()
    {
        super(Card.class);
    }
}
