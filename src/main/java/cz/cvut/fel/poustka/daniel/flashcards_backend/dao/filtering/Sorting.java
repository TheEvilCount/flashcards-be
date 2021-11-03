package cz.cvut.fel.poustka.daniel.flashcards_backend.dao.filtering;

public class Sorting
{
    private final OrderType orderType;
    private final String columnToOrderBy;

    public Sorting(OrderType orderType, String columnToOrderBy)
    {
        this.orderType = orderType;
        this.columnToOrderBy = columnToOrderBy;
    }

    public Sorting()
    {
        this.orderType = OrderType.ASCENDING;
        this.columnToOrderBy = "id";
    }

    public OrderType getOrderType()
    {
        return orderType;
    }

    public String getColumnToOrderBy()
    {
        return columnToOrderBy;
    }
}
