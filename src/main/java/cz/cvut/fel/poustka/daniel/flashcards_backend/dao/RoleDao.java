package cz.cvut.fel.poustka.daniel.flashcards_backend.dao;

import cz.cvut.fel.poustka.daniel.flashcards_backend.dao.genericDao.BaseDao;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Role;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDao extends BaseDao<Role>
{
    public RoleDao()
    {
        super(Role.class);
    }
}
