package ui.screens.orders.showorder;

import common.constants.Constants;
import common.constants.ConstantsErrors;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.mongo.Customer;
import model.mongo.MenuItem;
import model.mongo.Order;
import model.mongo.OrderItem;
import model.error.Error;
import org.bson.types.ObjectId;
import service.CustomerService;
import service.MenuItemService;
import service.OrderService;
import ui.screens.common.BaseScreenController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ShowOrderController extends BaseScreenController {

    @FXML
    private TableView<Order> tableViewOrders;
    @FXML
    private TableColumn<Order, LocalDateTime> dateOrder;
    @FXML
    private TableColumn<Order,Integer> idTable;
    @FXML
    private TableView<DataTable> tableViewOrderItems;
    @FXML
    private TableColumn<DataTable,String> nameItem;
    @FXML
    private TableColumn<DataTable,Double> priceItem;
    @FXML
    private TableColumn<DataTable,Integer> quantityItem;
    @FXML
    private TableColumn<DataTable,Double> totalItem;
    @FXML
    private Label total;

    private final OrderService orderService;
    private final CustomerService customerService;
    private final MenuItemService menuItemService;
    @FXML
    public ComboBox<String> filterComboBox;
    @FXML
    public DatePicker dateField;
    @FXML
    public ComboBox <ObjectId> idComboBox;

    @Inject
    public ShowOrderController(OrderService orderService, CustomerService customerService, MenuItemService menuItemService) {
        this.orderService = orderService;
        this.customerService=customerService;
        this.menuItemService=menuItemService;
    }
    public void initialize(){
        dateOrder.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        idTable.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        setTableOrder();
        nameItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceItem.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityItem.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalItem.setCellValueFactory(new PropertyValueFactory<>("total"));
        if(BaseScreenController.id.compareTo(new ObjectId("67c4ad7f07578cc6dea543ab"))!=0) {
            filterComboBox.getItems().addAll(Constants.ITEM1, Constants.ITEM2);
            idComboBox.setVisible(false);
        }else {
            filterComboBox.getItems().addAll(Constants.ITEM1, Constants.ITEM2, Constants.ITEM3);
            idComboBox.getItems().addAll(getIds());
        }
        tableViewOrders.setOnMouseClicked(this::clickOrder);
        dateField.setVisible(false);
        idComboBox.setVisible(false);
        filterComboBox.setOnAction(event -> {
            if (Constants.ITEM3.equals(filterComboBox.getValue())) {
                dateField.setVisible(false);
                idComboBox.setVisible(true);
            } else if(Constants.ITEM2.equals(filterComboBox.getValue())){
                idComboBox.setVisible(false);
                dateField.setVisible(true);
            }else{
                setTableOrder();
                dateField.setVisible(false);
                idComboBox.setVisible(false);
            }
        });
    }
    private void setTableOrder() {
        tableViewOrders.getItems().clear();
        if(BaseScreenController.id.compareTo(new ObjectId("67c4ad7f07578cc6dea543ab"))!=0) {
            orderService.getAll(BaseScreenController.id)
                    .peek(orders -> tableViewOrders.getItems().addAll(orders))
                    .peekLeft(error -> getPrincipalController().sacarAlertError(error.getMessage()));
        }else{
            orderService.getAll()
                    .peek(orders -> tableViewOrders.getItems().addAll(orders))
                    .peekLeft(error -> getPrincipalController().sacarAlertError(error.getMessage()));
        }
    }

    private HashSet<ObjectId> getIds(){
        List<ObjectId> ids=new ArrayList<>();
        Either<Error,List<Customer>> data2=customerService.getAll();
        if(data2.isRight()) {
            for (Customer customer : data2.get()) {
                if(customer.getOrders()!=null){
                    ids.add(customer.getId());
                }
            }
        }
        return new HashSet<>(ids);
    }
    public void clickOrder(MouseEvent event) {
        if (event.getClickCount() == 1) {
            if(tableViewOrders.getSelectionModel().getSelectedItem()!=null) {
                Either<Error, Order> result = orderService.get(tableViewOrders.getSelectionModel().getSelectedItem().getOrderDate());
                if (result.isRight()) {
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
    @Getter
    @Setter
    @AllArgsConstructor
    public static class DataTable{
        private String name;
        private Double price;
        private Integer quantity;
        private Double total;
    }

    public void filter(){
        Either<Error,List<Order>> result=orderService.getAll();
        tableViewOrders.getItems().clear();
            if (Constants.ITEM2.equals(filterComboBox.getValue())) {
                if(dateField.getValue()!=null) {
                    result.peek(orders -> {
                                List<Order> filteredOrders = orders.stream()
                                        .filter(order -> order.getOrderDate().toLocalDate().equals(dateField.getValue()))
                                        .toList();
                                tableViewOrders.getItems().setAll(filteredOrders);
                            })
                            .peekLeft(orderError -> getPrincipalController().sacarAlertError(orderError.getMessage()));
                }else {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle(ConstantsErrors.ERROR);
                    a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
                    a.setHeaderText(ConstantsErrors.EMPTYFIELD);
                    a.show();
                }
            } else if (Constants.ITEM3.equals(filterComboBox.getValue())) {
                if (result.isRight()&&idComboBox.getValue()!=null) {
                    orderService.getAll(idComboBox.getValue())
                            .peek(orders -> tableViewOrders.getItems().addAll(orders))
                            .peekLeft(error -> getPrincipalController().sacarAlertError(error.getMessage()));
                }else {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle(ConstantsErrors.ERROR);
                    a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
                    a.setHeaderText(ConstantsErrors.EMPTYFIELD);
                    a.show();
                }
            } else {
                setTableOrder();
            }
    }
}
