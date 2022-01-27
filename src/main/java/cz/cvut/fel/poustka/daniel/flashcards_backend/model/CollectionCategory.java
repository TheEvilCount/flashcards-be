package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CollectionCategory extends AbstractEntity
{
    @Column(nullable = false)
    private String title;

    @JsonBackReference
    @OneToMany(mappedBy = "category")
    private List<CardCollection> collectionList;


    public CollectionCategory(String title, List<CardCollection> collectionList)
    {
        this.title = title;
        if (collectionList != null)
            this.collectionList = collectionList;
        else
            this.collectionList = new ArrayList<>();
    }

    public CollectionCategory()
    {
        this.collectionList = new ArrayList<>();
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<CardCollection> getCollectionList()
    {
        return collectionList;
    }

    public void setCollectionList(List<CardCollection> collectionList)
    {
        this.collectionList = collectionList;
    }

}
