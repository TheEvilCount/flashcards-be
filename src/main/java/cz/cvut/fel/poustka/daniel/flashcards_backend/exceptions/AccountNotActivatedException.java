package cz.cvut.fel.poustka.daniel.flashcards_backend.exceptions;

import org.springframework.security.authentication.AccountStatusException;

public class AccountNotActivatedException extends AccountStatusException
{
    public AccountNotActivatedException(String msg)
    {
        super(msg);
    }
}
