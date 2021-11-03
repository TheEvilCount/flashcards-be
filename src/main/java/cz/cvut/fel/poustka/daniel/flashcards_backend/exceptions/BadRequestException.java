package cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions;

public class BadRequestException extends Exception
{
    public BadRequestException(String message)
    {
        super(message);
    }
}
