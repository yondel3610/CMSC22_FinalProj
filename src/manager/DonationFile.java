package manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

// Saves donations.txt (inside the data folder, next to products.txt)
// one donation per line:
// donor|dateTime|name|type|quantity|unit|recordedBy|phone|email
//
// Older lines are still readable:
//   7 fields (no phone/email)  -> phone and email show as "-"
//   4 fields (name|type|quantity|unit) -> donor, date, recordedBy, phone, email show as "-"
public class DonationFile {

    private static final File FILE = new File("data" + File.separator + "donations.txt");
    private static final File OLD_FILE = new File("donations.txt");

    public static void saveDonation(Donation d) throws IOException {
        prepareFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE, true))) {
            writer.write(d.getDonor() + "|" + d.getDate() + "|" + d.getName() + "|"
                    + d.getType() + "|" + d.getQuantity() + "|" + d.getUnit() + "|"
                    + d.getRecordedBy() + "|" + d.getPhone() + "|" + d.getEmail());
            writer.newLine();
        }
    }

    public static ArrayList<Donation> loadDonations() throws IOException {
        ArrayList<Donation> donations = new ArrayList<>();
        prepareFile();

        if (!FILE.exists()) {
            return donations;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] f = line.split("\\|", -1);

                if (f.length == 9) {
                    donations.add(new Donation(f[0], f[1], f[2], f[3], f[4], f[5],
                            f[6], f[7], f[8]));
                } else if (f.length == 7) {
                    // saved before phone/email existed
                    donations.add(new Donation(f[0], f[1], f[2], f[3], f[4], f[5],
                            f[6], Donation.UNKNOWN, Donation.UNKNOWN));
                } else if (f.length == 4) {
                    // oldest format: name|type|quantity|unit
                    donations.add(new Donation(Donation.UNKNOWN, Donation.UNKNOWN,
                            f[0], f[1], f[2], f[3], Donation.UNKNOWN,
                            Donation.UNKNOWN, Donation.UNKNOWN));
                }
                // any other line is broken, so skip it
            }
        }
        return donations;
    }

    // Total of all cash (Money) donations, in PHP.
    public static double totalMonetary() throws IOException {
        double total = 0;
        for (Donation d : loadDonations()) {
            if (d.isMonetary()) {
                total += d.getAmount();
            }
        }
        return total;
    }

    // Makes sure the data folder exists, and moves an old donations.txt
    // from the project root into it (one time only).
    private static void prepareFile() throws IOException {
        File folder = FILE.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        if (!FILE.exists() && OLD_FILE.exists()) {
            Files.move(OLD_FILE.toPath(), FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}