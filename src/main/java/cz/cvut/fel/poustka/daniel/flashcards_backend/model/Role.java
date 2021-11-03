package cz.cvut.fel.poustka.daniel.flashcards_backend.model;


/**
 * @author Daniel Poustka
 * @version 1.0
 */

public enum Role
{
    ADMIN("ROLE_ADMIN"), USER("ROLE_USER");

    private final String name;

    Role(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}