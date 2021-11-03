package cz.cvut.fel.poustka.daniel.flashcards_backend.service;


import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.CollectionCategoryDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Sorting;
import cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions.EntityAlreadyExistsException;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CollectionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CollectionCategoryService
{
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final CollectionCategoryDao collectionCategoryDao;

    @Autowired
    public CollectionCategoryService(CollectionCategoryDao collectionCategoryDao)
    {
        this.collectionCategoryDao = collectionCategoryDao;
    }


    @Transactional(readOnly = true)
    public CollectionCategory getById(int id)
    {
        return collectionCategoryDao.find(id);
    }

    @Transactional(readOnly = true)
    public List<CollectionCategory> getAll()
    {
        return this.collectionCategoryDao.findAll();
    }

    @Transactional
    public void persist(CollectionCategory collectionCategory) throws EntityAlreadyExistsException
    {
        Objects.requireNonNull(collectionCategory);

        if (collectionCategoryDao.find(new Sorting(), CollectionCategoryDao.ByTitle(collectionCategory.getTitle())) != null)
        {
            throw new EntityAlreadyExistsException("Genre with this title already exists!");
        }

        this.collectionCategoryDao.persist(collectionCategory);
    }

    @Transactional
    public void delete(CollectionCategory collectionCategory)
    {
        Objects.requireNonNull(collectionCategory);
        collectionCategoryDao.remove(collectionCategory);
    }
}
