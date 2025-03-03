package ui.screens.orders.deleteorder;

import common.constants.ConstantsErrors;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.mongo.*;
import model.error.Error;
import model.mongo.MenuItem;
import org.bson.types.ObjectId;
import service.MenuItemService;
import service.OrderService;
import ui.screens.common.BaseScreenController;
import ui.screens.orders.showorder.ShowOrderController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeleteOrderController extends BaseScreenController {
    @FXML
    private TableView<Order> tableViewOrders;
    @FXML
    private TableColumn<Order, String> dateOrder;
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
    private Label total;
    @FXML
    private Button delete;
    private final OrderService orderService;
    private final MenuItemService menuItemService;

    @Inject
    public DeleteOrderController(OrderService orderService, MenuItemService menuItemService) {
        this.orderService = orderService;
        this.menuItemService = menuItemService;
    }

    public void initialize() {
        dateOrder.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        idTable.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        setTableOrder();
        nameItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceItem.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityItem.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalItem.setCellValueFactory(new PropertyValueFactory<>("total"));
        tableViewOrders.setOnMouseClicked(this::clickOrder);
        delete.setVisible(false);
    }

    private void setTableOrder() {
        orderService.getAll().peek(orders -> tableViewOrders.getItems().addAll(orders))
                .peekLeft(ordersError -> getPrincipalController().sacarAlertError(ordersError.getMessage()));
    }

    public void clickOrder(MouseEvent event) {
        if (event.getClickCount() == 1) {
            if(tableViewOrders.getSelectionModel().getSelectedItem()!=null) {
                Either<Error, Order> result = orderService.get(tableViewOrders.getSelectionModel().getSelectedItem().getOrderDate());
                if (result.isRight()) {
                    delete.setVisible(true);
                    extracted(result.get());
                } else
                    result.peekLeft(ordersError -> getPrincipalController().sacarAlertError(ordersError.getMessage()));
            }
        }else{
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.NONCLICKEDMENUITEMMESSAGE);
            a.setHeaderText(ConstantsErrors.NONCLICKEDMENUITEM);
            a.show();
        }
    }


    private void extracted(Order selectedOrder) {
        List<OrderItem> orderItems = selectedOrder.getOrder_items();
        List<ShowOrderController.DataTable> items = new ArrayList<>();
        for (OrderItem item : orderItems) {
            MenuItem menuItem=menuItemService.get(item.getMenuItemId()).get();
            items.add(new ShowOrderController.DataTable(menuItem.getName(), menuItem.getPrice(), item.getQuantity(), Math.round((menuItem.getPrice() * item.getQuantity()) * 100.0) / 100.0));
        }
        tableViewOrderItems.getItems().clear();
        tableViewOrderItems.getItems().addAll(items);
        double sumaTotal = 0.0;
        for (TableColumn<ShowOrderController.DataTable, ?> column : tableViewOrderItems.getColumns()) {
            if (column.equals(totalItem)) {
                for (ShowOrderController.DataTable orderItem : tableViewOrderItems.getItems()) {
                    String totalStr = column.getCellData(orderItem).toString();
                    double totalValue = Double.parseDouble(totalStr);
                    sumaTotal += totalValue;
                }
                break;
            }
        }
        total.setText(String.format("%.2f €", sumaTotal));
    }

    public void delete() {
        Order selectedOrder = tableViewOrders.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            if (orderService.delete(selectedOrder.getOrderDate()).isRight()) {
                tableViewOrders.getItems().clear();
                tableViewOrderItems.getItems().clear();
                setTableOrder();
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle(ConstantsErrors.ERROR);
                a.setContentText(ConstantsErrors.DELETINGERRORORDER);
                a.setHeaderText(ConstantsErrors.DELETINGERROR);
                a.show();
            }
        }
    }
}
