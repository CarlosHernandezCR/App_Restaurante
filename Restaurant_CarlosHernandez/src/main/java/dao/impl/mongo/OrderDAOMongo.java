package dao.impl.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import common.config.Configuration;
import common.constants.Constants;
import common.constants.ConstantsErrors;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.error.Error;
import model.mongo.Order;
import model.mongo.OrderItem;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class OrderDAOMongo {

    private final String connectionString;
    private final String databaseName;
    private final String collectionName;

    @Inject
    public OrderDAOMongo(Configuration configuration) {
        this.connectionString = configuration.getProperty(Constants.MONGO_CONNECTION_STRING);
        this.databaseName = configuration.getProperty(Constants.DATABASENAME);
        this.collectionName = configuration.getProperty(Constants.CUSTOMERS);
    }

    public Either<Error, List<Order>> getAll() {
        Either<Error, List<Order>> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);

            List<Order> orders = new ArrayList<>();
            for (Document customerDoc : customersCollection.find()) {
                List<Document> mongoOrders = (List<Document>) customerDoc.get("orders");
                if (mongoOrders != null) {
                    for (Document mongoOrder : mongoOrders) {
                        Order order = new Order(mongoOrder);
                        orders.add(order);
                    }
                }
            }

            result = Either.right(orders);
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDER));
        }
        return result;
    }


    public Either<Error, List<Order>> getAll(ObjectId customerId) {
        Either<Error, List<Order>> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);

            Document query = new Document("_id", customerId);
            Document customerDoc = customersCollection.find(query).first();
            if (customerDoc == null) {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
                return result;
            }

            List<Order> orders = new ArrayList<>();
            List<Document> ordersList = (List<Document>) customerDoc.get("orders");
            if (ordersList != null) {
                for (Document orderDoc : ordersList) {
                    Order order = new Order(orderDoc);
                    orders.add(order);
                }
            }
            result = Either.right(orders);
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDER));
        }
        return result;
    }

    public Either<Error, Order> get(LocalDateTime orderDate) {
        Either<Error, Order> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection("customers");
            String dateString = orderDate.toString();
            Document query = new Document("orders.order_date", dateString);
            Document customerDoc = customersCollection.find(query).first();

            if (customerDoc == null) {
                return Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.ORDERNOEXIST));
            }

            List<Document> ordersList = (List<Document>) customerDoc.get("orders");
            Document orderDoc = ordersList.stream()
                    .filter(order -> dateString.equals(order.getString("order_date")))
                    .findFirst()
                    .orElse(null);
            if (orderDoc == null) {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.ORDERNOEXIST));
            } else {
                Order order = new Order(orderDoc);
                result = Either.right(order);
            }
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDER));
        }
        return result;
    }


    public Either<Error, Integer> add(Order order, ObjectId customerId) {
        Either<Error, Integer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);
            Document query = new Document("_id", customerId);
            Document customerDoc = customersCollection.find(query).first();
            if (customerDoc == null) {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
                return result;
            }

            Document orderDoc = new Document();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            String dateString = order.getOrderDate().format(formatter);
            orderDoc.append("order_date", dateString)
                    .append("table_id", order.getTableId());
            List<Document> orderItems = new ArrayList<>();
            for (OrderItem item : order.getOrder_items()) {
                Document orderItemDoc = new Document();
                orderItemDoc.append("menu_item_id", item.getMenuItemId())
                        .append("quantity", item.getQuantity());
                orderItems.add(orderItemDoc);
            }
            orderDoc.append("order_items", orderItems);
            List<Document> ordersList = (List<Document>) customerDoc.get("orders");
            ordersList.add(orderDoc);
            customerDoc.put("orders", ordersList);
            customersCollection.replaceOne(query, customerDoc);
            result = Either.right(order.getOrder_items().size() + 1);
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDER));
        }
        return result;
    }


    public Either<Error, Integer> delete(LocalDateTime orderDate) {
        Either<Error, Integer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);
            Document customerQuery = new Document("orders.order_date", orderDate.toString());
            Document customerDoc = customersCollection.find(customerQuery).first();

            if (customerDoc != null) {
                List<Document> ordersList = (List<Document>) customerDoc.get("orders");
                ordersList.removeIf(orderDoc -> orderDoc.getString("order_date").equals(orderDate.toString()));
                Document updateQuery = new Document("_id", customerDoc.getObjectId("_id"));
                Document updateDoc = new Document("$set", new Document("orders", ordersList));
                customersCollection.updateOne(updateQuery, updateDoc);
                result = Either.right(1);
            } else {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.ORDERNOEXIST));
            }
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORORDER));
        }
        return result;
    }


    public Either<Error, Integer> update(Order updatedOrder) {
        Either<Error, Integer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection("customer");
            collection.updateOne
                    (eq("orders.order_date", updatedOrder.getOrderDate().toString()),
                            set("table_id", updatedOrder.getTableId()));
            collection.updateOne
                    (eq("orders.order_date", updatedOrder.getOrderDate().toString()),
                            set("order_items", updatedOrder.getOrder_items()));
            collection.updateOne
                    (eq("orders.order_date", updatedOrder.getOrderDate().toString()),
                            set("orders.order_date", LocalDateTime.now()));
            result = Either.right(1);
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORORDER));
        }
        return result;
    }
}
