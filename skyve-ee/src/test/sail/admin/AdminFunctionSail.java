package sail.admin;

import org.junit.Test;

public class AdminFunctionSail extends TestAdminMenuExternalTabletAsAdmininstrator {

	@Test
	public void test() throws Exception {
		
		String clearTextPassword = "Password01";
		
		login("demo", "admin", clearTextPassword);
		
//		testMenuPassword(clearTextPassword);
//		testMenuUserDashboard();
		testMenuSecurityAdminGroups();
		
//		testMenuContacts();

		logout();
	}
}
