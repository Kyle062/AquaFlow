package models;

public abstract class Customer {
    protected String name;
    protected String type;

    public Customer(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public abstract double calculateTotal(int gallons);
}