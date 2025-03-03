package service;

import dao.OrderItemDAO;
import dao.impl.Hibernate.OrderItemsDAOImplHibernate;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.OrderItem;
import model.error.Error;
import java.util.List;

public class OrderItemService {
    final OrderItemsDAOImplHibernate orderItemDAO;
    @Inject
    public OrderItemService(OrderItemsDAOImplHibernate orderItemDAO){this.orderItemDAO=orderItemDAO;}
    public Either<Error,List<OrderItem>> getAll(){return orderItemDAO.getAll();}
    public Either<Error,Integer> add(OrderItem orderItem) {return orderItemDAO.add(orderItem);}
    public Either<Error,Integer> deleteAll(List<OrderItem> orderItems) {return orderItemDAO.deleteAll(orderItems);}
}
