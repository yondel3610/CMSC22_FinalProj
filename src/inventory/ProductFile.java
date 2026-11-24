package inventory;

import storage.FileStore;

// Saves products.txt
// one product per line:
// id|name|description|category|unit|unitValue|reorderLevel|active
public class ProductFile extends FileStore<Product> {

    public ProductFile() {
        super("products.txt");
    }

    @Override
    protected String toLine(Product p) {
        return p.getId() + "|" + p.getName() + "|" + p.getDescription() + "|"
                + p.getCategory() + "|" + p.getUnit() + "|" + p.getUnitValue()
                + "|" + p.getReorderLevel() + "|" + p.isActive();
    }

    @Override
    protected Product fromLine(String[] f) {
        if (f.length < 8) {
            return null;
        }
        try {
            return new Product(f[0], f[1], f[2], f[3], f[4],
                    Double.parseDouble(f[5]), Integer.parseInt(f[6]),
                    Boolean.parseBoolean(f[7]));
        } catch (NumberFormatException e) {
            return null;   // skip a broken line instead of crashing
        }
    }
}