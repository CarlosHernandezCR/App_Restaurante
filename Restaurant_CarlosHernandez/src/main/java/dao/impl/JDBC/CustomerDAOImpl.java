package dao.impl.JDBC;

import common.constants.*;
import common.utils.SQLQueries;
import dao.CustomerDAO;
import dao.DBConnectionPool;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Credentials;
import model.Customer;
import model.Order;
import model.error.Error;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerDAOImpl implements CustomerDAO {
    private final DBConnectionPool db;

    @Inject
    public CustomerDAOImpl(DBConnectionPool db) {
        this.db = db;
    }

    @Override
    public Either<Error, List<Customer>> getAll() {
        Either<Error, List<Customer>> result;
        List<Customer> customers = new ArrayList<>();
        try (Connection con = db.getConnection(); Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(SQLQueries.GETALLCUSTOMERS);
            while (rs.next()) {
                int id = rs.getInt(ConstantsCustomer.ID);
                String firstname = rs.getNString(ConstantsCustomer.FIRSTNAME);
                String lastname = rs.getNString(ConstantsCustomer.LASTNAME);
                String email = rs.getString(ConstantsCustomer.EMAIL);
                String phoneNumber = rs.getString(ConstantsCustomer.PHONE);
                LocalDate date = rs.getDate(ConstantsCustomer.DOB).toLocalDate();
                customers.add(new Customer(id, firstname, lastname, email, phoneNumber, date));
            }
            if (customers.isEmpty()) {
                result = Either.left(new Error(ConstantsErrors.EMPTYLISTERROR, ConstantsErrors.EMPTYLISTCUSTOMERERRORMESSAGE));
            } else result = Either.right(customers);
        } catch (SQLException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        }
        return result;
    }

    @Override
    public Either<Error, Customer> get(int id) {
        Either<Error, Customer> result = null;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.GETCUSTOMER)) {
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String firstName = rs.getString(ConstantsCustomer.FIRSTNAME);
                String lastName = rs.getString(ConstantsCustomer.LASTNAME);
                String email = rs.getString(ConstantsCustomer.EMAIL);
                String phone = rs.getString(ConstantsCustomer.PHONE);
                LocalDate dob = rs.getTimestamp(ConstantsCustomer.DOB).toLocalDateTime().toLocalDate();
                result = Either.right(new Customer(id, firstName, lastName, email, phone, dob));
            }
        } catch (SQLException e) {
            result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
        }
        return result;
    }

    @Override
    public Either<Error, Integer> add(model.mongo.Customer c) {
        return null;
    }

    @Override
    public Either<Error, Integer> update(model.mongo.Customer c) {
        return null;
    }

    public Either<Error, Integer> add(Customer c) {
        Either<Error, Integer> result;
        int added = 0;
        int customerId = 0;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.INSERTCUSTOMER, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, c.getFirst_name());
            preparedStatement.setString(2, c.getLast_name());
            preparedStatement.setString(3, c.getEmail());
            preparedStatement.setString(4, c.getPhone());
            preparedStatement.setDate(5, Date.valueOf(c.getDob()));
            added = preparedStatement.executeUpdate();
            if (added > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    customerId = generatedKeys.getInt(1);
                }
            } else {
                return Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGERRORCUSTOMER));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, sqle);
            return Either.left(new Error(ConstantsErrors.FILEWRITINGERROR, ConstantsErrors.FILEWRITINGERRORCUSTOMER));
        }
        result = addCredentials(c.getCredentials(), customerId, added);

        return result;
    }

    private Either<Error, Integer> addCredentials(Credentials c, int customerId, int added) {
        Either<Error, Integer> result;
        if (customerId > 0) {
            try (Connection con = db.getConnection()) {
                try (PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.INSERTCREDENTIAL)) {
                    preparedStatement.setInt(1, customerId);
                    preparedStatement.setString(2, c.getUser());
                    preparedStatement.setString(3, c.getPassword());
                    int rowsInserted = preparedStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        added += rowsInserted;
                        result = Either.right(added);
                    } else {
                        result = Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGCREDENTIALERROR));
                    }
                } catch (SQLException sqle) {
                    if (sqle.getErrorCode() == 1062) {
                        delete(customerId, false,false);
                        result = Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.USERNAMEREPEATED));
                    } else
                        result = Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGCREDENTIALERROR));
                }
            } catch (SQLException ex) {
                result = Either.left(new Error(ConstantsErrors.FILEWRITINGERROR, ConstantsErrors.ADDINGERRORCUSTOMER));
            }
        } else {
            result = Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGERRORCUSTOMER));
        }
        return result;
    }

    public Either<Error, Integer> update(Customer c) {
        Either<Error, Integer> result;
        boolean change = false;
        try (Connection con = db.getConnection()) {
            Customer currentCustomer = get(c.getId()).get();
            List<Object> toChange = new ArrayList<>();
            StringBuilder sentence = new StringBuilder(SQLQueries.UPDATECUSTOMER);
            if (c.getFirst_name().compareTo(currentCustomer.getFirst_name()) != 0) {
                sentence.append(SQLQueries.UPDATEFNAME);
                toChange.add(c.getFirst_name());
                change = true;
            }
            if (c.getLast_name().compareTo(currentCustomer.getLast_name()) != 0) {
                sentence.append(SQLQueries.UPDATELNAME);
                toChange.add(c.getLast_name());
                change = true;
            }

            if (c.getEmail().compareTo(currentCustomer.getEmail()) != 0) {
                sentence.append(SQLQueries.UPDATEEMAIL);
                toChange.add(c.getEmail());
                change = true;
            }
            if (currentCustomer.getPhone() != null) {
                if (c.getPhone().compareTo(currentCustomer.getPhone()) != 0) {
                    sentence.append(SQLQueries.UPDATEPHONE);
                    toChange.add(c.getPhone());
                    change = true;
                }
            } else {
                sentence.append(SQLQueries.UPDATEPHONE);
                toChange.add(c.getPhone());
                change = true;
            }
            if (c.getDob().toString().compareTo(currentCustomer.getDob().toString()) != 0) {
                sentence.append(SQLQueries.UPDATEDOB);
                toChange.add(c.getDob());
                change = true;
            }
            if (!change) {
                return Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.NONUPDATED));
            }
            if (sentence.charAt(sentence.length() - 2) == ',') {
                sentence.delete(sentence.length() - 2, sentence.length());
            }
            sentence.append(SQLQueries.UPDATEWHERE);
            toChange.add(c.getId());

            try (PreparedStatement preparedStatement = con.prepareStatement(sentence.toString())) {
                for (int i = 0; i < toChange.size(); i++) {
                    preparedStatement.setObject(i + 1, toChange.get(i));
                }
                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    result = Either.right(rowsUpdated);
                } else
                    result = Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.UPDATINGERRORCUSTOMER));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, sqle);
            result = Either.left(new Error(ConstantsErrors.UPDATINGERROR, ConstantsErrors.UPDATINGERRORCUSTOMER));
        }
        return result;
    }

    @Override
    public Either<Error, Integer> delete(int customerId, boolean confirmed,boolean backup) {
        Either<Error, Integer> back;
        int deleted = 0;
        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement deleteCredentialsStatement = con.prepareStatement(
                        SQLQueries.DELETECREDENTIALWITHIDCUSTOMER)) {
                    deleteCredentialsStatement.setInt(1, customerId);
                    deleted += deleteCredentialsStatement.executeUpdate();
                }

                try (PreparedStatement deleteCustomerStatement = con.prepareStatement(
                        SQLQueries.DELETECUSTOMERWITHID)) {
                    deleteCustomerStatement.setInt(1, customerId);
                    deleted += deleteCustomerStatement.executeUpdate();
                }

                con.commit();
                back = Either.right(deleted);
            } catch (SQLException ex) {

                //delete orders if user wants
                if (!confirmed) {
                    if (ex.getErrorCode() == 14551) {
                        con.rollback();
                        back = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.EXISTINGORDERS));
                    } else {
                        con.rollback();
                        back = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORCUSTOMER));
                    }
                } else {
                    con.setAutoCommit(false);
                    try (PreparedStatement deleteCredentialsStatement = con.prepareStatement(
                            SQLQueries.DELETECREDENTIALWITHIDCUSTOMER)) {
                        deleteCredentialsStatement.setInt(1, customerId);
                        deleted = deleted + deleteCredentialsStatement.executeUpdate();
                    }
                    try (PreparedStatement deleteOrderItemsStatement = con.prepareStatement(
                            SQLQueries.DELETEORDERITEMWITHORDERID +
                                    SQLQueries.SELECTORDERIDWITHCUSTOMERID)) {
                        deleteOrderItemsStatement.setInt(1, customerId);
                        deleted = deleted + deleteOrderItemsStatement.executeUpdate();
                    }
                    con.commit();
                    //make the backup of the orders
                    if(backup){
                        String customerName="";
                        List<Order> orders=new ArrayList<>();
                        try (PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.GETCUSTOMERNAMEWITHID)) {
                            preparedStatement.setInt(1, customerId);
                            ResultSet rs = preparedStatement.executeQuery();
                            if (rs.next()) {
                                customerName = rs.getString(ConstantsCustomer.FIRSTNAME);
                            }
                        } catch (SQLException e) {
                            back = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
                            return back;
                        }
                        try (PreparedStatement statement = con.prepareStatement(
                                SQLQueries.SELECTORDERSWITHCUSTOMERIDGETED)) {
                            statement.setInt(1,customerId);
                            ResultSet rs = statement.executeQuery();
                            while (rs.next()) {
                                int order_id = rs.getInt(ConstantsOrders.ORDERID);
                                Timestamp order_date= rs.getTimestamp(ConstantsOrders.ORDERDATE);
                                int customer_id = rs.getInt(ConstantsOrders.CUSTOMERID);
                                int table_id = rs.getInt(ConstantsOrders.TABLEID);
                                orders.add(new Order(order_id, order_date.toLocalDateTime(), customer_id, table_id));
                            }
                        } catch (SQLException e) {
                            back=Either.left(new Error(ConstantsErrors.NOTFOUND,ConstantsErrors.ORDERITEMNOTFOUND));
                            return back;
                        }
                    }
                    try (PreparedStatement deleteOrdersStatement = con.prepareStatement(SQLQueries.DELETEORDERWITHCUSTOMERIDGETED)) {
                        deleteOrdersStatement.setInt(1, customerId);
                        deleted = deleted + deleteOrdersStatement.executeUpdate();
                    }
                    try (PreparedStatement deleteCustomerStatement = con.prepareStatement(SQLQueries.DELETECUSTOMERWITHID)) {
                        deleteCustomerStatement.setInt(1, customerId);
                        deleted = deleted + deleteCustomerStatement.executeUpdate();
                    }
                    back=Either.right(deleted);
                }
                return back;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            back = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORCUSTOMER));
            return back;
        }return back;
    }
}
