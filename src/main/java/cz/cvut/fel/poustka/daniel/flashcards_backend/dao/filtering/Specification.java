package cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering;

import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;

@Deprecated
/**
 * Not used. Now using org.springframework.data.jpa.domain.Specification
 */
public interface Specification<T> extends Serializable
{
    @Nullable
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
