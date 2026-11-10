
package manager;

import java.io.*;
import java.util.ArrayList;

public class DonationFile {

    private static final String FILE_NAME = "donations.txt";

    public static void saveDonation(Donation donation)
            throws IOException {

        BufferedWriter writer = new BufferedWriter(
            new FileWriter(FILE_NAME, true)
        );

        writer.write(
            donation.getName() + "|" +
            donation.getType() + "|" +
            donation.getQuantity() + "|" +
            donation.getUnit()
        );

        writer.newLine();
        writer.close();
    }

    public static ArrayList<Donation> loadDonations()
            throws IOException {

        ArrayList<Donation> donations = new ArrayList<>();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return donations;
        }

        BufferedReader reader = new BufferedReader(
            new FileReader(FILE_NAME)
        );

        String line;

        while ((line = reader.readLine()) != null) {

            String[] data = line.split("\\|", -1);

            if (data.length == 4) {

                Donation donation = new Donation(
                    data[0],
                    data[1],
                    data[2],
                    data[3]
                );

                donations.add(donation);
            }
        }

        reader.close();

        return donations;
    }
}
