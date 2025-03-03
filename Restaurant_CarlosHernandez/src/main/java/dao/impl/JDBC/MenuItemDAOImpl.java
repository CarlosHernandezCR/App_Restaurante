package dao.impl.JDBC;

import common.constants.ConstantsErrors;
import common.constants.ConstantsMenuItems;
import common.utils.SQLQueries;
import dao.DBConnectionPool;
import dao.MenuItemDAO;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.MenuItem;
import model.error.Error;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MenuItemDAOImpl implements MenuItemDAO{
    private final DBConnectionPool db;
    @Inject
    public MenuItemDAOImpl(DBConnectionPool db) {
        this.db = db;
    }
    @Override
    public Either<Error, List<MenuItem>> getAll() {
        Either<Error, List<MenuItem>> result;
        List<MenuItem> items = new ArrayList<>();
        try (Connection con = db.getConnection(); Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(SQLQueries.GETALLMENUITEMS);
            while (rs.next()) {
                int id = rs.getInt(ConstantsMenuItems.MENUITEMID);
                String name = rs.getString(ConstantsMenuItems.NAME);
                String description = rs.getString(ConstantsMenuItems.DESCRIPTION);
                Double price = rs.getDouble(ConstantsMenuItems.PRICE);
                items.add(new MenuItem(id, name, description, price));
            }
            if (items.isEmpty()) {
                result = Either.left(new Error(ConstantsErrors.EMPTYLISTERROR, ConstantsErrors.EMPTYLISTMENUITEMERRORMESSAGE));
            } else result = Either.right(items);
        } catch (SQLException ex) {
            Logger.getLogger(MenuItem.class.getName()).log(Level.SEVERE, null, ex);
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORMENUITEM));
        }
        return result;
    }

    @Override
    public Either<Error, MenuItem> get(int id) {
        Either<Error, MenuItem> result=null;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.GETMENUITEMSBYID)) {
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String name=rs.getString(ConstantsMenuItems.NAME);
                String description= rs.getString(ConstantsMenuItems.DESCRIPTION);
                Double price=rs.getDouble(ConstantsMenuItems.PRICE);
                result=Either.right(new MenuItem(id, name, description, price));
            }
        } catch (SQLException e) {
            result=Either.left(new Error(ConstantsErrors.NOTFOUND,ConstantsErrors.MENUITEMNOTFOUND));
        }
        return result;
    }
    @Override
    public Either<Error,MenuItem> get(String name){
        Either<Error, MenuItem> result=null;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.GETMENUITEMSBYNAME)) {
            preparedStatement.setString(1, name);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int id= rs.getInt(ConstantsMenuItems.MENUITEMID);
                String description= rs.getString(ConstantsMenuItems.DESCRIPTION);
                Double price=rs.getDouble(ConstantsMenuItems.PRICE);
                result=Either.right(new MenuItem(id, name, description, price));
            }
        } catch (SQLException e) {
            result=Either.left(new Error(ConstantsErrors.NOTFOUND,ConstantsErrors.MENUITEMNOTFOUND));
        }
        return result;
    }
}
