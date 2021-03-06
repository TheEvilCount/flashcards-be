package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class CardCollection extends AbstractEntity
{
    @Column(nullable = false)
    private String title;

    @Column
    private String collectionColor;

    @Column
    private long counterFav;

    @Column
    private long counterDup;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private CardCollectionVisibility visibility;

    @Column(nullable = false)
    private Date creationDate;

    @JsonIgnore
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> cardList;

    @ManyToOne
    private CollectionCategory category;

    @JsonIgnore
    @ManyToOne
    private User owner;

    @JsonBackReference
    @OneToMany(mappedBy = "cardCollection", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<LinkedCollection> linkedCollectionList;


    public CardCollection(User owner, String title, CardCollectionVisibility visibility, CollectionCategory category, String collectionColor, List<Card> cardList, List<LinkedCollection> linkedCollectionList)
    {
        this.owner = owner;
        this.title = title;
        this.visibility = visibility;
        this.category = category;
        this.collectionColor = collectionColor;

        this.counterDup = 0;
        this.counterFav = 0;

        if (cardList != null)
            this.cardList = cardList;
        else
            cardList = new ArrayList<>();

        if (linkedCollectionList != null)
            this.linkedCollectionList = linkedCollectionList;
        else
            linkedCollectionList = new ArrayList<>();

    }

    public CardCollection(User owner, String title, CardCollectionVisibility visibility, CollectionCategory category, String collectionColor)
    {
        this.owner = owner;
        this.title = title;
        this.visibility = visibility;
        this.category = category;
        this.collectionColor = collectionColor;

        this.counterDup = 0;
        this.counterFav = 0;
    }

    public CardCollection()
    {
        this.cardList = new ArrayList<>();
        this.linkedCollectionList = new ArrayList<>();
        this.collectionColor = "";

        this.counterDup = 0;
        this.counterFav = 0;
    }

    public CardCollection(CardCollection cardCollection)
    {
        //this.setId(null);
        this.title = cardCollection.getTitle();
        this.collectionColor = cardCollection.getCollectionColor();
        this.owner = cardCollection.getOwner();
        this.visibility = cardCollection.getVisibility();
        this.category = cardCollection.getCategory();

        this.creationDate = java.sql.Date.valueOf(LocalDate.now());

        this.linkedCollectionList = new ArrayList<>();//cardCollection.getLinkedCollectionList();

        this.cardList = new ArrayList<>();
        //cardList.addAll(cardCollection.getCardList());
        cardCollection.getCardList().forEach(card -> this.cardList.add(card.duplicate(this)));

        this.counterFav = 0;
        this.counterDup = 0;
    }

    public Card getCardById(Long cardId)
    {
        return this.cardList.stream().filter(card -> card.getId() == cardId).findFirst().orElse(null);
    }

    public CardCollection duplicate()
    {
        return new CardCollection(this);
    }

    public void AddCounterDup()
    {
        this.counterDup++;
    }

    public void AddCounterFav()
    {
        this.counterFav++;
    }

    public void SubstractCounterFav()
    {
        this.counterFav--;
    }

    //Getters & Setters

    public Integer getCardCount()
    {
        if (this.cardList == null)
            this.cardList = new ArrayList<>();
        return this.cardList.size();
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public CardCollectionVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(CardCollectionVisibility visibility)
    {
        this.visibility = visibility;
    }

    public List<Card> getCardList()
    {
        return cardList;
    }

    public void setCardList(List<Card> cardList)
    {
        this.cardList = cardList;
    }

    public CollectionCategory getCategory()
    {
        return category;
    }

    public void setCategory(CollectionCategory category)
    {
        this.category = category;
    }

    public String getCollectionColor()
    {
        return collectionColor;
    }

    public void setCollectionColor(String collectionColor)
    {
        this.collectionColor = collectionColor;
    }

    public User getOwner()
    {
        return owner;
    }

    public void setOwner(User owner)
    {
        this.owner = owner;
    }

    public List<LinkedCollection> getLinkedCollectionList()
    {
        return linkedCollectionList;
    }

    public void setLinkedCollectionList(List<LinkedCollection> linkedCollectionList)
    {
        this.linkedCollectionList = linkedCollectionList;
    }

    public long getCounterFav()
    {
        return counterFav;
    }

    public void setCounterFav(long counterFav)
    {
        this.counterFav = counterFav;
    }

    public long getCounterDup()
    {
        return counterDup;
    }

    public void setCounterDup(long counterDup)
    {
        this.counterDup = counterDup;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }
}
