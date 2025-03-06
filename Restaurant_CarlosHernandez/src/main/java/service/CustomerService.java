package service;

import dao.impl.Hibernate.CustomerDAOImplHibernate;
import dao.impl.mongo.CustomerDAOMongo;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.error.Error;
import model.mongo.Customer;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Objects;

import static common.constants.ConstantsErrors.DELETINGERROR;
import static common.constants.ConstantsErrors.EMPTYLISTERROR;

public class CustomerService {
    private final CustomerDAOMongo dao;
    private final CustomerDAOImplHibernate customerDAOImplHibernate;
    @Inject
    public CustomerService(CustomerDAOMongo dao,CustomerDAOImplHibernate customerDAOImplHibernate){
        this.dao=dao;
        this.customerDAOImplHibernate=customerDAOImplHibernate;
    }

    public Either<Error, List<Customer>> getAll() {
        return dao.getAll();
    }

    public Either<Error, Customer> get(ObjectId id) {return dao.get(id);}

    public Either<Error, Integer> add(Customer c,String username,String password) {
        return dao.add(c,username,password);}

    public Either<Error, Integer> update(Customer c) {return dao.update(c);}

    public Either<Error, Integer> delete(Customer c,boolean o,boolean b) {
        Either<Error, Customer> customer = dao.get(c.getId());
        if (!o){
            if (customer.isRight()) {
                if (customer.get().getOrders().isEmpty()) {
                    return Either.left(new Error(DELETINGERROR, ""));
                }
            }
        }
        return dao.delete(c,o,b);
    }

    public Either<Error, List<model.Customer>> getAllHibernate() {
        return customerDAOImplHibernate.getAll();
    }
}
