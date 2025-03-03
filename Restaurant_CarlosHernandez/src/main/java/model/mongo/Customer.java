package model.mongo;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;

import org.bson.Document;
import org.bson.types.ObjectId;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Customer {
    private ObjectId id;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private LocalDate dob;

    private List<Order> orders=new ArrayList<>();

    public Customer(String first_name, String last_name, String email, String phone, LocalDate dob) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
    }

    public Customer(ObjectId id,String first_name, String last_name, String email, String phone, Date dob) {
        this.id=id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.phone = phone;
        Instant instant = dob.toInstant();
        LocalDateTime dobDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        this.dob = dobDateTime.toLocalDate();
    }
    public Customer(Document customerDocument) {
        id = customerDocument.getObjectId("_id");
        first_name = customerDocument.getString("first_name");
        last_name = customerDocument.getString("last_name");
        email = customerDocument.getString("email");
        phone = customerDocument.getString("phone");
        Date dobDate = customerDocument.getDate("dob");
        if (dobDate != null) {
            Instant instant = dobDate.toInstant();
            LocalDateTime dobDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            dob = dobDateTime.toLocalDate();
        }
    }

    public Customer(String firstName, String lastName, String email, String phone, LocalDate dob, List<Order> ordersMongo) {
        this.first_name = firstName;
        this.last_name = lastName;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.orders=ordersMongo;
    }

    //{
//  "_id": ObjectId("customer_id"),
//  "first_name": "John",
//  "last_name": "Doe",
//  "email": "john.doe@example.com",
//  "phone": "123-456-7890",
//  "dob": ISODate("1990-01-01"),
//  "orders": [
//    {
//      "_id": ObjectId("order_id_1"),
//      "dateOrder": ISODate("2024-01-01T12:00:00Z"),
//      "idTable": 1,
//      "orderItems": [
//        {
//          "menuItem": {
//            "id": ObjectId("menu_item_id_1"),
//            "name": "Burger",
//            "price": 10.99
//          },
//          "quantity": 2
//        },
//        // Más elementos de la orden...
//      ]
//    },
//    // Más órdenes...
//  ]
//}
}