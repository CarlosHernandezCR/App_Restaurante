package ui.screens.principal;


import common.constants.Constants;
import common.constants.ConstantsErrors;
import dao.DBConnectionPool;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.mongo.Credentials;
import ui.screens.common.BaseScreenController;
import ui.screens.common.Screens;

import java.io.IOException;
@Singleton
public class PrincipalController {
    private final Alert alert;
    public Credentials actualUser;
    @FXML
    public BorderPane root;
    final Instance<Object> instance;
    private Stage primaryStage;

    @FXML
    private MenuBar menuPrincipal;
    @FXML
    private MenuItem addOrder;
    @FXML
    private MenuItem deleteOrder;
    @FXML
    private Menu menuCustomers;
    private final DBConnectionPool dbConnectionPool;

    @Inject
    public PrincipalController(Instance<Object> instance,DBConnectionPool dbConnectionPool) {
        this.instance = instance;
        alert = new Alert(Alert.AlertType.NONE);
        this.dbConnectionPool=dbConnectionPool;
    }

    public void initialize() {
        menuPrincipal.setVisible(false);
        cargarPantalla(Screens.LOGIN);
    }

    private void cargarPantalla(Screens pantalla) {
        cambioPantalla(cargarPantalla(pantalla.getRoute()));
    }

    private Pane cargarPantalla(String ruta) {
        Pane panePantalla = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(controller -> instance.select(controller).get());
            panePantalla = fxmlLoader.load(getClass().getResourceAsStream(ruta));
            BaseScreenController pantallaController = fxmlLoader.getController();
            pantallaController.setPrincipalController(this);
            pantallaController.principalCargado();
        } catch (IOException e) {
            System.out.println(e);
        }
        return panePantalla;
    }

    public void sacarAlertError(String mensaje) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setContentText(mensaje);
        alert.getDialogPane().setId("alert");
        alert.getDialogPane().lookupButton(ButtonType.OK).setId("btn-ok");
        alert.showAndWait();
    }
    @FXML
    public void doLogin(Credentials actualUser) {
        this.actualUser = actualUser;
        if(actualUser.getUsername().compareTo(Constants.ROOT)==0) {
            addOrder.setVisible(false);
            deleteOrder.setVisible(true);
            menuCustomers.setVisible(true);
        }else{
            addOrder.setVisible(true);
            deleteOrder.setVisible(false);
            menuCustomers.setVisible(false);
        }
        BaseScreenController.id=actualUser.getId();
        menuPrincipal.setVisible(true);
        cargarPantalla(Screens.WELCOME);
    }
    @FXML
    private void logOut(ActionEvent actionEvent) {
        actualUser = null;
        cargarPantalla(Screens.LOGIN);
        menuPrincipal.setVisible(false);
    }

    private void cambioPantalla(Pane pantallaNueva) {
        root.setCenter(pantallaNueva);
    }

    public void exit() {
        Platform.exit();
        dbConnectionPool.closePool();
    }

    public void setStage(Stage stage) {
        primaryStage = stage;
    }

    public void menuCustomers(ActionEvent actionEvent) {
        switch (((MenuItem) actionEvent.getSource()).getId()) {
            case "listCustomers" -> cargarPantalla(Screens.LISTCUSTOMER);
            case "addCustomer" -> cargarPantalla(Screens.ADDCUSTOMER);
            case "updateCustomer" -> cargarPantalla(Screens.UPDATECUSTOMER);
            case "deleteCustomer" -> cargarPantalla(Screens.DELETECUSTOMER);
            default -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle(ConstantsErrors.ERROR);
                a.setContentText(ConstantsErrors.SCREENERROR);
                a.setHeaderText(null);
                a.show();
            }
        }
    }

    public void menuOrders(ActionEvent actionEvent) {
        switch (((MenuItem) actionEvent.getSource()).getId()) {
            case "listOrders" -> cargarPantalla(Screens.LISTORDERS);
            case "addOrder" -> cargarPantalla(Screens.ADDORDERS);
            case "updateOrder" -> cargarPantalla(Screens.UPDATEORDERS);
            case "deleteOrder" -> cargarPantalla(Screens.DELETEORDERS);
            default -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle(ConstantsErrors.ERROR);
                a.setContentText(ConstantsErrors.SCREENERROR);
                a.setHeaderText(null);
                a.show();
            }
        }
    }
}
