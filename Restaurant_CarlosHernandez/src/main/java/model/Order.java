package model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
@XmlRootElement(name = "order")
@XmlAccessorType(XmlAccessType.FIELD)
@ToString(exclude = "orderItems")
@NamedQuery(name = "HQL_GET_ALL_ORDERS",
        query = "from Order ")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;
    @Column(name = "order_date")
    @XmlTransient
    private LocalDateTime dateOrder;
    @Column(name = "customer_id")
    @XmlTransient
    private Integer idCustomer;
    @Column(name = "table_id")
    @XmlTransient
    private Integer idTable;
    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "orderItems")
    @XmlElement(name = "order_item")
    private List<OrderItem> orderItems;

    public Order(int id, LocalDateTime date, int idCust, int idTable) {
        this.id=id;
        dateOrder=date;
        idCustomer=idCust;
        this.idTable=idTable;
        orderItems=new ArrayList<>();
    }
}
