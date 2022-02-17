package cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto;

import lombok.Data;

@Data
public class CollectionUpdateDTO
{
    private Long id;
    private String title;
    private String collectionColor;
    private String category;
}
