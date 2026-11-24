package inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// One change in stock. Stock is NEVER edited directly —
// a product's stock is simply the SUM of all its movements.
// Reasons used in this program:
//   INITIAL       - starting stock when the product was created
//   RESTOCK       - manager added stock
//   DONATION      - an item donation came in          (step 2)
//   RELEASE       - goods were given out              (step 3)
//   ADJUSTMENT    - manager corrected the stock after counting
//   VOID_REVERSAL - a transaction was voided, stock is put back
// qtyChange is positive for stock IN and negative for stock out.
// Every movement keeps who did it, so the log doubles as an audit trail.
public class StockMovement {

    public static final String INITIAL = "INITIAL";
    public static final String RESTOCK = "RESTOCK";
    public static final String DONATION = "DONATION";
    public static final String RELEASE = "RELEASE";
    public static final String ADJUSTMENT = "ADJUSTMENT";
    public static final String VOID_REVERSAL = "VOID_REVERSAL";

    // Shared date format for file storage and display.
    public static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String id;
    private final LocalDateTime dateTime;
    private final String productId;
    private final int qtyChange;
    private final String reason;
    private final String refId;    // transaction id that caused it, or ""
    private final String byUser;

    public StockMovement(String id, LocalDateTime dateTime, String productId,
                         int qtyChange, String reason, String refId, String byUser) {
        this.id = id;
        this.dateTime = dateTime;
        this.productId = productId;
        this.qtyChange = qtyChange;
        this.reason = reason;
        this.refId = refId;
        this.byUser = byUser;
    }

    public String getId() { return id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getProductId() { return productId; }
    public int getQtyChange() { return qtyChange; }
    public String getReason() { return reason; }
    public String getRefId() { return refId; }
    public String getByUser() { return byUser; }

    public String getDateTimeText() {
        return dateTime.format(FMT);
    }

    // "+20" or "-5", for the movement log table.
    public String getChangeText() {
        return (qtyChange >= 0 ? "+" : "") + qtyChange;
    }

    // Friendly version of the reason for the movement log.
    public String getReasonLabel() {
        switch (reason) {
            case INITIAL:       return "Starting stock";
            case RESTOCK:       return "Restock";
            case DONATION:      return "Item donation";
            case RELEASE:       return "Release";
            case ADJUSTMENT:    return "Stock correction";
            case VOID_REVERSAL: return "Void reversal";
            default:            return reason;
        }
    }
}