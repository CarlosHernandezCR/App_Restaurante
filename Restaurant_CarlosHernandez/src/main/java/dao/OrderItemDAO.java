package dao;

import io.vavr.control.Either;
import model.OrderItem;
import model.error.Error;

import java.util.List;

public interface OrderItemDAO {

    Either<Error, List<OrderItem>> getAll();

    Either<Error, List<OrderItem>> get(int id);

    Either<Error, Integer> add(OrderItem orderItem);
    Either<Error, Integer> delete(int id);
    Either<Error, Integer> deleteAll(List<OrderItem> orderItems);
}
