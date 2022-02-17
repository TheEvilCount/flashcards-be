package cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto;

import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import lombok.Data;

@Data
public class CollectionDTO
{
    private Long id;
    private String title;
    private String collectionColor;
    private long counterFav;
    private long counterDup;
    private int cardNum;
    private CardCollectionVisibility visibility;
    //private Date creationDate;
    //private List<Card> cardList;
    private String category;
    //private User owner;
}
