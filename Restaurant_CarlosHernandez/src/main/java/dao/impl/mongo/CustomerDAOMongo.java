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
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import model.XML.XmlOrders;
import model.error.Error;
import model.mongo.Customer;
import model.mongo.Order;
import model.mongo.OrderItem;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;


public class CustomerDAOMongo {
    private final String connectionString;
    private final String databaseName ;
    private final String collectionName;
    private final String collectionName2;

    @Inject
    public CustomerDAOMongo(Configuration configuration) {
        this.connectionString = configuration.getProperty(Constants.MONGO_CONNECTION_STRING);
        this.databaseName = configuration.getProperty(Constants.DATABASENAME);
        this.collectionName = configuration.getProperty(Constants.CUSTOMERS);
        this.collectionName2 = configuration.getProperty(Constants.CREDENTIALS);
    }
    public Either<Error, List<Customer>> getAll() {
        Either<Error, List<Customer>> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);

            List<Customer> customers = new ArrayList<>();
            for (Document customerDocument : customersCollection.find()) {
                customers.add(new Customer(customerDocument));
            }

            result= Either.right(customers);
        } catch (Exception e) {
            result= Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        }return result;
    }

    public Either<Error, Customer> get(ObjectId id) {
        Either<Error, Customer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);

            Document customerDocument = customersCollection.find(eq("id", id)).first();
            if (customerDocument == null) {
                result= Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
            } else {
                Customer customer = new Customer(id,
                        customerDocument.getString("first_name"),
                        customerDocument.getString("last_name"),
                        customerDocument.getString("email"),
                        customerDocument.getString("phone"),
                        customerDocument.getDate("dob")
                );
                result= Either.right(customer);
            }
        } catch (Exception e) {
            result= Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        }return result;
    }

    public Either<Error, Integer> add(Customer c, String username, String password) {
        Either<Error, Integer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> credentialsCollection = database.getCollection(collectionName2);
            Document credentialsDocument = new Document("user_name", username)
                    .append("password", password);
            credentialsCollection.insertOne(credentialsDocument);
            ObjectId credentialsId = credentialsDocument.getObjectId("_id");
            c.setId(credentialsId);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);
            Document customerDocument = new Document("_id", c.getId())
                    .append("first_name", c.getFirst_name())
                    .append("last_name", c.getLast_name())
                    .append("email", c.getEmail())
                    .append("phone", c.getPhone())
                    .append("dob", c.getDob());
            if (!c.getOrders().isEmpty()) {
                List<Document> orderDocuments = new ArrayList<>();
                for (Order order : c.getOrders()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    String dateString = order.getOrderDate().format(formatter);
                    Document orderDocument = new Document("order_date", dateString)
                            .append("table_id", order.getTableId());
                    List<Document> orderItemDocuments = new ArrayList<>();
                    for (OrderItem orderItem : order.getOrder_items()) {
                        Document orderItemDocument = new Document("menu_item_id", orderItem.getMenuItemId())
                                .append("quantity", orderItem.getQuantity());
                        orderItemDocuments.add(orderItemDocument);
                    }
                    orderDocument.append("order_items", orderItemDocuments);

                    orderDocuments.add(orderDocument);
                }
                customerDocument.append("orders", orderDocuments);
            } else {
                customerDocument.append("orders", new ArrayList<Document>());
            }
            customersCollection.insertOne(customerDocument);

            result = Either.right(2);
        } catch (Exception ex) {
            result = Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGERRORCUSTOMER));
        }
        return result;
    }



    public Either<Error, Integer> update(Customer c) {
        Either<Error, Integer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customersCollection = database.getCollection(collectionName);
            Document updatedCustomerDocument = new Document();
            updatedCustomerDocument.append("first_name", c.getFirst_name())
                    .append("last_name", c.getLast_name())
                    .append("email", c.getEmail())
                    .append("phone", c.getPhone())
                    .append("dob", c.getDob());
            customersCollection.updateOne(eq("_id", c.getId()), updatedCustomerDocument);
            result = Either.right(1);
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.UPDATINGERRORCUSTOMER));
        }
        return result;
    }

    public Either<Error, Integer> delete(Customer customer, boolean confirm, boolean xml) {
        Either<Error, Integer> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> customerCollection = database.getCollection("customers");
            MongoCollection<Document> credentialsCollection = database.getCollection("credentials");

            ObjectId customerId = customer.getId();

            if (customer.getOrders().isEmpty()) {
                customerCollection.deleteOne(eq("_id", customerId));
                credentialsCollection.deleteOne(eq("_id", customerId));
                result = Either.right(2);
            } else {
                if (confirm) {
                    List<Document> emptyOrderList = new ArrayList<>();
                    customerCollection.updateOne(eq("_id", customerId), set("orders", emptyOrderList));
                    if (xml) {
                        backupOrdersToXml(customer.getOrders(), customerId);
                    }
                    result = Either.right(2 + customer.getOrders().size());
                } else {
                    result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.EXISTINGORDERS));
                }
            }
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        }
        return result;
    }


    private void backupOrdersToXml(List<Order> orderList,ObjectId id) {
        try {
            XmlOrders xmlOrders = new XmlOrders(orderList);
            JAXBContext context = JAXBContext.newInstance(Order.class, XmlOrders.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlOrders, new File("src/main/data/" + id + "_backup.xml"));
        } catch (JAXBException e) {

        }
    }

}

