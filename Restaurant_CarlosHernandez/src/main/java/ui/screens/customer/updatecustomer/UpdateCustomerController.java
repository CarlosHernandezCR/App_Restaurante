package ui.screens.customer.updatecustomer;

import common.constants.ConstantsErrors;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.mongo.Customer;
import org.bson.types.ObjectId;
import service.CustomerService;
import ui.screens.common.BaseScreenController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class UpdateCustomerController extends BaseScreenController {
    private final CustomerService servicesCustomers;
    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<Integer, Customer> idCustomerColumn;
    @FXML
    private TableColumn<String, Customer> firstnameCustomerColumn;
    @FXML
    private TableColumn<String, Customer> lastnameCustomerColumn;
    @FXML
    private TableColumn<String, Customer> emailCustomerColumn;
    @FXML
    private TableColumn<String, Customer> phoneCustomerColumn;
    @FXML
    private TableColumn<Date, Customer> dobCustomerColumn;
    @FXML
    private TextField fNameField;
    @FXML
    private TextField lNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker dobField;
    private boolean clicked=false;

    @Inject
    public UpdateCustomerController(CustomerService servicesCustomers) {
        this.servicesCustomers = servicesCustomers;
    }

    public void initialize(){
        idCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstnameCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        lastnameCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        emailCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        customersTable.setOnMouseClicked(this::clickTable);
    }

    private void clickTable(MouseEvent event) {
        if (event.getClickCount() == 1) {
            Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                fNameField.setText(selectedCustomer.getFirst_name());
                lNameField.setText(selectedCustomer.getLast_name());
                emailField.setText(selectedCustomer.getEmail());
                phoneField.setText(selectedCustomer.getPhone());
                dobField.setValue(selectedCustomer.getDob());
                clicked=true;
            }
        }
    }

    @Override
    public void principalCargado(){
        setTable();
    }

    private void setTable() {
        customersTable.getItems().clear();
        servicesCustomers.getAll().peek(customers -> customersTable.getItems().addAll(customers))
                .peekLeft(customerError -> getPrincipalController().sacarAlertError(customerError.getMessage()));
    }

    public void updateCustomer(){
        if(clicked) {
            if (fNameField.getText().isEmpty() || lNameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty() || dobField.getValue() == null) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle(ConstantsErrors.ERROR);
                a.setContentText(ConstantsErrors.EMPTYFIELDMESSAGE);
                a.setHeaderText(ConstantsErrors.EMPTYFIELD);
                a.show();
            } else {
                LocalDate selectedDate = dobField.getValue();
                Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (servicesCustomers.update(new Customer(new ObjectId(String.valueOf(customersTable.getSelectionModel().getSelectedItem().getId())), fNameField.getText(), lNameField.getText(), emailField.getText(), phoneField.getText(), date)).isRight()) {
                    setTable();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle(ConstantsErrors.ERROR);
                    a.setContentText(ConstantsErrors.UPDATINGERRORCUSTOMER);
                    a.setHeaderText(ConstantsErrors.UPDATINGERROR);
                    a.show();
                }
                fNameField.clear();
                lNameField.clear();
                emailField.clear();
                phoneField.clear();
                dobField.setValue(null);
            }
        }else{
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(ConstantsErrors.ERROR);
            a.setContentText(ConstantsErrors.UPDATINGERRORCUSTOMER);
            a.setHeaderText(ConstantsErrors.UPDATINGERROR);
            a.show();
        }
    }
}
