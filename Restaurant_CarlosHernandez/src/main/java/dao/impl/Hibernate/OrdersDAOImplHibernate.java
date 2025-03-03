package dao.impl.Hibernate;

import common.constants.ConstantsErrors;
import dao.JPAUtil;
import dao.OrdersDAO;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import model.Order;
import model.OrderItem;
import model.error.Error;

import java.util.List;


public class OrdersDAOImplHibernate implements OrdersDAO {
    private final JPAUtil jpaUtil;

    @Inject
    public OrdersDAOImplHibernate(JPAUtil jpaUtil) {
        this.jpaUtil = jpaUtil;
    }

    @Override
    public Either<Error, List<Order>> getAll() {
        Either<Error, List<Order>> result;
        EntityManager em = jpaUtil.getEntityManager();
        try {
            List<Order> orders = em.createNamedQuery("HQL_GET_ALL_ORDERS", Order.class).getResultList();
            result = Either.right(orders);
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDER));
        } finally {
            em.close();
        }
        return result;
    }

    @Override
    public Either<Error, List<Order>> getAll(int customerId) {
        Either<Error, List<Order>> result;
        EntityManager em = jpaUtil.getEntityManager();
        try {
            em = jpaUtil.getEntityManager();
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o WHERE o.idCustomer = :customerId",
                    Order.class
            );
            query.setParameter("customerId", customerId);
            List<Order> orders = query.getResultList();
            result= Either.right(orders);
        } catch (PersistenceException ex) {
            result=Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDERITEM));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }return result;
    }
    @Override
    public Either<Error, Order> get(int id) {
        Either<Error, Order> result;
        EntityManager em = jpaUtil.getEntityManager();

        try {
            Order order = em.find(Order.class, id);
            if (order != null) {
                result = Either.right(order);
            } else {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.ORDERNOEXIST));
            }
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORORDER));
        } finally {
            em.close();
        }

        return result;
    }

    @Override
    public Either<Error, Integer> add(Order order) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Either<Error, Integer> result;
        try {
            tx.begin();
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItem.setOrder(order);
            }

            em.persist(order);
            tx.commit();

            result = Either.right(order.getId());
        } catch (PersistenceException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            result = Either.left(new Error(ConstantsErrors.ADDINGERROR, ex.getMessage()));
        } finally {
            em.close();
        }

        return result;
    }

    @Override
    public Either<Error, Integer> update(Order updatedOrder) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Either<Error, Integer> result;

        try {
            tx.begin();
            em.merge(updatedOrder);
            tx.commit();
            result = Either.right(1);
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            result = Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.UPDATINGERRORORDER));
        } finally {
            em.close();
        }

        return result;
    }

    @Override
    public Either<Error, Integer> delete(Order c) {
        Either<Error, Integer> result;
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
                em.remove(em.merge(c));
                tx.commit();
                result = Either.right(c.getOrderItems().size() + 1);
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORORDER));
        } finally {
            em.close();
        }
        return result;
    }
}


