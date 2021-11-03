package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Specification;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CollectionCategory;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CollectionCategory_;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionCategoryDao extends BaseDao<CollectionCategory>
{
    public CollectionCategoryDao()
    {
        super(CollectionCategory.class);
    }

    public static Specification<CollectionCategory> ByTitle(String title)
    {
        return (Specification<CollectionCategory>) (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get(CollectionCategory_.TITLE), title);
        };
    }
}
