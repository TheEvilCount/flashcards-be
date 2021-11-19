package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import cz.cvut.fel.poustka.daniel.flashcards_backend.config.OtherConstants;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NamedQueries({
        @NamedQuery(name = "VerificationToken.findByToken", query = "SELECT v FROM VerificationToken v WHERE v.token = :token")
})
public class VerificationToken extends AbstractEntity
{
    @Column
    private String token;

    @Column
    private Date createdDate;


    @OneToOne//(targetEntity = User.class, fetch = FetchType.EAGER)
    //@JoinColumn(nullable = false, name = "id")
    private User user;

    public VerificationToken(User user)
    {
        this.user = user;
        this.createdDate = new Date();
        this.token = UUID.randomUUID().toString();
    }

    public VerificationToken()
    {
        this.createdDate = new Date();
        this.token = UUID.randomUUID().toString();
    }

    public boolean isTokenExpired()
    {
        return new Date().getTime() < this.createdDate.getTime() + OtherConstants.VERIFICATION_TOKEN_EXPIRE;
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

}
