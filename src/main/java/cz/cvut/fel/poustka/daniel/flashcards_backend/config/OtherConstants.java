package cz.cvut.fel.poustka.daniel.flashcards_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * Class with constants loaded from application.config
 */
@Component
public class OtherConstants
{
    public static String FLASHCARDS_FE_URL;
    public static long VERIFICATION_TOKEN_EXPIRE;
    public static long PASSWORD_RESET_TOKEN_EXPIRE;
    public static String FLASHCARDS_BE_URL;
    public static String FLASHCARDS_BE_API_PATH;
    public static String FLASHCARDS_FE_PATHS_VERIFY;
    public static String FLASHCARDS_FE_PATHS_RESET;

    @Value("${flashcards.fe.url}")
    public void setFLASHCARDS_FE_URL(String value)
    {
        FLASHCARDS_FE_URL = value;
    }

    @Value("${flashcards.be.verificationToken.expireMs}")
    public void setVERIFICATION_TOKEN_EXPIRE(String value)
    {
        VERIFICATION_TOKEN_EXPIRE = Long.parseLong(value);
    }

    @Value("${flashcards.be.passwordResetToken.expireMs}")
    public void setPASSWORD_RESET_TOKEN_EXPIRE(String value)
    {
        PASSWORD_RESET_TOKEN_EXPIRE = Long.parseLong(value);
    }

    @Value("${flashcards.be.url}")
    public void setFLASHCARDS_BE_URL(String value)
    {
        FLASHCARDS_BE_URL = value;
    }

    @Value("${server.servlet.context-path}")
    public void setFLASHCARDS_BE_API_PATH(String value)
    {
        FLASHCARDS_BE_API_PATH = value;
    }

    @Value("${flashcards.fe.paths.verify}")
    public void setFLASHCARDS_FE_PATHS_VERIFY(String value)
    {
        FLASHCARDS_FE_PATHS_VERIFY = value;
    }

    @Value("${flashcards.fe.paths.reset}")
    public void setFLASHCARDS_FE_PATHS_RESET(String value)
    {
        FLASHCARDS_FE_PATHS_RESET = value;
    }
}
