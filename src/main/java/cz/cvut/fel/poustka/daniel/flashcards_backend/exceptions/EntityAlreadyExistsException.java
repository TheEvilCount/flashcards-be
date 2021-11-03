package cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions;

public class EntityAlreadyExistsException extends Exception
{
    public EntityAlreadyExistsException(String message)
    {
        super(message);
    }
}
