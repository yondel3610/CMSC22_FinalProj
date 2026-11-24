package inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import storage.FileStore;

// Same logic as the ProductFile class (mostly reused the code)
// Saves movements.txt
// one stock movement per line:
// id|dateTime|productId|qtyChange|reason|refId|byUser
public class MovementFile extends FileStore<StockMovement> {

    public MovementFile() {
        super("movements.txt");
    }

    @Override
    protected String toLine(StockMovement m) {
        return m.getId() + "|" + m.getDateTimeText() + "|" + m.getProductId() + "|"
                + m.getQtyChange() + "|" + m.getReason() + "|" + m.getRefId() + "|" + m.getByUser();
    }

    @Override
    protected StockMovement fromLine(String[] f) {
        if (f.length < 7) {
            return null;
        }
        try {
            return new StockMovement(f[0],
                    LocalDateTime.parse(f[1], StockMovement.FMT),
                    f[2], Integer.parseInt(f[3]), f[4], f[5], f[6]);
        } catch (NumberFormatException | DateTimeParseException e) {
            return null;   // skip a broken line instead of crashing
        }
    }
}