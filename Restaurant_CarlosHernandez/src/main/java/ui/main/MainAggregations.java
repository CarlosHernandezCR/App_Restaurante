package ui.main;

import service.AggregationsService;

public class MainAggregations {
    public static void main(String[] args) {
        AggregationsService service = new AggregationsService();
        String customerId="65c7bbed7fb5267d64e3017f";
        System.out.println("Exercise a:");
        System.out.println(service.a());

        System.out.println("Exercise b:");
        System.out.println(service.b(customerId));

        System.out.println("Exercise c:");
        System.out.println(service.c());

        System.out.println("Exercise d:");
        System.out.println(service.d());

        System.out.println("Exercise e:");
        System.out.println(service.e());

        System.out.println("Exercise f:");
        System.out.println(service.f());

        System.out.println("Exercise g:");
        System.out.println(service.g(customerId));

        System.out.println("Exercise h:");
        System.out.println(service.h());

        System.out.println("Exercise i:");
        System.out.println(service.i());

        System.out.println("Exercise j: (I don't have unique items not requested more than once)");
        System.out.println(service.j());

        System.out.println("Exercise k:");
        System.out.println(service.k());

        System.out.println("Exercise l:");
        System.out.println(service.l());

        System.out.println("Exercise m:");
        System.out.println(service.m());

        System.out.println("\n Airplane database(json file in folder data):");
        System.out.println("Exercise 1:\nProvide the manufacturers with the smallest number of types of armament, and the length of the armament list and the total count of armaments.");
        System.out.println(service.aa());
        System.out.println("Exercise 2:\nAircraft name with unit cost adjusted for inflation:");
        System.out.println(service.bb());
        System.out.println("Exercise 3:\nAverage passenger capacity by manufacturer:");
        System.out.println(service.cc());
        System.out.println("Exercise 4:\nManufacturer along with the total number of armaments for each one:");
        System.out.println(service.dd());
        System.out.println("Exercise 5:\nThis aggregation returns the total cost of the commercial aircrafts.");
        System.out.println(service.ee());
    }
}
