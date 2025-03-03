package service;

import dao.impl.mongo.AggregationsDAO;
import jakarta.inject.Inject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AggregationsService {
    private final AggregationsDAO aggregationsDAO;
    @Inject
    public AggregationsService(){aggregationsDAO=new AggregationsDAO();}

    public String a() {
        ArrayList<Document> result = aggregationsDAO.a();
        StringBuilder stringBuilder = new StringBuilder();
        for (Document document : result) {
            stringBuilder.append(document.toJson()).append("\n");
        }
        return stringBuilder.toString();
    }
    public String b(String customerId) {
        return toString(aggregationsDAO.b(customerId));
    }

    public String c() {
        return toString(aggregationsDAO.c());
    }

    public String d() {
        return toString(aggregationsDAO.d());
    }

    public String e() {
        return toString(aggregationsDAO.e());
    }

    public String f() {
        return toString(aggregationsDAO.f());
    }

    public String g(String customerId) {
        return toString(aggregationsDAO.g(customerId));
    }

    public String h() {
        return toString(aggregationsDAO.h());
    }

    public String i() {
        return toString(aggregationsDAO.i());
    }

    public String j() {
        return toString(aggregationsDAO.j());
    }

    public String k() {
        return toString(aggregationsDAO.k());
    }

    public String l() {
        return toString(aggregationsDAO.l());
    }

    public String m() {
        return toString(aggregationsDAO.m());
    }
    public String aa() {
        return toString(aggregationsDAO.aa());
    }
    public String bb() {
        return toString(aggregationsDAO.bb());
    }
    public String cc() {
        return toString(aggregationsDAO.cc());
    }
    public String dd() {
        return toString(aggregationsDAO.dd());
    }
    public String ee() {
        return toString(aggregationsDAO.ee());
    }

    private String toString(List<Document> documents) {
        StringBuilder result = new StringBuilder();
        for (Document document : documents) {
            result.append(document.toJson()).append("\n");
        }
        return result.toString();
    }

}
