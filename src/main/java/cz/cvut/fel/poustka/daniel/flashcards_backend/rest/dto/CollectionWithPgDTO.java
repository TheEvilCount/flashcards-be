package cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class CollectionWithPgDTO
{
    private Integer maxPages;
    private Integer page;
    private Integer pageSize;
    private List<CollectionDTO> collections;
}
