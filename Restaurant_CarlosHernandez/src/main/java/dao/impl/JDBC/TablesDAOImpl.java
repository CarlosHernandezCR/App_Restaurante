package dao.impl.JDBC;

import common.constants.ConstantsErrors;
import common.constants.ConstantsTables;
import common.utils.SQLQueries;
import dao.DBConnectionPool;
import dao.TablesDAO;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Customer;
import model.Restaurant_tables;
import model.error.Error;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TablesDAOImpl implements TablesDAO {
    private final DBConnectionPool db;

    @Inject
    public TablesDAOImpl(DBConnectionPool db) {
        this.db = db;
    }

    @Override
    public Either<Error, List<Restaurant_tables>> getAll() {
        Either<Error, List<Restaurant_tables>> result;
        List<Restaurant_tables> tables = new ArrayList<>();
        try (Connection con = db.getConnection(); Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(SQLQueries.GETALLTABLES);
            while (rs.next()) {
                int id = rs.getInt(ConstantsTables.ID);
                int nSeats = rs.getInt(ConstantsTables.NUMBER_OF_SEATS);
                tables.add(new Restaurant_tables(id, nSeats));
            }
            if (tables.isEmpty()) {
                result = Either.left(new Error(ConstantsErrors.EMPTYLISTERROR, ConstantsErrors.EMPTYLISTCUSTOMERERRORMESSAGE));
            } else result = Either.right(tables);
        } catch (SQLException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
            result = Either.left(new Error(ConstantsErrors.FILEREADINGERROR, ConstantsErrors.FILEREADINGERRORCUSTOMER));
        }
        return result;
    }

    @Override
    public Either<Error, Integer> add(Restaurant_tables t) {
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.INSERTTABLE)) {
            preparedStatement.setString(1, String.valueOf(t.getId()));
            preparedStatement.setString(2, String.valueOf(t.getN_seats()));
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                return Either.right(rowsInserted);
            } else {
                return Either.left(new Error(ConstantsErrors.ADDINGERROR, ConstantsErrors.ADDINGERRORTABLE));
            }
        } catch (SQLException sqle) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, sqle);
            return Either.left(new Error(ConstantsErrors.FILEWRITINGERROR, ConstantsErrors.ADDINGERRORTABLE));
        }
    }
}