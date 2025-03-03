package dao.impl.JDBC;

import common.constants.ConstantsErrors;
import common.constants.ConstantsOrders;
import common.utils.SQLQueries;
import dao.DBConnectionPool;
import dao.OrderItemDAO;
import dao.OrdersDAO;
import dao.impl.Hibernate.OrderItemsDAOImplHibernate;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Order;
import model.error.Error;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrdersDAOImpl implements OrdersDAO {
    private final DBConnectionPool db;
    private final OrderItemDAO orderItemDAO;
    @Inject
    public OrdersDAOImpl(DBConnectionPool db, OrderItemsDAOImplHibernate orderItemDAO) {
        this.db = db;
        this.orderItemDAO=orderItemDAO;
    }
    @Override
    public Either<Error, List<Order>> getAll() {
        Either<Error, List<Order>> result;
        List<Order> orders = new ArrayList<>();
        try (Connection con = db.getConnection(); Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(SQLQueries.GETALLORDERS);
            while (rs.next()){
                int id=rs.getInt(ConstantsOrders.ORDERID);
                Timestamp timestamp=rs.getTimestamp(ConstantsOrders.ORDERDATE);
                LocalDateTime date = timestamp.toLocalDateTime();
                int idCust=rs.getInt(ConstantsOrders.CUSTOMERID);
                int idTable=rs.getInt(ConstantsOrders.TABLEID);
                orders.add(new Order(id,date,idCust,idTable));
            }result=Either.right(orders);
        } catch (SQLException ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
            result=Either.left(new Error(ConstantsErrors.FILEREADINGERROR,ConstantsErrors.FILEREADINGERRORORDER));
        }
        return result;
    }

    @Override
    public Either<Error, List<Order>> getAll(int id) {
        return null;
    }

    @Override
    public Either<Error, Order> get(int id) {
        Either<Error, Order> result;
        try (Connection con = db.getConnection(); PreparedStatement statement = con.prepareStatement(SQLQueries.GETORDERWITHCUSTOMERID)) {
            statement.setInt(1,id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                LocalDateTime dateTime=rs.getTimestamp(ConstantsOrders.ORDERDATE).toLocalDateTime();
                int idOrder=rs.getInt(ConstantsOrders.CUSTOMERID);
                int idTable= rs.getInt(ConstantsOrders.TABLEID);
                if(orderItemDAO.get(id).isRight()){
                    result=Either.right(new Order(idOrder,dateTime,id,idTable,orderItemDAO.get(id).get()));
                }else result=Either.right(new Order(idOrder,dateTime,id,idTable));
            }else{result=Either.left(new Error(ConstantsErrors.DELETINGERROR,ConstantsErrors.DELETINGERRORORDER));}
        } catch (SQLException ex) {
            result=Either.left(new Error(ConstantsErrors.NOTFOUND,ConstantsErrors.ORDERNOEXIST));
        }
        return result;
    }

    @Override
    public Either<Error, Integer> add(Order o) {
        Either<Error, Integer> result=null;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.INSERTORDER,Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(o.getDateOrder()));
            preparedStatement.setString(2, String.valueOf(o.getIdCustomer()));
            preparedStatement.setString(3, String.valueOf(o.getIdTable()));
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    result= Either.right(generatedKeys.getInt(1));
                }
            } else {
                result= Either.left(new Error(ConstantsErrors.ADDINGERROR,ConstantsErrors.ADDINGERRORORDER));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, sqle);
            result= Either.left(new Error(ConstantsErrors.FILEWRITINGERROR,ConstantsErrors.FILEWRITINGERRORORDER));
        }return result;
    }

    @Override
    public Either<Error, Integer> update(Order o) {
        try (Connection con = db.getConnection()) {
            Order currentOrder = get(o.getId()).get();
            List<Object> toChange = new ArrayList<>();
            StringBuilder sentence = new StringBuilder(SQLQueries.UPDATEORDER);
            if (!(o.getDateOrder().isEqual(currentOrder.getDateOrder()))){
                sentence.append(SQLQueries.UPDATEDOBORDER);
                toChange.add(o.getDateOrder().toString());
            }

            if (!Objects.equals(o.getIdCustomer(), currentOrder.getIdCustomer())){
                sentence.append(SQLQueries.UPDATECUSTOMERIDORDER);
                toChange.add(o.getIdCustomer());
            }
            if (!Objects.equals(o.getIdTable(), currentOrder.getIdTable())){
                sentence.append(SQLQueries.UPDATETABLEIDORDER);
                toChange.add(o.getIdTable());
            }
            if(!o.getOrderItems().equals(currentOrder.getOrderItems())){
                for (int i = 0; i < o.getOrderItems().size(); i++) {
                    if(currentOrder.getOrderItems().isEmpty()){
                        orderItemDAO.add(o.getOrderItems().get(i));
                    }else {
                        for (int j = 0; j <= currentOrder.getOrderItems().size(); j++) {
                            if(!o.getOrderItems().get(i).equals(currentOrder.getOrderItems().get(j)))
                                orderItemDAO.add(o.getOrderItems().get(i));
                        }
                    }
                }
            }
            if (sentence.charAt(sentence.length() - 2) == ',') {
                sentence.delete(sentence.length() - 2, sentence.length());
            }
            sentence.append(SQLQueries.UPDATEWHEREORDER);
            toChange.add(o.getId());
            try (PreparedStatement preparedStatement = con.prepareStatement(sentence.toString())) {
                for (int i = 0; i < toChange.size(); i++) {
                    preparedStatement.setObject(i + 1, toChange.get(i));
                }
                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0)
                    return Either.right(rowsUpdated);
                else return Either.left(new Error(ConstantsErrors.UPDATINGERROR,ConstantsErrors.UPDATINGERRORORDER));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, sqle);
            return Either.left(new Error(ConstantsErrors.UPDATINGERROR,ConstantsErrors.UPDATINGERRORORDER));
        }
    }

    @Override
    public Either<Error, Integer> delete(Order o) {
        Either<Error, Integer> back;
        int nTablesDeleted=0;
        try (Connection con = db.getConnection()) {
            try (PreparedStatement deleteOrderItemsStatement = con.prepareStatement(SQLQueries.DELETEORDERITEMSWITHIDORDER)){
                deleteOrderItemsStatement.setInt(1, o.getId());
                nTablesDeleted=nTablesDeleted+deleteOrderItemsStatement.executeUpdate();
            }
            try (PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.DELETEORDERWITHIDORDER)) {
                preparedStatement.setInt(1, o.getId());
                nTablesDeleted=nTablesDeleted+preparedStatement.executeUpdate();
            }catch (SQLException sqle){
                Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, sqle);
            }
            back=Either.right(nTablesDeleted);
        } catch (SQLException e) {
            back = Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.EXISTINGORDERS));
        }
        return back;
    }
}
