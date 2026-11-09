
package manager;

public class Donation {

    private String name;
    private String type;
    private String quantity;
    private String unit;

    public Donation(String name, String type,String quantity, String unit) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
