package models;

public class Reseller extends Customer {
    public Reseller(String name) {
        super(name, "Reseller");
    }

    @Override
    public double calculateTotal(int gallons) {
        return gallons * 25.00;
    }
}