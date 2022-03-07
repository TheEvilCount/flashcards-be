package cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto;

import lombok.Data;

@Data
public class CollectionCreateDTO
{
    private String title;
    private String collectionColor;
    private String visibility;
    private String category;
}
