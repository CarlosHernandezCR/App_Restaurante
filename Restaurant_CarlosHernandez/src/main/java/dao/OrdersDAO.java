package dao;

import io.vavr.control.Either;
import model.Order;
import model.error.Error;


import java.util.List;

public interface OrdersDAO {
    Either<Error, List<Order>> getAll();
    Either<Error, List<Order>> getAll(int id);
    Either<Error, Order> get(int id);

    Either<Error, Integer> add(Order c);

    Either<Error, Integer> update(Order c);

    Either<Error, Integer> delete(Order c);
}
