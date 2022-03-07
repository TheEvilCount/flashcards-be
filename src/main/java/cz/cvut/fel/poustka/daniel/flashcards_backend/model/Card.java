package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Card extends AbstractEntity
{
    @Column(nullable = false)
    private String frontText;

    @Column(nullable = false)
    private String backText;

    @JsonBackReference
    @ManyToOne
    private CardCollection collection;

    public Card()
    {
    }

    public Card duplicate(CardCollection collection)
    {
        Card newCard = new Card();
        newCard.setId(null);
        newCard.setFrontText(this.getFrontText());
        newCard.setBackText(this.getBackText());
        newCard.setCollection(collection);
        return newCard;
    }

    public String getFrontText()
    {
        return frontText;
    }

    public void setFrontText(String frontText)
    {
        this.frontText = frontText;
    }

    public String getBackText()
    {
        return backText;
    }

    public void setBackText(String backText)
    {
        this.backText = backText;
    }

    public CardCollection getCollection()
    {
        return collection;
    }

    public void setCollection(CardCollection collection)
    {
        this.collection = collection;
    }
}
