package cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto;

import lombok.Data;

@Data
public class CardDTO
{
    private String frontText;
    private String backText;
}
