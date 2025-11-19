package models;

public class RegularCustomer extends Customer {
    public RegularCustomer(String name) {
        super(name, "Regular");
    }

    @Override
    public double calculateTotal(int gallons) {
        return gallons * 35.00;
    }
}