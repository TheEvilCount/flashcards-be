package cz.cvut.fel.poustka.daniel.flashcards_backend.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
@PreAuthorize("permitAll()")
public class CardController
{
    //getbyid

    //??? here or in collection??
}
