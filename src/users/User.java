package users;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

// this is the base class for everyone who 
// can log in to the system
public abstract class User {
	private final String username;
    private final String fullName;
    private final String passwordHash;
    
    // only for the users package
    protected User(String username, String password, String fullName) {
    	
    	// null or empty input 
    	if (username == null || username.isBlank() ||
    		fullName == null || fullName.isBlank() ||
    		password == null || password.isEmpty()) {
    		// throw an exception message
    		throw new IllegalArgumentException("Missing fields are required"); // edit niyo error message if needed
    	}
    	
    	this.username = username.trim().toLowerCase();
    	this.fullName = fullName.trim();
    	// store password in hash, not plain text
    	this.passwordHash = hash(password); // TODO: fix error if meron pa rin
    }
    
    // getters
    public String getUsername() {
    	return username;
    }
    
    public String getFullName() {
    	return fullName;
    }
    
    // return the password if it's not null AND it's equal to 
    // the hashed password saved for that account
    public boolean checkPassword(String password) {
    	return password != null && passwordHash.equals(hash(password)); 
    }
    
    public abstract Role getRole();	// each subclass reports to its role
    public abstract boolean canManageProducts(); // permission check for the users and determines which screens they can access

    // hash function (for passwords
    public static String hash(String password) {
    	try {
    		MessageDigest digest = MessageDigest.getInstance("SHA-256"); // sha 256 encryption
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
    	} catch (NoSuchAlgorithmException e) {
    		throw new IllegalStateException("Lorem ipsum");
    	}
    }
    
    @Override
    public String toString() {
    	return fullName + " (" + username + ")";
    }
    // TODO: add more documentation on the users package
}





























