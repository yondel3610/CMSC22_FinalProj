package system;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import inventory.Inventory;
import users.Clerk;
import users.Manager;
import users.User;

public class DonationSystem {
    private static final DonationSystem INSTANCE = new DonationSystem();

    // The one shared product catalog + stock ledger. Created once when the
    // system starts
    // every screen (products, inventory, donations) reads and
    // writes this same object so the data stays consistent everywhere
    private final Inventory inventory = new Inventory();

    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    private DonationSystem() {
        seedTestAccounts();
    }

    public static DonationSystem getInstance() {
        return INSTANCE;
    }

    // The shared catalog + stock. Used by the dashboards, the product and
    // inventory screens, and (later) donation recording.
    public Inventory getInventory() {
        return inventory;
    }

    // TODO: temp accounts for testing, replace later or comment out when not in use
    private void seedTestAccounts() {
        addUser(new Manager("admin", "admin123", "Administrator"));
        addUser(new Clerk("clerk", "clerk123", "Test Clerk"));
    }

    // TODO: addUser, return false if username is taken
    public boolean addUser(User user) {
        return users.putIfAbsent(user.getUsername(), user) == null;
    }

    // TODO: credentials check; if success/match user becomes curr_user
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        User user = users.get(username.trim().toLowerCase());
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // TODO: getters(?)
    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<Clerk> getClerks() {
        return users.values().stream()
                .filter(u -> u instanceof Clerk)
                .map(u -> (Clerk) u)
                .toList();
    }
}