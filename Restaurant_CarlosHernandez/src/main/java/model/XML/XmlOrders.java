package model.XML;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.mongo.Order;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "orders")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlOrders {

    @XmlElement(name = "order")
    private List<Order> orders;
}


