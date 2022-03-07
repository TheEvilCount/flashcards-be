package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.config.OtherConstants;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Date;

@Entity
//@Table(name = "tokens")
//@DiscriminatorColumn(name = "token_type")
//@DiscriminatorValue(value = "2")
@NamedQueries({
        @NamedQuery(name = "PasswordResetToken.findByToken", query = "SELECT t FROM PasswordResetToken t WHERE t.token = :token")
})
public class PasswordResetToken extends Token
{
    public PasswordResetToken(User user)
    {
        super(user);
    }

    public PasswordResetToken()
    {
        super();
    }

    @Override
    public boolean isTokenValid()
    {
        final long expireTime = OtherConstants.PASSWORD_RESET_TOKEN_EXPIRE;
        final long tempDateLong = this.createdDate.getTime() + expireTime;
        final Date expireDate = new Date(tempDateLong);
        final Date today = new Date();
        return !today.after(expireDate);
    }

}
