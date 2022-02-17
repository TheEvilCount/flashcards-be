package cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto;

import lombok.Data;

@Data
public class RegisterDTO
{
    private String username;
    private String password;
    private String email;
}
