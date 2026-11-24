package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic pipe-delimited text file store.
 *
 * Every module (products, movements, donors, transactions...) saves its
 * records as one line of text, using "|" as the field separator:
 *
 *     id|name|description|...
 *
 * Subclasses only define how an object becomes a line (toLine) and
 * how a line becomes an object (fromLine).
 * Everything else (reading, writing, creating the data folder) is handled here.
 */
public abstract class FileStore<T> {

    private final File file;

    protected FileStore(String filename) {
        this.file = new File("data" + File.separator + filename);
        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
    }

    // Turn one object into one line for the text file. 
    protected abstract String toLine(T item);

    // Turn one line back into an object. Return null to skip a broken line.
    protected abstract T fromLine(String[] fields);

    // Read every record from the file. Missing or empty file = empty list.
    public List<T> loadAll() {
        List<T> items = new ArrayList<>();
        if (!file.exists()) {
            return items;
        }
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] fields = line.split("\\|", -1);
                T item = fromLine(fields);
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + file.getName() + ": " + e.getMessage());
        }
        return items;
    }

    // Overwrite the file with the current list of records.
    public void saveAll(List<T> items) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            for (T item : items) {
                out.println(toLine(item));
            }
        } catch (IOException e) {
            System.out.println("Could not save " + file.getName() + ": " + e.getMessage());
        }
    }
}