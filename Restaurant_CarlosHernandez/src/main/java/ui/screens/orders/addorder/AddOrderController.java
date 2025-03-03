package ui.screens.orders.addorder;

import common.constants.ConstantsErrors;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Restaurant_tables;
import model.error.Error;
import model.mongo.MenuItem;
import model.mongo.Order;
import model.mongo.OrderItem;
import service.MenuItemService;
import service.OrderService;
import service.TableService;
import ui.screens.common.BaseScreenController;
import ui.screens.orders.showorder.ShowOrderController;
import ui.screens.orders.updateorder.UpdateOrderController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddOrderController extends BaseScreenController {
    @FXML
    private TableView<Order> tableViewOrders;
    @FXML
    private TableColumn<Order, LocalDateTime> dateOrder;
    @FXML
    private TableColumn<Order, Integer> idTable;
    @FXML
    private TableView<ShowOrderController.DataTable> tableViewOrderItems;
    @FXML
    private TableColumn<ShowOrderController.DataTable, String> nameItem;
    @FXML
    private TableColumn<ShowOrderController.DataTable, Double> priceItem;
    @FXML
    private TableColumn<ShowOrderController.DataTable, Integer> quantityItem;
    @FXML
    private TableColumn<ShowOrderController.DataTable, Double> totalItem;
    @FXML
    private ComboBox<Integer> tablesCombobox;
    @FXML
    private ComboBox<String> itemsComboBox;
    @FXML
    private ComboBox<Integer> quantityComboBox;
    private final OrderService orderService;
    private final MenuItemService menuItemService;
    private final TableService tableService;

    @Inject
    public AddOrderController(OrderService orderService, MenuItemService menuItemService, TableService tableService) {
        this.orderService = orderService;
        this.menuItemService = menuItemService;
        this.tableService = tableService;
    }

    public void initialize() {
        dateOrder.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        idTable.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        setTableOrder();
        nameItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceItem.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityItem.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalItem.setCellValueFactory(new PropertyValueFactory<>("total"));
        itemsComboBox.getItems().addAll(getItems());
        tablesCombobox.getItems().addAll(getTables());
        for (int i = 1; i < 11; i++) {
            quantityComboBox.getItems().add(i);
        }
    }

    private List<String> getItems() {
        Either<Error, List<MenuItem>> data = menuItemService.getAll();
        ArrayList<String> names = new ArrayList<>();
        if (data.isRight()) {
            for (MenuItem m : data.get())
                names.add(m.getName());
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.FILEREADINGERRORCUSTOMER);
            a.setHeaderText(ConstantsErrors.FILEREADINGERROR);
            a.show();
        }
        return names;
    }

    private List<Integer> getTables() {
        ArrayList<Integer> ids = new ArrayList<>();
        Either<Error, List<Restaurant_tables>> data = tableService.getAll();
        if (data.isRight()) {
            for (Restaurant_tables t : data.get())
                ids.add(t.getId());
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.FILEREADINGERRORCUSTOMER);
            a.setHeaderText(ConstantsErrors.FILEREADINGERROR);
            a.show();
        }
        return ids;
    }

    private void setTableOrder() {
        orderService.getAll(BaseScreenController.id)
                .peek(orders -> tableViewOrders.getItems().addAll(orders))
                .peekLeft(error -> getPrincipalController().sacarAlertError(error.getMessage()));
    }

    public void addOrder() {
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < tableViewOrderItems.getItems().size(); i++) {
            MenuItem menuItemId = menuItemService.get(tableViewOrderItems.getItems().get(i).getName()).get();
            OrderItem orderItem = new OrderItem(menuItemId.getId(), tableViewOrderItems.getItems().get(i).getQuantity());
            orderItems.add(orderItem);
        }
        UpdateOrderController.deleteDuplicate(orderItems);
        if (tablesCombobox.getValue() != null && quantityComboBox.getValue() != null && !orderItems.isEmpty()) {
            if (orderService.add(new Order(LocalDateTime.now(), tablesCombobox.getValue(), orderItems), BaseScreenController.id).isRight()) {
                tableViewOrderItems.getItems().clear();
                tableViewOrders.getItems().clear();
                setTableOrder();
            } else {
                orderService.add(new Order(LocalDateTime.now(), tablesCombobox.getValue(), orderItems), BaseScreenController.id)
                        .peekLeft(ordersError -> getPrincipalController().sacarAlertError(ordersError.getMessage()));
            }

        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
            a.setHeaderText(ConstantsErrors.EMPTYFIELDMESSAGE);
            a.show();
        }
    }

    public void addMenuItem() {
        if (itemsComboBox.getValue() != null && quantityComboBox.getValue() != null) {
            Either<Error, MenuItem> data = menuItemService.get(itemsComboBox.getValue());
            if (data.isRight()) {
                tableViewOrderItems.getItems().add(new ShowOrderController.DataTable(data.get().getName(), data.get().getPrice(), quantityComboBox.getValue(), Math.round((data.get().getPrice() * quantityComboBox.getValue()) * 100.0) / 100.0));
            } else {
                data.peekLeft(ordersError -> getPrincipalController().sacarAlertError(ordersError.getMessage()));
            }
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
            a.setHeaderText(ConstantsErrors.EMPTYLISTMENUITEMERRORMESSAGE);
            a.show();
        }
        quantityComboBox.setValue(1);
    }

    public void removeMenuItem() {
        ShowOrderController.DataTable selectedItem = tableViewOrderItems.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            tableViewOrderItems.getItems().remove(selectedItem);
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.NONCLICKEDMENUITEMMESSAGE);
            a.setHeaderText(ConstantsErrors.NONCLICKEDMENUITEM);
            a.show();
        }
    }
}


