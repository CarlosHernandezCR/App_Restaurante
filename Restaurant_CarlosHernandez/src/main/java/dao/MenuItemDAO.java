package dao;

import io.vavr.control.Either;
import model.MenuItem;
import model.error.Error;

import java.util.List;

public interface MenuItemDAO {
    Either<Error, List<MenuItem>> getAll();
    Either<Error,MenuItem> get(int id);
    Either<Error,MenuItem> get(String name);
}
