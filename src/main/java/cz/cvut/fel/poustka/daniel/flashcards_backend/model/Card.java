package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

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

    @ManyToOne
    private CardCollection collection;

    public Card(String frontText, String backText, CardCollection collection)
    {
        this.frontText = frontText;
        this.backText = backText;
        this.collection = collection;
    }

    public Card()
    {
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
