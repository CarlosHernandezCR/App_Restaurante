package ui.screens.customer.showcustomers;

import io.vavr.control.Either;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.mongo.Customer;
import model.mongo.Order;
import org.bson.types.ObjectId;
import service.CustomerService;
import service.OrderService;
import ui.screens.common.BaseScreenController;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ShowCustomerController extends BaseScreenController {
    private final CustomerService customerService;
    private final OrderService orderService;
    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<ObjectId, Customer> idCustomerColumn;
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
    @Inject
    private ShowCustomerController(CustomerService customerService,OrderService orderService) {
        this.customerService = customerService;
        this.orderService=orderService;
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
        setTables();
    }

    private void setTables() {
        customersTable.getItems().clear();
        customerService.getAll().peek(customers -> customersTable.getItems().addAll(customers))
                .peekLeft(customerError -> getPrincipalController().sacarAlertError(customerError.getMessage()));
    }

    public void saveInMongo(ActionEvent actionEvent) {
        Either<model.error.Error, List<model.Customer>> data=customerService.getAllHibernate();

        if(data.isRight()){
            for (int i = 0; i < data.get().size(); i++) {
                model.Customer customer=data.get().get(i);
                Either<model.error.Error, List<model.Order>> data2=orderService.getAllHibernate(customer.getId());
                if(data2.isRight()){
                    List<model.mongo.Order> ordersMongo=transformToMongo(data2.get());
                    model.mongo.Customer customerMongo=new model.mongo.Customer(customer.getFirst_name(),customer.getLast_name(),customer.getEmail(),customer.getPhone(),customer.getDob(),ordersMongo);
                    customerService.add(customerMongo,customer.getCredentials().getUser(),customer.getCredentials().getPassword());
                }else{
                    model.mongo.Customer customerMongo=new model.mongo.Customer(customer.getFirst_name(),customer.getLast_name(),customer.getEmail(),customer.getPhone(),customer.getDob());
                    customerService.add(customerMongo,customer.getCredentials().getUser(),customer.getCredentials().getPassword());
                }
            }
            setTables();
        }else getPrincipalController().sacarAlertError("Error al cargar los Customers del Hibernate");
    }

    private List<Order> transformToMongo(List<model.Order> orders) {
        List<Order> result=new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            result.add(new model.mongo.Order(orders.get(i)));
        }
        return result;
    }
}
