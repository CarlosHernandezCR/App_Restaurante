package ui.screens.orders.updateorder;

import common.constants.ConstantsErrors;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.Restaurant_tables;
import model.mongo.*;
import model.error.Error;
import model.mongo.MenuItem;
import org.bson.types.ObjectId;
import service.*;
import ui.screens.common.BaseScreenController;
import ui.screens.orders.showorder.ShowOrderController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateOrderController extends BaseScreenController {
    @FXML
    private TableView<Order> tableViewOrders;
    @FXML
    private TableColumn<Order,LocalDateTime> dateOrder;
    @FXML
    private TableColumn<Order,Integer> idTable;

    @FXML
    private TableView<ShowOrderController.DataTable> tableViewOrderItems;
    @FXML
    private TableColumn<ShowOrderController.DataTable,String> nameItem;
    @FXML
    private TableColumn<ShowOrderController.DataTable,Double> priceItem;
    @FXML
    private TableColumn<ShowOrderController.DataTable,Integer> quantityItem;
    @FXML
    private TableColumn<ShowOrderController.DataTable,Double> totalItem;
    @FXML
    private Button updateButton;
    private final OrderService orderService;
    private final MenuItemService menuItemService;
    private final TableService tableService;
    @FXML
    private Label total;
    @FXML
    private ComboBox<Integer>tablesCombobox;
    @FXML
    private ComboBox<String>itemsComboBox;
    @FXML
    private ComboBox<Integer> quantity;
    private LocalDateTime date;
    List<OrderItem> orderItemsOriginal = new ArrayList<>();
    @Inject
    public UpdateOrderController(OrderService orderService, MenuItemService menuItemService,TableService tableService){
        this.orderService = orderService;
        this.menuItemService=menuItemService;
        this.tableService=tableService;
    }

    public void initialize(){
        dateOrder.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        idTable.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        setTableOrder();
        nameItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceItem.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityItem.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalItem.setCellValueFactory(new PropertyValueFactory<>("total"));
        tableViewOrders.setOnMouseClicked(this::clickOrder);
        itemsComboBox.getItems().addAll(getItems());
        tablesCombobox.getItems().addAll(getTables());
        updateButton.setVisible(false);
        for (int i = 1; i < 11; i++) {
            quantity.getItems().add(i);
        }
    }

    public void clickOrder(MouseEvent event) {
        if (event.getClickCount() == 1) {
            if(tableViewOrders.getSelectionModel().getSelectedItem()!=null) {
                Either<Error, Order> result = orderService.get(tableViewOrders.getSelectionModel().getSelectedItem().getOrderDate());
                tablesCombobox.setValue(tablesCombobox.getItems().get(result.get().getTableId()));
                date=tableViewOrders.getSelectionModel().getSelectedItem().getOrderDate();
                if (result.isRight()) {
                    updateButton.setVisible(true);
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


    private void setTableOrder() {
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
    private List<Integer> getTables() {
        ArrayList<Integer>ids=new ArrayList<>();
        Either<Error,List<Restaurant_tables>>data=tableService.getAll();
        if(data.isRight()) {
            for (Restaurant_tables t : data.get())
                ids.add(t.getId());
        }else{
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.FILEREADINGERRORCUSTOMER);
            a.setHeaderText(ConstantsErrors.FILEREADINGERROR);
            a.show();
        }
        return ids;
    }
    public void update(){
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i=0;i<tableViewOrderItems.getItems().size();i++) {
            MenuItem menuItem = menuItemService.get(tableViewOrderItems.getItems().get(i).getName()).get();
            OrderItem orderItem = new OrderItem(menuItem.getId(), tableViewOrderItems.getItems().get(i).getQuantity());
            orderItems.add(orderItem);
        }
        deleteDuplicate(orderItems);
        if (tablesCombobox.getValue() != null) {
            Either<Error,Integer>result= orderService.update(new Order(date, tablesCombobox.getValue(), orderItems));
            if (result.isRight()) {
                tableViewOrderItems.getItems().clear();
                tableViewOrders.getItems().clear();
                total.setText("");
                setTableOrder();
            } else {
                getPrincipalController().sacarAlertError(result.getLeft().getMessage());
            }

        }else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
            a.setHeaderText(ConstantsErrors.EMPTYFIELDMESSAGE);
            a.show();
        }
    }

    public static void deleteDuplicate(List<OrderItem> orderItems) {
        for (int i = 0; i < orderItems.size(); i++) {
            for (int j = 1; j < orderItems.size(); j++) {
                if(i!=j && Objects.equals(orderItems.get(i).getMenuItemId(), orderItems.get(j).getMenuItemId())) {
                    orderItems.get(i).setQuantity(orderItems.get(i).getQuantity() + orderItems.get(j).getQuantity());
                    orderItems.remove(orderItems.get(j));
                }
            }
        }
    }

    public void addMenuItem() {
        if(itemsComboBox.getValue()!=null&&quantity.getValue()!=null){
            Either<Error,MenuItem> data=menuItemService.get(itemsComboBox.getValue());
            if(data.isRight()){
                tableViewOrderItems.getItems().add(new ShowOrderController.DataTable(data.get().getName(),data.get().getPrice(),quantity.getValue(),Math.round((data.get().getPrice() * quantity.getValue()) * 100.0) / 100.0));
                quantity.setValue(1);
                double sumaTotal = 0.0;
                sumaTotal = getSumaTotal(sumaTotal);
                total.setText(String.format("%.2f", sumaTotal));
            }else{
                data.peekLeft(ordersError -> getPrincipalController().sacarAlertError(ordersError.getMessage()));
            }
        }else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
            a.setHeaderText(ConstantsErrors.EMPTYLISTMENUITEMERRORMESSAGE);
            a.show();
        }
    }

    private double getSumaTotal(double sumaTotal) {
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
        return sumaTotal;
    }

    public void removeMenuItem() {
        ShowOrderController.DataTable selectedItem = tableViewOrderItems.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            tableViewOrderItems.getItems().remove(selectedItem);
        }else{
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.NONCLICKEDMENUITEMMESSAGE);
            a.setHeaderText(ConstantsErrors.NONCLICKEDMENUITEM);
            a.show();
        }
    }
    private List<String> getItems() {
        Either<Error,List<MenuItem>>data=menuItemService.getAll();
        ArrayList<String>names=new ArrayList<>();
        if(data.isRight()) {
            for (MenuItem m : data.get())
                names.add(m.getName());
        }else{
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.FILEREADINGERRORCUSTOMER);
            a.setHeaderText(ConstantsErrors.FILEREADINGERROR);
            a.show();
        }
        return names;
    }
}
