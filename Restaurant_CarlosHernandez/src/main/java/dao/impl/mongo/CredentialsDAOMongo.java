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
import model.mongo.Credentials;
import org.bson.Document;
import model.error.Error;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class CredentialsDAOMongo {
    private final String connectionString;
    private final String databaseName ;
    private final String collectionName;

    @Inject
    public CredentialsDAOMongo(Configuration configuration) {
        this.connectionString = configuration.getProperty(Constants.MONGO_CONNECTION_STRING);
        this.databaseName = configuration.getProperty(Constants.DATABASENAME);
        this.collectionName = configuration.getProperty(Constants.CREDENTIALS);
    }

    public Either<Error, Credentials> get(String user_name, String password) {
        Either<Error, Credentials> result;
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> credentialsCollection = database.getCollection(collectionName);

            Document credentialsDocument = credentialsCollection.find(and(eq("user_name", user_name), eq("password", password))).first();
            if (credentialsDocument == null) {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CREDENTIALS_NOT_FOUND));
            } else {
                Credentials credentials = new Credentials();
                credentials.setId(credentialsDocument.getObjectId("_id"));
                credentials.setUsername(credentialsDocument.getString("user_name"));
                credentials.setPassword(credentialsDocument.getString("password"));
                result = Either.right(credentials);
            }
        } catch (Exception e) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCREDENTIALS));
        }
        return result;
    }


}
