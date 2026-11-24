package inventory;

// One product in the LGU's catalog, e.g. rice, canned goods, seedlings.
// Products are never deleted. When they stop being used they are
// archived instead, so old donation and release records still point to a valid product.
public class Product {

    public static final String[] CATEGORIES =
            { "Grain", "Vegetable", "Seed", "Tool", "Other" };
    public static final String[] UNITS =
            { "kg", "sack", "pack", "piece", "bottle" };

    private String id;
    private String name;
    private String description;
    private String category;
    private String unit;
    private double unitValue;   // estimated value per unit, in PHP
    private int reorderLevel;   // stock at or below this level = LOW
    private boolean active;

    public Product(String id, String name, String description, String category,
                   String unit, double unitValue, int reorderLevel, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unit = unit;
        this.unitValue = unitValue;
        this.reorderLevel = reorderLevel;
        this.active = active;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public double getUnitValue() { return unitValue; }
    public int getReorderLevel() { return reorderLevel; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setUnitValue(double unitValue) { this.unitValue = unitValue; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name;   // handy when shown inside ComboBoxes
    }
}