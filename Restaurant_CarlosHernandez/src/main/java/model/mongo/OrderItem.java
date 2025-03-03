package model.mongo;

import lombok.*;
import org.bson.types.ObjectId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderItem {
    private int menuItemId;
    private int quantity;

    public OrderItem(model.OrderItem orderItem) {
        this.menuItemId= orderItem.getId();
        this.quantity= orderItem.getQuantity();
    }
}
