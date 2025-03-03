package model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "order_item")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NamedQueries({
        @NamedQuery(name = "HQL_GET_ALL_ORDERITEMS", query = "from OrderItem")
})
public class OrderItem {
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
    private Integer id;

    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @XmlElement
    private Integer quantity;
}