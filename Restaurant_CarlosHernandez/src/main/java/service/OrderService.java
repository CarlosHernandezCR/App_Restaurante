package service;

import dao.impl.Hibernate.OrdersDAOImplHibernate;
import dao.impl.mongo.OrderDAOMongo;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.mongo.Order;
import model.error.Error;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final OrderDAOMongo daoOrders;
    private final OrdersDAOImplHibernate dao;
    @Inject
    public OrderService(OrderDAOMongo daoOrders, OrdersDAOImplHibernate dao){
        this.daoOrders=daoOrders;
        this.dao=dao;
    }

    public Either<Error, List<Order>> getAll() {
        return daoOrders.getAll();
    }
    public Either<Error, List<Order>> getAll(ObjectId id) {
        return daoOrders.getAll(id);
    }

    public Either<Error, Order> get(LocalDateTime date) {
        return daoOrders.get(date);
    }

    public Either<Error, Integer> add(Order o, ObjectId customerId) {
        return daoOrders.add(o,customerId);
    }

    public Either<Error, Integer> update(Order o) {
        return daoOrders.update(o);
    }
    public Either<Error, Integer> delete(LocalDateTime o) {
        return daoOrders.delete(o);
    }

    public Either<Error, List<model.Order>> getAllHibernate(Integer id) {
        return dao.getAll(id);
    }
}
