package cz.cvut.fel.poustka.daniel.flashcards_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Toolbox
{
    public static boolean isStringValidJSON(String jsonObject)
    {
        try
        {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonObject);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
