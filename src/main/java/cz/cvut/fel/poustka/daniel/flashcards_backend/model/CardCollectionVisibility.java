package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import java.util.Arrays;

public enum CardCollectionVisibility
{
    /**
     * private - private; public - public; lined - only marked as favourite
     */
    PRIVATE(0), PUBLIC(1), LINKED(2);

    private Integer value;

    CardCollectionVisibility(Integer value)
    {
        this.value = value;
    }

    public static CardCollectionVisibility getByValue(Integer value)
    {
        return Arrays.stream(CardCollectionVisibility.values())
                .filter(visibility -> value.equals(visibility.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such visibility constant"));
    }

    public Integer getValue()
    {
        return value;
    }
}
