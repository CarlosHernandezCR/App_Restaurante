package ui.screens.customer.deletecustomer;

import common.constants.Constants;
import common.constants.ConstantsCustomer;
import common.constants.ConstantsErrors;
import common.constants.ConstantsOrders;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.mongo.Customer;
import model.mongo.Order;
import model.error.Error;
import org.bson.types.ObjectId;
import service.CustomerService;
import service.OrderService;
import ui.screens.common.BaseScreenController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DeleteCustomerController extends BaseScreenController {
    @FXML
    private TableColumn<ObjectId, Customer> idCustomerColumn;
    @FXML
    private TableColumn<String, Customer> firstnameCustomerColumn;
    @FXML
    private  TableColumn<String,Customer> lastnameCustomerColumn;
    @FXML
    private  TableColumn<String,Customer> emailCustomerColumn;
    @FXML
    private  TableColumn<String,Customer> phoneCustomerColumn;
    @FXML
    private TableColumn<LocalDate, Customer> dobCustomerColumn;
    @FXML
    private  TableColumn<Integer, Order> idOrderColumn;
    @FXML
    private  TableColumn<LocalDateTime, Order> dateOrderColumn;
    @FXML
    private  TableColumn<Integer, Order> tableOrderColumn;
    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableView<Order> ordersTable;

    private final CustomerService customerService;
    private final OrderService orderService;

    @Inject
    public DeleteCustomerController(CustomerService customerService,OrderService orderService){
        this.customerService=customerService;
        this.orderService=orderService;
    }
    public void initialize() {
        configureCustomersTable();
        configureOrdersTable();
        loadCustomersList();
        customersTable.setOnMouseClicked(this::loadOrdersForCustomer);
    }

    private void configureCustomersTable() {
        idCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstnameCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        lastnameCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        emailCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
    }

    private void configureOrdersTable() {
        dateOrderColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        tableOrderColumn.setCellValueFactory(new PropertyValueFactory<>("tableId"));
    }

    private void loadCustomersList() {
        customerService.getAll().peek(customers -> customersTable.getItems().addAll(customers))
                .peekLeft(customerError -> getPrincipalController().sacarAlertError(customerError.getMessage()));
    }

    private void loadOrdersForCustomer(MouseEvent event) {
        ordersTable.getItems().clear();
        if (event.getClickCount() == 1 && customersTable.getSelectionModel().getSelectedItem()!=null) {
            orderService.getAll(customersTable.getSelectionModel().getSelectedItem().getId()).peek(orders -> ordersTable.getItems().addAll(orders))
                    .peekLeft(orderError -> getPrincipalController().sacarAlertError(orderError.getMessage()));
        }
    }

    @FXML
    private void deleteCustomer() {
        if(customersTable.getSelectionModel().getSelectedItem()==null){
            getPrincipalController().sacarAlertError(ConstantsErrors.NONCLICKEDCUSTOMER);
        }else {
            Customer c=customersTable.getSelectionModel().getSelectedItem();
            if(customerService.get(c.getId()).isRight()){
                Either<Error,Integer> data=customerService.delete(c,false,false);
                if(data.isLeft()) {
                    confirmDelete();
                }else{
                    customersTable.getItems().clear();
                    ordersTable.getItems().clear();
                    loadCustomersList();
                }
            }
        }
    }

    private void confirmDelete() {

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle(ConstantsCustomer.CUSTOMERWITHORDER);
        confirmationAlert.setHeaderText(ConstantsCustomer.CUSTOMERWITHORDER);
        confirmationAlert.setContentText(ConstantsCustomer.CUSTOMERWITHORDERMESSAGE);
        ButtonType yesButton = new ButtonType(Constants.YES);
        ButtonType noButton = new ButtonType(Constants.NO);
        confirmationAlert.getButtonTypes().setAll(yesButton, noButton);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        Alert confirmationAlert2 = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert2.setTitle(ConstantsOrders.MAKEBACKUP);
        confirmationAlert2.setHeaderText(ConstantsOrders.CONFIRMBACKUP);
        confirmationAlert2.setContentText(ConstantsOrders.CONFIRMBACKUPMESSAGE);
        confirmationAlert2.getButtonTypes().setAll(yesButton, noButton);
        Optional<ButtonType> result2 = confirmationAlert2.showAndWait();

        if (result.isPresent() && (result.get() == yesButton) &&(result2.isPresent()&&(result2.get())==noButton)) {
            Customer c=customersTable.getSelectionModel().getSelectedItem();
            Either<Error,Integer> r=customerService.delete(c,true,false);
            if (r.isLeft()) {
                getPrincipalController().sacarAlertError(r.getLeft().getMessage());
            } else {
                customersTable.getItems().clear();
                loadCustomersList();
            }
        } else if (result.isPresent() && (result.get() == yesButton) &&(result2.isPresent()&&(result2.get())==yesButton)) {
            Customer c=customersTable.getSelectionModel().getSelectedItem();

            Either<Error, Integer> deleteData=customerService.delete(c, true,true);
            if (deleteData.isLeft()) {
                getPrincipalController().sacarAlertError(deleteData.getLeft().getMessage());
            } else {
                customersTable.getItems().clear();
                loadCustomersList();
            }
        }
    }

}
