package users;


public class Manager extends User {
	public Manager(String username, String password, String fullName) {
		super(username, password, fullName);
	}
	
	// returns their role
	@Override
	public Role getRole() {
		return Role.MANAGER;
	}
	
	// the manager can manage the products
	@Override
	public boolean canManageProducts() {
		return true;
	}
}
