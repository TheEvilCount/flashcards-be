package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class LinkedCollection extends AbstractEntity
{

    @JsonIgnore
    @ManyToOne()
    private CardCollection cardCollection;

    @JsonIgnore
    @ManyToOne()
    private User user;

    public LinkedCollection()
    {
    }

    public LinkedCollection(CardCollection cardCollection, User user)
    {
        this.cardCollection = cardCollection;
        this.user = user;
    }

    public CardCollection getCardCollection()
    {
        return cardCollection;
    }

    public void setCardCollection(CardCollection cardCollection)
    {
        this.cardCollection = cardCollection;
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
