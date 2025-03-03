package model;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu_items")
@NoArgsConstructor
@AllArgsConstructor
@Data
@XmlRootElement(name = "menu_item")
@XmlAccessorType(XmlAccessType.FIELD)
public class MenuItem {
    @Id
    @Column(name = "menu_item_id")
    @XmlTransient
    private Integer id;
    private String name;
    @XmlTransient
    private String description;
    @XmlTransient
    private Double price;
}
