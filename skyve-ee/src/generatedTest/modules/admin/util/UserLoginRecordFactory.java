package modules.admin.util;

import modules.admin.domain.UserLoginRecord;
import org.skyve.CORE;
import org.skyve.metadata.customer.Customer;
import org.skyve.metadata.model.document.Document;
import org.skyve.metadata.module.Module;
import org.skyve.util.Util;
import util.AbstractDomainFactory;

public class UserLoginRecordFactory extends AbstractDomainFactory<UserLoginRecord> {

	@Override
	public UserLoginRecord getInstance() throws Exception {
		Customer customer = CORE.getUser().getCustomer();
		Module module = customer.getModule(UserLoginRecord.MODULE_NAME);
		Document document = module.getDocument(customer, UserLoginRecord.DOCUMENT_NAME);

		UserLoginRecord userLoginRecord = Util.constructRandomInstance(CORE.getPersistence().getUser(), module, document, 1);

		return userLoginRecord;
	}
}