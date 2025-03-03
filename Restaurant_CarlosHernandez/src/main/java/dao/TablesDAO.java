package dao;

import io.vavr.control.Either;
import model.Restaurant_tables;
import model.error.Error;

import java.util.List;

public interface TablesDAO {
    Either<Error, List<Restaurant_tables>> getAll();

    Either<Error, Integer> add(Restaurant_tables t);

}
