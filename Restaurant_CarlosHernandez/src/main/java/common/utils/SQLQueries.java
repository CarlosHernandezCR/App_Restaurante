package common.utils;

public class SQLQueries {
    public static final String UPDATECUSTOMER = "UPDATE customers SET ";
    public static final String UPDATEFNAME= "first_name = ?, ";
    public static final String UPDATELNAME= "last_name = ?, ";
    public static final String UPDATEEMAIL= "email = ?, ";
    public static final String UPDATEPHONE= "phone = ?, ";
    public static final String UPDATEDOB= "dob = ?, ";
    public static final String UPDATEWHERE= " WHERE id = ?";


    public static final String INSERTCUSTOMER = "INSERT INTO customers (first_name, last_name, email, phone, date_of_birth) VALUES ( ?, ?, ?, ?, ?)";
    public static final String INSERTTABLE = "INSERT INTO restaurant_tables (table_number_id, number_of_seats) VALUES (?, ?)";
    public static final String INSERTCREDENTIAL = "INSERT INTO credentials(customer_id,user_name,password) VALUES(?,?,?)";


    public static final String GETALLCUSTOMERS = "select * from customers";
    public static final String GETALLTABLES = "select * from restaurant_tables";
    public static final String GETALLORDERS = "select * from orders";
    public static final String GETALLORDERITEMS = "select * from order_items";
    public static final String GETALLMENUITEMS = "select * from menu_items";





    public static final String DELETECREDENTIALWITHIDCUSTOMER="DELETE FROM credentials WHERE customer_id = ?";
    public static final String DELETEORDERITEMWITHORDERID="DELETE FROM order_items WHERE order_id IN ";
    public static final String SELECTORDERIDWITHCUSTOMERID="(SELECT order_id FROM orders WHERE customer_id = ?)";
    public static final String DELETEORDERWITHCUSTOMERIDGETED="DELETE FROM orders WHERE customer_id = ?";
    public static final String DELETECUSTOMERWITHID="DELETE FROM customers WHERE id = ?";
    public static final String UPDATEORDER = "UPDATE orders SET ";
    public static final String UPDATEDOBORDER= "order_date = ?, ";
    public static final String UPDATECUSTOMERIDORDER= "customer_id = ?, ";
    public static final String UPDATETABLEIDORDER= "table_id = ?, ";
    public static final String INSERTORDER ="INSERT INTO orders (order_date, customer_id, table_id) VALUES (?, ?, ?)";
    public static final String DELETEORDERITEMSWITHIDORDER = "DELETE FROM order_items WHERE order_id = ?";
    public static final String GETMENUITEMSBYID = "SELECT * FROM menu_items WHERE menu_item_id = ?";
    public static final String DELETEORDERWITHIDORDER = "DELETE FROM orders WHERE order_id = ?";
    public static final String INSERTORDERITEM = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";
    public static final String UPDATEWHEREORDER = " WHERE order_id = ?";
    public static final String GETCUSTOMER = "SELECT * FROM customers WHERE id = ?";
    public static final String GETORDERITEM = "SELECT * FROM order_items WHERE order_id = ?";
    public static final String GETORDERWITHCUSTOMERID = "SELECT * FROM orders WHERE customer_id = ?";
    public static final String GETMENUITEMSBYNAME = "SELECT * FROM menu_items WHERE name = ?";
    public static final String GETALLCREDENTIALS = "SELECT * FROM credentials";
    public static final String GETCUSTOMERNAMEWITHID = "SELECT first_name FROM customers WHERE id = ?";
    public static final String SELECTORDERSWITHCUSTOMERIDGETED = "SELECT * FROM orders WHERE customer_id = ?";
    public static final String GETCREDENTIALS = "SELECT * FROM Credentials WHERE username=? AND password=?";


    private SQLQueries(){}

}
