package ui.screens.customer.addcustomer;

import common.constants.ConstantsErrors;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.mongo.Customer;
import model.error.Error;
import service.CustomerService;
import ui.screens.common.BaseScreenController;
import java.time.LocalDate;
import java.util.Date;

public class AddCustomerController extends BaseScreenController {
    private final CustomerService servicesCustomers;
    @FXML
    TableView<Customer> customersTable;
    @FXML
    public TableColumn<Integer, Customer> idCustomerColumn;
    @FXML
    public TableColumn<String, Customer> firstnameCustomerColumn;
    @FXML
    public TableColumn<String, Customer> lastnameCustomerColumn;
    @FXML
    public TableColumn<String, Customer> emailCustomerColumn;
    @FXML
    public TableColumn<String, Customer> phoneCustomerColumn;
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
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField usernameField;

    @Inject
    public AddCustomerController(CustomerService servicesCustomers) {
        this.servicesCustomers = servicesCustomers;
    }

    public void initialize(){
        idCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstnameCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        lastnameCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        emailCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
    }

    @Override
    public void principalCargado(){
        setTable();
    }

    private void setTable() {
        customersTable.getItems().clear();
        customersTable.getItems().addAll(servicesCustomers.getAll().get());
    }

    public void addCustomer() {
        if (fNameField.getText().isEmpty() || lNameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty() || dobField.getValue() == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText(ConstantsErrors.EMPTYFIELD);
            a.show();
        } else {
            Either<Error, Integer> r1 = servicesCustomers.add(new Customer(fNameField.getText(), lNameField.getText(), emailField.getText(), phoneField.getText(), dobField.getValue()),usernameField.getText(),passwordField.getText());
            if (r1.isRight()) {
                setTable();
                fNameField.clear();
                lNameField.clear();
                emailField.clear();
                phoneField.clear();
                dobField.setValue(null);
                usernameField.clear();
                passwordField.clear();
            } else {
                getPrincipalController().sacarAlertError(r1.getLeft().getMessage());
                if(r1.getLeft().getMessage().compareTo(ConstantsErrors.USERNAMEREPEATED)==0)
                    usernameField.clear();
            }
        }
    }

}
