package service;

import dao.MenuItemDAO;
import dao.impl.mongo.MenuItemMongo;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.mongo.MenuItem;
import model.error.Error;
import org.bson.types.ObjectId;

import java.util.List;

public class MenuItemService {
    private final MenuItemMongo menuItemDAO;
    @Inject
    public MenuItemService(MenuItemMongo menuItemDAO){
        this.menuItemDAO=menuItemDAO;
    }
    public Either<Error, List<MenuItem>>getAll(){return menuItemDAO.getAll();}
    public Either<Error,MenuItem> get(Integer id){return menuItemDAO.get(id);}

    public Either<Error,MenuItem> get(String name) {return menuItemDAO.get(name);
    }
}
