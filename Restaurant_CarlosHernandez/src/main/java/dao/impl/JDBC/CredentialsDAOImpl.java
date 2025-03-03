package dao.impl.JDBC;

import common.constants.ConstantsCustomer;
import common.constants.ConstantsErrors;
import common.utils.SQLQueries;
import dao.CredentialsDAO;
import dao.DBConnectionPool;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Credentials;
import model.error.Error;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CredentialsDAOImpl implements CredentialsDAO {
    private final DBConnectionPool db;
    @Inject
    public CredentialsDAOImpl(DBConnectionPool dbConnectionPool){
        this.db=dbConnectionPool;
    }
    @Override
    public Either<Error, List<Credentials>> getAll() {
        Either<Error, List<Credentials>> result;
        try (Connection con = db.getConnection();
                 Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(SQLQueries.GETALLCREDENTIALS);
            List<Credentials> credentials=new ArrayList<>();
            while (rs.next()) {
                credentials.add(new Credentials(rs.getInt("id"),rs.getString("user_name"), rs.getString("password")));
            }
            result=Either.right(credentials);
        }catch (SQLException ex) {
            Logger.getLogger(Credentials.class.getName()).log(Level.SEVERE, null, ex);
            result=Either.left(new Error(ConstantsErrors.FILEREADINGERROR,ConstantsErrors.FILEREADINGERRORCREDENTIALS));
        }return result;
    }

    public Either<Error, Credentials> get(String username,String password) {
        Either<Error, Credentials> result;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.GETCREDENTIALS)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(ConstantsCustomer.ID);
                String name = rs.getString("username");
                String passw = rs.getString("password");
                result = Either.right(new Credentials(id, name, passw));
            }else {
                result = Either.left(new Error(ConstantsErrors.NOTFOUND,ConstantsErrors.CREDENTIALS_NOT_FOUND));
            }
        } catch (SQLException e) {
            result = Either.left(new Error(ConstantsErrors.NOTFOUND, ConstantsErrors.CUSTOMERNOEXIST));
        }
        return result;
    }
    public Either<Error, Integer> delete(int id) {
        Either<Error, Integer> result;
        try (Connection con = db.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(SQLQueries.DELETECREDENTIALWITHIDCUSTOMER)) {
                preparedStatement.setInt(1, id);
                result = Either.right(preparedStatement.executeUpdate());
            }
        }catch (SQLException sqlException){
            result=Either.left(new Error(ConstantsErrors.DELETINGERROR,ConstantsErrors.DELETECREDENTIALSERROR));
        }
        return result;
    }
}
