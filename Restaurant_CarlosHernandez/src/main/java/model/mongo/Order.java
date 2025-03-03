package model.mongo;

import lombok.*;
import model.MenuItem;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Order {
    private LocalDateTime orderDate;
    private int tableId;
    private List<OrderItem> order_items;

    public Order(Document orderDoc) {
        String dateString = orderDoc.getString("order_date");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        this.orderDate = LocalDateTime.parse(dateString, formatter);

        this.tableId = orderDoc.getInteger("table_id");

        List<Document> orderItemDocs = (List<Document>) orderDoc.get("order_items");
        this.order_items = new ArrayList<>();
        for (Document orderItemDoc : orderItemDocs) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(orderItemDoc.getInteger("menu_item_id"));
            orderItem.setQuantity(orderItemDoc.getInteger("quantity"));
            order_items.add(orderItem);
        }
    }

    public Order(model.Order order) {
        this.orderDate=order.getDateOrder();
        this.tableId=order.getIdTable();
        this.order_items=transformToMongo(order.getOrderItems());
    }

    private List<OrderItem> transformToMongo(List<model.OrderItem> orderItems) {
        List<OrderItem> orderItemList=new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            orderItemList.add(new OrderItem(orderItems.get(i)));
        }
        return orderItemList;
    }
}
