package inventory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Data manager, the product catalog + every stock movement.
// STOCK IS NEVER EDITED DIRECTLY.
// A product's stock is always the SUM of all its StockMovements.
// That gives us a free audit trail and makes voiding transactions safe
// because a void simply adds a reversing movement.
public class Inventory {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_LOW = "LOW";
    public static final String STATUS_OUT = "OUT";

    private List<Product> products;
    private List<StockMovement> movements;

    private ProductFile productFile = new ProductFile();
    private MovementFile movementFile = new MovementFile();

    public Inventory() {
        products = productFile.loadAll();
        movements = movementFile.loadAll();
        seedIfEmpty();
    }

    // First-run seed data (written only when products.txt doesn't exist yet)
    private void seedIfEmpty() {
        if (!products.isEmpty()) {
            return;
        }
        addProduct("Rice", "Well-milled rice for relief packs", "Grain", "sack", 2150.00, 10, 20, "system");
        addProduct("Canned Goods", "Assorted canned meat, fish and vegetables", "Other", "pack", 45.00, 24, 50, "system");
        addProduct("Seedlings", "Vegetable seedlings for backyard farming", "Seed", "pack", 30.00, 20, 100, "system");
    }

    // Product catalog
    public List<Product> getProducts() { return products; }
    public List<StockMovement> getMovements() { return movements; }

    public Product findProduct(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // True if some other product already uses this name (names must be unique).
    public boolean nameExists(String name, String ignoreId) {
        for (Product p : products) {
            if (!p.getId().equals(ignoreId) && p.getName().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    public Product addProduct(String name, String description, String category, String unit,
                              double unitValue, int reorderLevel, int initialQty, String byUser) {
        validate(name, description, unitValue, reorderLevel);
        if (nameExists(name, "")) {
            throw new IllegalArgumentException("A product named \"" + name.trim()
                    + "\" already exists. Product names must be unique.");
        }
        Product p = new Product(nextProductId(), name.trim(), description.trim(),
                category, unit, unitValue, reorderLevel, true);
        products.add(p);
        if (initialQty > 0) {
            movements.add(new StockMovement(nextMovementId(), LocalDateTime.now(),
                    p.getId(), initialQty, StockMovement.INITIAL, "", byUser));
        }
        save();
        return p;
    }

    public void updateProduct(Product p, String name, String description, String category,
                              String unit, double unitValue, int reorderLevel) {
        validate(name, description, unitValue, reorderLevel);
        if (nameExists(name, p.getId())) {
            throw new IllegalArgumentException("A product named \"" + name.trim()
                    + "\" already exists. Product names must be unique.");
        }
        p.setName(name.trim());
        p.setDescription(description.trim());
        p.setCategory(category);
        p.setUnit(unit);
        p.setUnitValue(unitValue);
        p.setReorderLevel(reorderLevel);
        save();
    }

    // Products are archived, never deleted
    // old records stay valid regardless
    public void setArchived(Product p, boolean archived) {
        p.setActive(!archived);
        save();
    }
    
    // validate 
    private void validate(String name, String description, double unitValue, int reorderLevel) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (name.contains("|") || (description != null && description.contains("|"))) {
            throw new IllegalArgumentException("The \"|\" character is not allowed in product details.");
        }
        if (unitValue < 0) {
            throw new IllegalArgumentException("Unit value cannot be negative.");
        }
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative.");
        }
    }

    private String nextProductId() {
        int max = 1000;
        for (Product p : products) {
            try {
                max = Math.max(max, Integer.parseInt(p.getId().substring(1)));
            } catch (RuntimeException e) {
                // id not in the expected P#### format - ignore it
            }
        }
        return "P" + (max + 1);
    }

    private String nextMovementId() {
        int max = 2000;
        for (StockMovement m : movements) {
            try {
                max = Math.max(max, Integer.parseInt(m.getId().substring(1)));
            } catch (RuntimeException e) {
                // ignore
            }
        }
        return "M" + (max + 1);
    }

    // Stock (always derived from movements)

    public int stockOf(String productId) {
        int total = 0;
        for (StockMovement m : movements) {
            if (m.getProductId().equals(productId)) {
                total += m.getQtyChange();
            }
        }
        return total;
    }
    
    // Total value of everything in stock (active products only):
    // sum of stock x unit value, in PHP. Same figure the Inventory screen shows.
    public double totalProductValue() {
        double total = 0;
        for (Product p : products) {
            if (p.isActive()) {
                total += stockOf(p.getId()) * p.getUnitValue();
            }
        }
        return total;
    }

    public String statusOf(Product p) {
        int stock = stockOf(p.getId());
        if (stock <= 0) {
            return STATUS_OUT;
        }
        if (stock <= p.getReorderLevel()) {
            return STATUS_LOW;
        }
        return STATUS_OK;
    }

    // Manager adds stock.
    public void restock(Product p, int qty, String byUser) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Restock quantity must be more than 0.");
        }
        recordMovement(p.getId(), qty, StockMovement.RESTOCK, "", byUser);
    }

    // Manager counted the physical stock; we record the difference as a correction.
    public void adjustStock(Product p, int countedQty, String byUser) {
        int diff = countedQty - stockOf(p.getId());
        if (diff == 0) {
            throw new IllegalArgumentException("The counted stock matches the system stock. Nothing to adjust.");
        }
        recordMovement(p.getId(), diff, StockMovement.ADJUSTMENT, "", byUser);
    }

    // Shared entry point used by later milestones too:
    // step 2 item donations (DONATION), step 3 releases (RELEASE)
    // and void reversals (VOID_REVERSAL) all record movements here.
    public void recordMovement(String productId, int qtyChange, String reason, String refId, String byUser) {
        movements.add(new StockMovement(nextMovementId(), LocalDateTime.now(),
                productId, qtyChange, reason, refId, byUser));
        save();
    }

    // All movements of one product, oldest first (reads like a ledger).
    public List<StockMovement> movementsFor(String productId) {
        List<StockMovement> result = new ArrayList<>();
        for (StockMovement m : movements) {
            if (m.getProductId().equals(productId)) {
                result.add(m);
            }
        }
        result.sort(Comparator.comparing(StockMovement::getDateTime));
        return result;
    }

    // Persistence
    private void save() {
        productFile.saveAll(products);
        movementFile.saveAll(movements);
    }
}