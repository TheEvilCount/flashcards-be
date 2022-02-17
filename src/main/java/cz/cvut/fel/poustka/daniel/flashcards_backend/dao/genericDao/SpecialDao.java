package cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering.Sorting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SpecialDao<T>
{
    T find(Sorting sorting, Specification<T> s);

    List<T> findAll(Sorting sorting, Specification<T> s);

    List<T> findAll(Sorting sorting, Specification<T> s, Pageable page);
}
