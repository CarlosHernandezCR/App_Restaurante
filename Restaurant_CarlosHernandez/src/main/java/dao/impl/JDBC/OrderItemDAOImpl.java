package dao.impl.JDBC;

import common.constants.ConstantsErrors;
import common.constants.ConstantsOrderItems;
import common.constants.ConstantsOrders;
import common.utils.SQLQueries;
import dao.DBConnectionPool;
import dao.OrderItemDAO;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.MenuItem;
import model.OrderItem;
import model.Order;
import model.error.Error;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderItemDAOImpl implements OrderItemDAO {
    private final DBConnectionPool db;
    @Inject
    public OrderItemDAOImpl(DBConnectionPool db) {
        this.db = db;
    }
    @Override
    public Either<Error, List<OrderItem>> getAll() {
        Either<Error, List<OrderItem>> result;
        List<OrderItem> items = new ArrayList<>();
        try (Connection con = db.getConnection(); Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(SQLQueries.GETALLORDERITEMS);
            while (rs.next()) {
                int orderItemId = rs.getInt(ConstantsOrderItems.ORDERITEMID);
                rs.getInt(ConstantsOrders.ORDERID);
                MenuItem menuItem=new MenuItem();
                int quantity = rs.getInt(ConstantsOrderItems.QUANTITY);
                items.add(new OrderItem(orderItemId, null, menuItem, quantity)); //orderid changed to null
            }result=Either.right(items);
        } catch (SQLException ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
            result=Either.left(new Error(ConstantsErrors.FILEREADINGERROR,ConstantsErrors.FILEREADINGERRORORDERITEM));
        }
        return result;
    }
    @Override
    public Either<Error,List<OrderItem>> get(int id){
        Either<Error, List<OrderItem>> result;
        try (Connection con = db.getConnection(); PreparedStatement statement = con.prepareStatement(SQLQueries.GETORDERITEM)) {
            statement.setInt(1,id);
            ResultSet rs = statement.executeQuery();
            List<OrderItem> orderItems=new ArrayList<>();
            while (rs.next()) {
                int orderItemId = rs.getInt(ConstantsOrders.ORDERITEMID);
                int quantity = rs.getInt(ConstantsOrderItems.QUANTITY);
                orderItems.add(new OrderItem(orderItemId, null, new MenuItem(), quantity));//orderid changed to null
            }result=Either.right(orderItems);
        } catch (SQLException e) {
            result=Either.left(new Error(ConstantsErrors.NOTFOUND,ConstantsErrors.ORDERITEMNOTFOUND));
        }
        return result;
    }
    @Override
    public Either<Error, Integer> add(OrderItem orderItem) {
        Either<Error, Integer> result;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.INSERTORDERITEM)) {
            //preparedStatement.setInt(1, orderItem.getOrderId());
            preparedStatement.setInt(2, orderItem.getMenuItem().getId());
            preparedStatement.setInt(3, orderItem.getQuantity());
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                result= Either.right(rowsInserted);
            } else {
                result= Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGERRORORDERITEM));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(OrderItem.class.getName()).log(Level.SEVERE, null, sqle);
            result= Either.left(new Error(ConstantsErrors.FILEWRITINGERROR, ConstantsErrors.FILEWRITINGERRORORDERITEM));
        }return result;
    }

    @Override
    public Either<Error, Integer> delete(int id) {
        Either<Error, Integer> result;
        try ( Connection con= db.getConnection();PreparedStatement deleteOrderItemsStatement = con.prepareStatement(
                SQLQueries.DELETEORDERITEMSWITHIDORDER)){
            deleteOrderItemsStatement.setInt(1, id);
            int rowsInserted = deleteOrderItemsStatement.executeUpdate();
            if (rowsInserted > 0) {
                result= Either.right(rowsInserted);
            } else {
                result= Either.left(new Error(ConstantsErrors.DELETINGERROR, ConstantsErrors.DELETINGERRORORDERITEM));
            }
        }catch (SQLException e){
            result=Either.left(new Error(ConstantsErrors.FILEWRITINGERROR,ConstantsErrors.DELETINGERRORORDERITEM));
        }return result;
    }

    @Override
    public Either<Error, Integer> deleteAll(List<OrderItem> orderItems) {
        int counter=1;
        for (OrderItem o : orderItems) {
            if(delete(o.getId()).isRight())
                counter++;
        }
        if(counter== orderItems.size())
            return Either.right(counter);
        else return Either.left(new Error(ConstantsErrors.DELETINGERROR,ConstantsErrors.DELETINGERRORORDERITEM));
    }
}
