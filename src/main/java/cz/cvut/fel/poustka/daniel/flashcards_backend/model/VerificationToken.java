package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.config.OtherConstants;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Date;

@Entity
//@Table(name = "tokens")
//@DiscriminatorColumn(name = "token_type")
//@DiscriminatorValue(value = "1")
@NamedQueries({
        @NamedQuery(name = "VerificationToken.findByToken", query = "SELECT t FROM VerificationToken t WHERE t.token = :token")
})
public class VerificationToken extends Token
{

    public VerificationToken(User user)
    {
        super(user);
    }

    public VerificationToken()
    {
        super();
    }

    @Override
    public boolean isTokenValid()
    {
        final long expireTime = OtherConstants.VERIFICATION_TOKEN_EXPIRE;
        final long tempDateLong = this.createdDate.getTime() + expireTime;
        final Date expireDate = new Date(tempDateLong);
        final Date today = new Date();

        //System.err.println("Time: "+expireTime);
        //System.err.println(tempDateLong +" -> exp: "+ expireDate);
        //System.err.println(today.getTime() +" -> today: "+ today);

        return !today.after(expireDate);
    }
}
