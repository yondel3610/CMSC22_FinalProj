package users;

public enum Role { // fixed, from public class to public enum | use enum for constants
	MANAGER("Manager"),
	CLERK("Clerk");
	
	private final String displayName;
	Role(String displayName){
		this.displayName = displayName;
	}
	
	public String getDisplayName(){
		return displayName;
	}
}
