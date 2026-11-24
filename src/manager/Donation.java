package manager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// One recorded donation: who gave it, how to reach them, what was given,
// when, and who logged it.
public class Donation {

    // Shared date format for the file and the History table.
    public static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Shown for anonymous donors and for donations saved before this info was tracked.
    public static final String UNKNOWN = "\u2014";
    // Donor name saved when the donor wants to stay anonymous.
    public static final String ANONYMOUS = "Anonymous";

    private final String donor;
    private final String date;        // already formatted text, or UNKNOWN
    private final String name;        // item name, e.g. Rice, Okra, Money
    private final String type;
    private final String quantity;
    private final String unit;
    private final String recordedBy;  // username of whoever recorded it
    private final String phone;       // UNKNOWN when not given
    private final String email;       // UNKNOWN when not given

    public Donation(String donor, String date, String name, String type,
                    String quantity, String unit, String recordedBy,
                    String phone, String email) {
        this.donor = donor;
        this.date = date;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.recordedBy = recordedBy;
        this.phone = blankToUnknown(phone);
        this.email = blankToUnknown(email);
    }

    // New donation: the date is stamped automatically.
    public static Donation createNew(String donor, String phone, String email,
                                     String name, String type, String quantity,
                                     String unit, String recordedBy) {
        return new Donation(donor, LocalDateTime.now().format(FMT),
                name, type, quantity, unit, recordedBy, phone, email);
    }

    private static String blankToUnknown(String s) {
        return (s == null || s.isBlank()) ? UNKNOWN : s.trim();
    }

    // The getters below are read by the History table (PropertyValueFactory).
    public String getDonor() { return donor; }
    public String getDate() { return date; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getRecordedBy() { return recordedBy; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // True for cash donations (counted separately from product stock value).
    public boolean isMonetary() {
        return "Money".equalsIgnoreCase(type);
    }

    // The quantity as a number (cash amount); 0 if it isn't a valid number.
    public double getAmount() {
        try {
            return Double.parseDouble(quantity.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isAnonymous() {
        return ANONYMOUS.equals(donor);
    }
}