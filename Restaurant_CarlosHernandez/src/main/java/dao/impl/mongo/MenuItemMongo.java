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
import model.mongo.MenuItem;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MenuItemMongo {
    private final String connectionString;
    private final String databaseName ;
    private final String collectionName;

    @Inject
    public MenuItemMongo(Configuration configuration) {
        this.connectionString = configuration.getProperty(Constants.MONGO_CONNECTION_STRING);
        this.databaseName = configuration.getProperty(Constants.DATABASENAME);
        this.collectionName = configuration.getProperty(Constants.MENU_ITEMS);
    }

    public Either<Error, List<MenuItem>> getAll() {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> menuItemCollection = database.getCollection(collectionName);

            List<MenuItem> menuItems = new ArrayList<>();
            for (Document menuItemDocument : menuItemCollection.find()) {
                menuItems.add(new MenuItem(menuItemDocument));
            }

            return Either.right(menuItems);
        } catch (Exception e) {
            return Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORMENUITEM));
        }
    }

    public Either<Error, MenuItem> get(Integer id) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> menuItemCollection = database.getCollection(collectionName);

            Document menuItemDocument = menuItemCollection.find(eq("_id", id)).first();
            if (menuItemDocument == null) {
                return Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.MENUITEMNOTFOUND));
            } else {
                MenuItem menuItem = new MenuItem(menuItemDocument);
                return Either.right(menuItem);
            }
        } catch (Exception e) {
            return Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORMENUITEM));
        }
    }

    public Either<Error, MenuItem> get(String name) {
        Either<Error, MenuItem> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> menuCollection = database.getCollection(collectionName);

            Document query = new Document("name", name);
            Document menuItemDoc = menuCollection.find(query).first();

            if (menuItemDoc == null) {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.MENUITEMNOTFOUND));
            } else {
                MenuItem menuItem = new MenuItem(menuItemDoc);
                result = Either.right(menuItem);
            }
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORMENUITEM));
        }
        return result;
    }

}
