package cz.cvut.fel.poustka.daniel.flashcards_backend.config;

public class SecurityConstants
{
    public static final String SESSION_COOKIE_NAME = "session_id";
    public static final String REMEMBER_ME_COOKIE_NAME = "remember";
    public static final String EMAIL_PARAM = "email";
    public static final String PASSWORD_PARAM = "password";
    public static final String SECURITY_CHECK_URI = "/login";
    public static final String LOGOUT_URI = "/logout";
    public static final String COOKIE_URI = "/flashcards/api/v1";

    /**
     * Session timeout in seconds.
     */
    public static final int SESSION_TIMEOUT = 60 * 60 * 24;
    public static final int REMEMBER_TIMEOUT = 60 * 60 * 24 * 2;

    private SecurityConstants()
    {
    }
}
