package cz.cvut.fel.poustka.daniel.flashcards_backend.security.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;

public class LoginStatus
{
    private final boolean loggedIn;
    private final String errorMessage;
    private final boolean success;
    private final User user;

    public LoginStatus(boolean loggedIn, boolean success, User user, String errorMessage)
    {
        this.loggedIn = loggedIn;
        this.errorMessage = errorMessage;
        this.success = success;
        this.user = user;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public String getErrorMessage()
    {
        return errorMessage;
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
