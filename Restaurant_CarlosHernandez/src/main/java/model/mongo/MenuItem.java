package model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem {
    int id;
    String name;
    String description;
    double price;

    public MenuItem(Document customerDocument) {
        id = customerDocument.getInteger("_id");
        name = customerDocument.getString("name");
        description = customerDocument.getString("description");
        price = customerDocument.getDouble("price");
    }
}
