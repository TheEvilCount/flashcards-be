package cz.cvut.fel.poustka.daniel.flashcards_backend.security;

import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.AuthenticationToken;
import cz.cvut.fel.poustka.daniel.flashcards_backend.security.model.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class SecurityUtils
{
    /**
     * Gets the currently authenticated user.
     * @return Current user
     */
    public static User getCurrentUser()
    {
        final UserDetailsImpl userDetailsImpl = getCurrentUserDetails();
        return userDetailsImpl != null ? userDetailsImpl.getUser() : null;
    }

    /**
     * Returns UserDetails object of the currently authenticated person.
     * @return Currently authenticated person UserDetailsImpl or null, if no one is currently authenticated
     */
    public static UserDetailsImpl getCurrentUserDetails()
    {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() instanceof UserDetailsImpl)
        {
            return (UserDetailsImpl) context.getAuthentication().getPrincipal();

        }
        else
        {
            return null;
        }
    }

    /**
     * Creates an authentication token based on the specified user details and sets it to the current thread's security
     * context.
     * @param userDetailsImpl Details of the user to set as current
     * @return The generated authentication token
     */
    public static AuthenticationToken setCurrentUser(UserDetailsImpl userDetailsImpl)
    {
        final AuthenticationToken token = new AuthenticationToken(userDetailsImpl.getAuthorities(), userDetailsImpl);
        token.setAuthenticated(true);

        final SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        return token;
    }

    /**
     * Checks whether the current authentication token represents an anonymous user.
     * @return Whether current authentication is anonymous
     */
    public static boolean isAuthenticatedAnonymously()
    {
        return getCurrentUserDetails() == null;
    }
}
