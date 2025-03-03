package ui.screens.common;

import lombok.Getter;
import org.bson.types.ObjectId;
import ui.screens.principal.PrincipalController;

import java.io.IOException;

public abstract class BaseScreenController {
    public static ObjectId id;
    @Getter
    private PrincipalController principalController;

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    public void principalCargado() throws IOException {

    }
}
