package cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions;

/**
 * Signifies that invalid data have been provided to the application.
 */
public class ValidationException extends Exception
{

    public ValidationException(String message)
    {
        super(message);
    }
}
