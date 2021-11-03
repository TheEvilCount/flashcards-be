package cz.cvut.fel.poustka.daniel.flashcards_backend.service.handler;

/**
 * Contains information about an error and can be send to client as JSON to let them know what went wrong.
 */
public class ErrorInfo
{

    private String errorMessage;

    private String requestUri;

    public ErrorInfo(String errorMessage, String requestUri)
    {
        this.errorMessage = errorMessage;
        this.requestUri = requestUri;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public void setRequestUri(String requestUri)
    {
        this.requestUri = requestUri;
    }

    @Override
    public String toString()
    {
        return "ErrorInfo{" + requestUri + ", errorMessage = " + errorMessage + "}";
    }
}
