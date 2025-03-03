package ui.screens.login;

import common.constants.ConstantsErrors;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.CredentialsService;
import ui.screens.common.BaseScreenController;

public class LoginController extends BaseScreenController{

    final CredentialsService credentialsService;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;

    @Inject
    public  LoginController(CredentialsService credentialsService){
        this.credentialsService = credentialsService;
    }
    @FXML
    private void login(){
        credentialsService.checkLogin(username.getText(),password.getText()).peek(credentials -> getPrincipalController().doLogin(credentials))
                .peekLeft(error -> getPrincipalController().sacarAlertError(ConstantsErrors.WRONG_PASSWORD));
    }


}
