package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.config.OtherConstants;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
//@Table(name = "tokens")
//@DiscriminatorColumn(name = "token_type",discriminatorType = DiscriminatorType.INTEGER)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Token extends AbstractEntity
{
    @Column
    protected String token;

    @Column
    protected Date createdDate;

    @Column
    protected Boolean isValid;

    @OneToOne
    protected User user;

    public Token(User user)
    {
        this.user = user;
        this.isValid = true;
        this.createdDate = new Date();
        this.token = UUID.randomUUID().toString();
    }

    public Token()
    {
        this.createdDate = new Date();
        this.isValid = true;
        this.token = UUID.randomUUID().toString();
    }

    public void invalidateToken()
    {
        setValid(false);
        setCreatedDate(new Date(this.createdDate.getTime() - OtherConstants.PASSWORD_RESET_TOKEN_EXPIRE));
    }

    public boolean isTokenValid()
    {
        if (!isValid)
            return false;

        final long expireTime = OtherConstants.PASSWORD_RESET_TOKEN_EXPIRE;
        final long tempDateLong = this.createdDate.getTime() + expireTime;
        final Date expireDate = new Date(tempDateLong);
        final Date today = new Date();

        //System.err.println("Time: "+expireTime);
        //System.err.println(tempDateLong +" -> exp: "+ expireDate);
        //System.err.println(today.getTime() +" -> today: "+ today);

        return !today.after(expireDate);
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public Boolean getValid()
    {
        return isValid;
    }

    public void setValid(Boolean valid)
    {
        isValid = valid;
    }
}
