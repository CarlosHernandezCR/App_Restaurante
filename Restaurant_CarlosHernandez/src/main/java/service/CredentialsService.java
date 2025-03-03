package service;

import dao.impl.mongo.CredentialsDAOMongo;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.mongo.Credentials;
import model.error.Error;

public class CredentialsService{
    final CredentialsDAOMongo credentialsDAOST;
    @Inject
    public CredentialsService(CredentialsDAOMongo credentialsDAOST) {
        this.credentialsDAOST = credentialsDAOST;
    }

    public Either<Error, Credentials> checkLogin(String username, String password){
        return credentialsDAOST.get(username,password);
    }
}
