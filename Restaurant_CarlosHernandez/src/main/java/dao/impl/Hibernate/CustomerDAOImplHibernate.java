package dao.impl.Hibernate;

import common.constants.ConstantsErrors;
import dao.JPAUtil;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import model.Customer;
import model.error.Error;

import java.util.List;

public class CustomerDAOImplHibernate {
    private final JPAUtil jpaUtil;
    private EntityManager em;

    @Inject
    public CustomerDAOImplHibernate(JPAUtil jpaUtil) {
        this.jpaUtil = jpaUtil;
    }

    public Either<Error, List<Customer>> getAll() {
        Either<Error, List<Customer>> result;
        em = jpaUtil.getEntityManager();

        try {
            List<Customer> customers = em.createNamedQuery("HQL_GET_ALL_CUSTOMER", Customer.class).getResultList();
            result = Either.right(customers);
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        } finally {
            em.close();
        }

        return result;
    }

    public Either<Error, Customer> get(int id) {
        Either<Error, Customer> result;
        em = jpaUtil.getEntityManager();

        try {
            Customer customer = em.find(Customer.class, id);
            if (customer != null) {
                result = Either.right(customer);
            } else {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
            }
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        } finally {
            em.close();
        }

        return result;
    }

    public Either<Error, Integer> add(Customer customer) {
        em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Either<Error, Integer> result;

        try {
            tx.begin();
            em.persist(customer.getCredentials());
            em.flush();
            em.persist(customer);
            tx.commit();
            result = Either.right(2);
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

    public Either<Error, Integer> update(Customer customer) {
        Either<Error, Integer> result;
        em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(customer);
            tx.commit();
            result = Either.right(1);
        } catch (PersistenceException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            result = Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.UPDATINGERRORCUSTOMER));
        } finally {
            em.close();
        }
        return result;
    }

    public Either<Error, Integer> delete(Customer customer, boolean confirm, boolean xml) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Either<Error, Integer> result;

        try {
            tx.begin();
            em.remove(em.merge(customer));
            tx.commit();
            result = Either.right(2);
        } catch (PersistenceException ex) {
            if (confirm) {
                if (xml) {
                    //backupOrdersToXml(customer);
                }
                try {
                    em = jpaUtil.getEntityManager();
                    tx = em.getTransaction();
                    tx.begin();
                    Query query = em.createQuery("DELETE FROM OrderItem oi WHERE oi.order.id IN (SELECT o.id FROM Order o WHERE o.idCustomer = :customerId)");
                    query.setParameter("customerId", customer.getId());
                    int n=query.executeUpdate();
                    Query query2 = em.createQuery("DELETE FROM Order o WHERE o.idCustomer = :customerId");
                    query2.setParameter("customerId", customer.getId());
                    n=n+query2.executeUpdate();
                    em.remove(em.merge(customer));
                    tx.commit();
                    result = Either.right(n + 2);
                } catch (PersistenceException ex1) {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                    result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORCUSTOMER));
                } finally {
                    em.close();
                }
            } else {
                if (tx.isActive()) {
                    tx.rollback();
                }
                result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORCUSTOMER));
            }
        } finally {
            em.close();
        }

        return result;
    }

//    private void backupOrdersToXml(Customer customer) {
//        em = jpaUtil.getEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            List<Order> orderList = em.createQuery("SELECT o FROM Order o WHERE o.idCustomer = :customerId", Order.class)
//                    .setParameter("customerId", customer.getId())
//                    .getResultList();
//            XmlOrders xmlOrders = new XmlOrders(orderList);
//            JAXBContext context = JAXBContext.newInstance(Order.class, XmlOrders.class);
//            Marshaller marshaller = context.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(xmlOrders, new File("src/main/data/" + customer.getId() + "_backup.xml"));
//
//        } catch (JAXBException e) {
//            if (tx.isActive()) {
//                tx.rollback();
//            }
//        } finally {
//            em.close();
//        }
//    }


}
