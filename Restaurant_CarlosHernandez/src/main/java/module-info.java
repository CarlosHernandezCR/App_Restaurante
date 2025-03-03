module RestaurantJDBC_CarlosHernandez {
    requires lombok;
    requires jakarta.inject;
    requires io.vavr;
    requires jakarta.xml.bind;
    requires java.sql;
    requires jakarta.persistence;
    requires mongo.java.driver;
    requires com.zaxxer.hikari;
    requires jakarta.annotation;
    requires commons.dbcp2;
    requires com.google.gson;
    requires jakarta.cdi;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;


    exports ui.main to javafx.graphics;
    exports ui.screens.principal;
    exports ui.screens.login;
    exports ui.screens.common;
    exports ui.screens.customer.showcustomers;
    exports ui.screens.customer.addcustomer;
    exports ui.screens.customer.deletecustomer;
    exports ui.screens.customer.updatecustomer;
    exports ui.screens.orders.showorder;
    exports ui.screens.orders.addorder;
    exports ui.screens.orders.deleteorder;
    exports ui.screens.orders.updateorder;
    exports service;
    exports model.error;
    exports dao.impl.Hibernate;
    exports ui.screens.welcome;
    exports common.config;
    exports model;
    exports dao;
    exports model.XML;
    exports model.mongo;
    exports dao.impl.mongo;
    exports dao.impl.JDBC;

    opens ui.screens.login;
    opens ui.screens.principal;
    opens ui.screens.customer.showcustomers;
    opens ui.screens.customer.addcustomer;
    opens ui.screens.customer.deletecustomer;
    opens ui.screens.customer.updatecustomer;
    opens ui.screens.orders.showorder;
    opens ui.screens.orders.addorder;
    opens ui.screens.orders.deleteorder;
    opens ui.screens.orders.updateorder;
    opens ui.main;
    opens css;
    opens fxml;
    opens service;
    opens common.constants;
    opens common.config;
    opens common.utils;
    opens config;
    opens dao;
    opens model;
    opens model.XML;
    opens model.mongo;

}