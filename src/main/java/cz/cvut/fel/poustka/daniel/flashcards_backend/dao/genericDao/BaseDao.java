package cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.OrderType;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Sorting;
import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Specification;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class BaseDao<T> implements GenericDao<T>, SpecialDao<T>
{
    protected final Class<T> type;
    @PersistenceContext
    protected EntityManager em;

    protected BaseDao(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T find(Integer id)
    {
        Objects.requireNonNull(id);
        return em.find(type, id);
    }

    @Override
    public List<T> findAll()
    {
        try
        {
            return em.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type).getResultList();
        }
        catch (RuntimeException e)
        {
            throw new javax.persistence.PersistenceException(e);
        }
    }

    @Override
    public void persist(T entity)
    {
        Objects.requireNonNull(entity);
        try
        {
            em.persist(entity);
        }
        catch (RuntimeException e)
        {
            throw new javax.persistence.PersistenceException(e);
        }
    }

    @Override
    public void persist(Collection<T> entities)
    {
        Objects.requireNonNull(entities);
        if (entities.isEmpty())
        {
            return;
        }
        try
        {
            entities.forEach(this::persist);
        }
        catch (RuntimeException e)
        {
            throw new javax.persistence.PersistenceException(e);
        }
    }

    @Override
    public T update(T entity)
    {
        Objects.requireNonNull(entity);
        try
        {
            return em.merge(entity);
        }
        catch (RuntimeException e)
        {
            throw new javax.persistence.PersistenceException(e);
        }
    }

    @Override
    public void remove(T entity)
    {
        Objects.requireNonNull(entity);
        try
        {
            final T toRemove = em.merge(entity);
            if (toRemove != null)
            {
                em.remove(toRemove);
            }
        }
        catch (RuntimeException e)
        {
            throw new PersistenceException(e);
        }
    }

    @Override
    public boolean exists(Integer id)
    {
        return id != null && em.find(type, id) != null;
    }

    @Override
    public T find(Sorting sorting, Specification<T> s)
    {
        List<T> list = findAll(sorting, s);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    @Override
    public List<T> findAll(Sorting sorting, Specification<T> s)
    {
        try
        {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> q = cb.createQuery(type);
            Root<T> r = q.from(type);

            q.select(r).where(s.toPredicate(r, q, cb));

            if (sorting.getOrderType().equals(OrderType.DESCENDING))
                q.orderBy(cb.desc(r.get(sorting.getColumnToOrderBy())));
            else
                q.orderBy(cb.asc(r.get(sorting.getColumnToOrderBy())));

            TypedQuery<T> query = em.createQuery(q);

            return query.getResultList();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }
}
