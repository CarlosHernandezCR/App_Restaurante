package model.XML;

import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({ @NamedQuery(name = "HQL_GET_ALL_ORDERITEMS",
        query = "from OrderItem") })
public class OrderItem {
    @XmlElement(name = "menu_item")
    private Integer menuItem;

    @XmlElement
    private Integer quantity;

    public OrderItem() {
    }

    public OrderItem(Integer menuItem, Integer quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public Integer getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(Integer menuItem) {
        this.menuItem = menuItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
