package cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions;

public class PersistenceException extends RuntimeException
{

    public PersistenceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PersistenceException(Throwable cause)
    {
        super(cause);
    }
}
