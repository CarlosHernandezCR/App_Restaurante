package dao.impl.Hibernate;

import common.constants.ConstantsErrors;
import dao.CredentialsDAO;
import dao.JPAUtil;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import model.Credentials;
import model.error.Error;

import java.util.List;

public class CredentialsDAOImplHibernate implements CredentialsDAO {
    private final JPAUtil jpaUtil;
    private EntityManager em;

    @Inject
    public CredentialsDAOImplHibernate(JPAUtil jpaUtil) {
        this.jpaUtil = jpaUtil;
    }

    public Either<Error, List<Credentials>> getAll() {
        Either<Error, List<Credentials>> result;
        em = jpaUtil.getEntityManager();

        try {
            List<Credentials> credentialsList = em.createQuery("SELECT c FROM Credentials c", Credentials.class).getResultList();
            result = Either.right(credentialsList);
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCREDENTIALS));
        } finally {
            em.close();
        }

        return result;
    }

    public Either<Error, Credentials> get(String name, String password) {
        Either<Error, Credentials> result;
        em = jpaUtil.getEntityManager();

        try {
            Credentials credentials = em.createQuery("SELECT c FROM Credentials c WHERE c.user = :name AND c.password = :password", Credentials.class)
                    .setParameter("name", name)
                    .setParameter("password", password)
                    .getSingleResult();
            if (credentials != null) {
                result = Either.right(credentials);
            } else {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CREDENTIALS_NOT_FOUND));
            }
        } catch (NoResultException ex) {
            result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CREDENTIALS_NOT_FOUND));
        } catch (PersistenceException ex) {
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCREDENTIALS));
        } finally {
            em.close();
        }

        return result;
    }

    public Either<Error, Integer> update(Credentials credentials) {
        Either<Error, Integer> result;
        em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(credentials);
            tx.commit();
            result = Either.right(1);
        } catch (PersistenceException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            result = Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.UPDATINGERRORCREDENTIALS));
        } finally {
            em.close();
        }

        return result;
    }

    public Either<Error, Integer> delete(Credentials credentials) {
        Either<Error, Integer> result;
        em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Credentials credentialsToDelete = em.merge(credentials);
            em.remove(credentialsToDelete);
            tx.commit();
            result = Either.right(1);
        } catch (PersistenceException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            result = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETECREDENTIALSERROR));
        } finally {
            em.close();
        }

        return result;
    }
}
