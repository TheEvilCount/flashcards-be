package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.LinkedCollection;
import org.springframework.stereotype.Repository;

@Repository
public class LinkedCollectionDao extends BaseDao<LinkedCollection>
{
    public LinkedCollectionDao()
    {
        super(LinkedCollection.class);
    }
}
