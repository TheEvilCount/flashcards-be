package cz.cvut.fel.poustka.daniel.flashcards_backend.security.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;

public class LoginStatus
{
    private final boolean loggedIn;
    private final String message;
    private final boolean success;
    private final User user;

    public LoginStatus(boolean loggedIn, boolean success, User user, String message)
    {
        this.loggedIn = loggedIn;
        this.message = message;
        this.success = success;
        this.user = user;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public User getUser()
    {
        return user;
    }
}
