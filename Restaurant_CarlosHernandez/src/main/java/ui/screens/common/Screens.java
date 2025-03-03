package ui.screens.common;

public enum Screens {
    LOGIN ("/fxml/login.fxml"),
    WELCOME("/fxml/welcome.fxml"),
    LISTCUSTOMER("/fxml/customer/showCustomers.fxml"),
    ADDCUSTOMER("/fxml/customer/addCustomer.fxml"),
    UPDATECUSTOMER("/fxml/customer/updateCustomer.fxml"),
    DELETECUSTOMER("/fxml/customer/deleteCustomer.fxml"),
    LISTORDERS("/fxml/orders/showOrders.fxml"),
    ADDORDERS("/fxml/orders/addOrder.fxml"),
    UPDATEORDERS("/fxml/orders/updateOrder.fxml"),
    DELETEORDERS("/fxml/orders/deleteOrder.fxml");

    private final String route;
    Screens(String route) {
        this.route = route;
    }
    public String getRoute(){return route;}


}
