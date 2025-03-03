package dao.impl.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Projections.*;
import static java.util.Arrays.asList;
import static com.mongodb.client.model.Sorts.*;


public class AggregationsDAO {

    private final MongoCollection<Document> collectionMenuItems;
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> collection2;

    public AggregationsDAO() {
        MongoClient mongo = MongoClients.create("mongodb://root:root@localhost:27017/");
        MongoDatabase db = mongo.getDatabase("CarlosHernandez_Restaurant");
        MongoDatabase db2 = mongo.getDatabase("CarlosHernandez_Aviones");
        collection2 = db2.getCollection("aviones");

        collection = db.getCollection("customers");
        collectionMenuItems = db.getCollection("menu_items");
    }

    public ArrayList<Document> a() {
        return collectionMenuItems.aggregate(asList(
                        project(fields(include("name"), excludeId())),
                        sort(descending("price")),
                        limit(1)
                )
        ).into(new ArrayList<>());
    }


    public ArrayList<Document> b(String customerId) {
        return collection.aggregate(asList(
                match(Filters.eq("_id", new org.bson.types.ObjectId(customerId))),
                project(new Document("first_name", 1L)
                        .append("_id", 0L)
                        .append("orders.table_id", 1L))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> c() {
        return collection.aggregate(asList(
                unwind("$orders"),
                addFields(new Field<>("nItems", new Document("$size", "$orders.order_items"))),
                project(fields(include("nItems"), excludeId()))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> d() {
        return collection.aggregate(asList(
                match(eq("orders.order_items.menu_item_id", 5L)),
                project(fields(include("first_name"), excludeId()))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> e() {
        return collection.aggregate(asList(
                unwind("$orders"),
                addFields(new Field("nItems", new Document("$sum", "$orders.order_items.quantity"))),
                group(null, avg("average", "$nItems")),
                project(fields(excludeId()))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> f() {
        return collection.aggregate(asList(
                unwind("$orders"),
                unwind("$orders.order_items"),
                group("$orders.order_items.menu_item_id", sum("totalRequested", 1L)),
                addFields(new Field("menu_item_id", "$_id"), new Field("totalRequested", "$totalRequested")),
                sort(descending("totalRequested")),
                limit(1),
                project(fields(excludeId(), include("menu_item_id")))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> g(String customerId) {
        return collection.aggregate(asList(
                match(eq("_id", new org.bson.types.ObjectId(customerId))),
                unwind("$orders"),
                unwind("$orders.order_items"),
                group("$orders.order_items.menu_item_id", sum("totalRequested", "$orders.order_items.quantity"))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> h() {
        return collection.aggregate(asList(
                unwind("$orders"),
                group("$orders.table_id", sum("ordersTable", 1L)),
                sort(descending("ordersTable")),
                limit(2),
                project(fields(include("_id", "ordersTable")))
        )).into(new ArrayList<>());
    }


    public ArrayList<Document> i() {
        return collection.aggregate(Arrays.asList(new Document("$unwind",
                        new Document("path", "$orders")),
                new Document("$group", new Document("_id", new Document("idCust", "$_id").append("idTable", "$orders.table_id")).append("totalOrders", new Document("$sum", 1L))),
                new Document("$sort", new Document("_id.idCust", 1L).append("_id.idTable", -1L)),
                new Document("$group", new Document("_id", "$_id.idCust").append("most_requested_table", new Document("$first", "$_id.idTable"))))
        ).into(new ArrayList<>());
    }


    public ArrayList<Document> j() {
        return collection.aggregate(asList(
                unwind("$orders"),
                unwind("$orders.order_items"),
                group("$orders.order_items.menu_item_id", sum("totalRequested", 1L)),
                match(eq("totalRequested", 1L))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> k() {
        return collection.aggregate(Arrays.asList(
                unwind("$orders"),
                unwind("$orders.order_items"),
                lookup("menu_items", "orders.order_items.menu_item_id", "_id", "menu_item"),
                unwind("$menu_item"),
                group("$orders.order_date", sum("total_price", new Document("$multiply", Arrays.asList("$orders.order_items.quantity", "$menu_item.price"))))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> l() {
        return collection.aggregate(Arrays.asList(
                unwind("$orders"),
                unwind("$orders.order_items"),
                lookup("menu_items", "orders.order_items.menu_item_id", "_id", "menu_item"),
                unwind("$menu_item"),
                group("$first_name", sum("total", new Document("$multiply", Arrays.asList("$orders.order_items.quantity", "$menu_item.price")))),
                sort(descending("total")),
                limit(1),
                project(exclude("total"))
        )).into(new ArrayList<>());
    }

    public ArrayList<Document> m() {
        return collection.aggregate(Arrays.asList(
                unwind("$orders"),
                unwind("$orders.order_items"),
                lookup("menu_items", "orders.order_items.menu_item_id", "_id", "menu_item"),
                unwind("$menu_item"),
                group("$_id", sum("total_spent",
                        new Document("$multiply",
                                Arrays.asList("$orders.order_items.quantity", "$menu_item.price"))))
        )).into(new ArrayList<>());
    }

    // proporcionará los fabricantes con la menor cantidad de tipos de armamento, junto con la longitud de la lista de armamentos y el conteo total de armamentos.
    public ArrayList<Document> aa() {
        return collection2.aggregate(Arrays.asList(
                unwind("$armamento"),
                group("$fabricante", sum("totalArmament", 1)),
                addFields(new Field<>("totalArmamentString", new Document("$toString", "$totalArmament"))),
                addFields(new Field<>("totalArmamentLength", new Document("$strLenCP", "$totalArmamentString"))),
                sort(ascending("totalArmamentLength")),
                limit(3),
                project(fields(include("_id", "totalArmamentLength", "totalArmament")))
        )).into(new ArrayList<>());
    }
    //Aeronaves con el costo unitario ajustado por la inflación:
    public ArrayList<Document> bb() {
        return collection2.aggregate(asList(
                addFields(new Field<>("costo_unitario_ajustado_usd",
                        new Document("$multiply", asList("$costo_unitario_usd", 1.1)))),
                project(fields(excludeId(), include("modelo", "costo_unitario_ajustado_usd"))),
                sort(descending("costo_unitario_ajustado_usd"))
        )).into(new ArrayList<>());
    }
    //Promedio de capacidad de pasajeros por fabricante:
    public ArrayList<Document> cc() {
        return collection2.aggregate(Arrays.asList(
                unwind("$fabricante"),
                group("$fabricante", avg("promedio_capacidad_pasajeros", "$capacidad_pasajeros")),
                sort(ascending("_id"))
        )).into(new ArrayList<>());
    }
    //fabricante junto con el número total de armamentos para cada uno
    public ArrayList<Document> dd() {
            return collection2.aggregate(Arrays.asList(
                    unwind("$armamento"),
                    group("$fabricante", sum("totalArmament", 1)),
                    project(fields(include("_id", "totalArmament")))
            )).into(new ArrayList<>());
    }
    //Esta agregación devuelve el costo total de las aeronaves comerciales.
    public ArrayList<Document> ee() {
        return collection2.aggregate(Arrays.asList(
                match(eq("tipo", "comercial")),
                addFields(new Field<>("totalCost",
                        new Document("$multiply",
                                Arrays.asList("$capacidad_pasajeros", "$costo_unitario_usd")))),
                group("$tipo", sum("totalCost", "$totalCost")),
                sort(ascending("_id")),
                project(fields(include("_id", "totalCost")))
        )).into(new ArrayList<>());
    }

}
