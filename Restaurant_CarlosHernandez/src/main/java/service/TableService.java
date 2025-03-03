package service;

import dao.TablesDAO;
import dao.impl.JDBC.TablesDAOImpl;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Restaurant_tables;
import model.error.Error;

import java.util.List;

public class TableService {
    private final TablesDAO tablesDAO;
    @Inject
    public TableService(TablesDAOImpl tablesDAO){this.tablesDAO=tablesDAO;}
    public Either<Error, List<Restaurant_tables>> getAll(){return tablesDAO.getAll();}
    public Either<Error, Integer> add(Restaurant_tables t){return tablesDAO.add(t);}
}
