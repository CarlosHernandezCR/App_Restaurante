package dao;

import io.vavr.control.Either;
import model.Credentials;
import model.error.Error;

import java.util.List;

public interface CredentialsDAO {

    Either<Error, List<Credentials>> getAll();
    Either<Error, Credentials> get(String username,String password);

}
