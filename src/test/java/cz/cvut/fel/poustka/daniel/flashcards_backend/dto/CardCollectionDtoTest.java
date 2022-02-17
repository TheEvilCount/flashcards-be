package cz.cvut.fel.poustka.daniel.flashcards_backend.dto;

import cz.cvut.fel.poustka.daniel.flashcards_backend.environment.Generator;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollection;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.CardCollectionVisibility;
import cz.cvut.fel.poustka.daniel.flashcards_backend.rest.dto.CollectionDTO;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardCollectionDtoTest
{
    private ModelMapper modelMapper = new ModelMapper();

    @Test
    public void whenConvertCardCollectionEntityToCollectionDto_thenCorrect()
    {
        CardCollection collection = new CardCollection();
        collection.setId(1L);
        collection.setTitle(Generator.randomString(6));
        collection.setCollectionColor("red");
        collection.setVisibility(CardCollectionVisibility.PRIVATE);

        CollectionDTO collectionDTO = modelMapper.map(collection, CollectionDTO.class);
        assertEquals(collection.getId(), collectionDTO.getId());
        assertEquals(collection.getTitle(), collectionDTO.getTitle());
        assertEquals(collection.getCollectionColor(), collectionDTO.getCollectionColor());
    }

    @Test
    public void whenConvertCollectionDtoToCardCollectionEntity_thenCorrect()
    {
        CollectionDTO collectionDTO = new CollectionDTO();
        collectionDTO.setId(1L);
        collectionDTO.setTitle(Generator.randomString(6));
        collectionDTO.setCollectionColor("red");

        CardCollection collection = modelMapper.map(collectionDTO, CardCollection.class);
        assertEquals(collectionDTO.getId(), collection.getId());
        assertEquals(collectionDTO.getTitle(), collection.getTitle());
        assertEquals(collectionDTO.getCollectionColor(), collection.getCollectionColor());
    }
}