package users;

public class Clerk extends User {
	public Clerk(String username, String password, String fullName) {
		super(username, password, fullName);
	}
	
	// returns their role
	@Override
	public Role getRole() {
		return Role.CLERK;
	}
	
	// the clerk can't manage the products
	@Override
	public boolean canManageProducts() {
		return false;
	}
}
