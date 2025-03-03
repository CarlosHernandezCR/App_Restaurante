package dao;

import io.vavr.control.Either;
import model.mongo.Customer;
import model.error.Error;

import java.util.List;

public interface CustomerDAO {
    Either<Error, List<model.Customer>> getAll();

    Either<Error, model.Customer> get(int id);

    Either<Error, Integer> add(Customer c);

    Either<Error, Integer> update(Customer c);

    Either<Error, Integer> delete(int c,boolean o,boolean b);
}
