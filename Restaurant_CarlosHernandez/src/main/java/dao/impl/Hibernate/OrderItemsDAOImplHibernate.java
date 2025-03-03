package dao.impl.Hibernate;

import common.constants.ConstantsErrors;
import dao.JPAUtil;
import dao.OrderItemDAO;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import model.Customer;
import model.OrderItem;
import model.error.Error;

import java.util.List;

public class OrderItemsDAOImplHibernate implements OrderItemDAO {
    private final JPAUtil jpaUtil;
    private EntityManager em;
    @Inject
    public OrderItemsDAOImplHibernate(JPAUtil jpaUtil){this.jpaUtil=jpaUtil;}
    @Override
    public Either<Error, List<OrderItem>> getAll() {
        Either<Error, List<OrderItem>> result;
        em = jpaUtil.getEntityManager();
        try {
            List<OrderItem> orders = em.createNamedQuery("HQL_GET_ALL_ORDERITEMS", OrderItem.class).getResultList();
            result = Either.right(orders);
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDERITEM));
        } finally {
            em.close();
        }
        return result;
    }

    @Override
    public Either<Error, List<OrderItem>> get(int orderId) {
        try {
            em = jpaUtil.getEntityManager();
            TypedQuery<OrderItem> query = em.createQuery(
                    "SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId",
                    OrderItem.class
            );
            query.setParameter("orderId", orderId);
            List<OrderItem> orderItems = query.getResultList();
            return Either.right(orderItems);
        } catch (PersistenceException ex) {
            return Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDERITEM));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Either<Error, Integer> add(OrderItem orderItem) {
        return null;
    }

    @Override
    public Either<Error, Integer> delete(int id) {
        return null;
    }

    @Override
    public Either<Error, Integer> deleteAll(List<OrderItem> orderItems) {
        return null;
    }
}
