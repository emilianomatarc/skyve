package org.skyve.impl.util;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.util.SerializationHelper;
import org.skyve.CORE;
import org.skyve.domain.Bean;
import org.skyve.impl.bind.BindUtil;
import org.skyve.impl.domain.AbstractPersistentBean;
import org.skyve.impl.metadata.model.document.AssociationImpl;
import org.skyve.impl.metadata.model.document.field.LengthField;
import org.skyve.impl.metadata.model.document.field.Text;
import org.skyve.impl.metadata.model.document.field.TextFormat;
import org.skyve.impl.metadata.model.document.field.validator.TextValidator.ValidatorType;
import org.skyve.impl.persistence.AbstractPersistence;
import org.skyve.metadata.customer.Customer;
import org.skyve.metadata.model.Attribute;
import org.skyve.metadata.model.Attribute.AttributeType;
import org.skyve.metadata.model.document.Association.AssociationType;
import org.skyve.metadata.model.document.Collection;
import org.skyve.metadata.model.document.Collection.CollectionType;
import org.skyve.metadata.model.document.Document;
import org.skyve.metadata.model.document.Reference;
import org.skyve.metadata.model.document.Relation;
import org.skyve.metadata.module.Module;
import org.skyve.metadata.user.User;
import org.skyve.persistence.DataStore;
import org.skyve.util.BeanVisitor;
import org.skyve.util.Binder;
import org.skyve.util.JSON;
import org.skyve.util.Util;

import com.mifmif.common.regex.Generex;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class UtilImpl {
	/**
	 * Disallow instantiation
	 */
	private UtilImpl() {
		// no-op
	}

	/**
	 * The raw configuration data from reading the JSON.
	 */
	public static Map<String, Object> CONFIGURATION = null;

	public static boolean XML_TRACE = true;
	public static boolean HTTP_TRACE = false;
	public static boolean QUERY_TRACE = false;
	public static boolean COMMAND_TRACE = false;
	public static boolean FACES_TRACE = false;
	public static boolean SQL_TRACE = false;
	public static boolean CONTENT_TRACE = false;
	public static boolean SECURITY_TRACE = false;
	public static boolean BIZLET_TRACE = false;
	public static boolean DIRTY_TRACE = false;
	public static boolean PRETTY_SQL_OUTPUT = false;
	public static final Logger LOGGER = Logger.getLogger("SKYVE");

	// This is set in the web.xml but defaults to windows
	// as a dev environment for design time and generation gear
	public static String CONTENT_DIRECTORY = "/_/Apps/content/";

	// The cron expression to use to fire off the content garbage collection
	// Defaults to run at 7 past the hour every hour.
	public static String CONTENT_GC_CRON = "0 7 0/1 1/1 * ? *";

	// Should the attachments be stored on the file system or inline.
	public static boolean CONTENT_FILE_STORAGE = true;

	// The arguments to send to the TCP server when running the content management in server mode.
	public static String CONTENT_SERVER_ARGS = null;

	// This is set in web.xml and should only be used when the APP server in use
	// doesn't allow us to get the absolute path of a resource - jboss 4.0.5.GA, WebLogic or any zipped deployment
	public static String APPS_JAR_DIRECTORY;

	public static boolean DEV_MODE = false;

	// If it is null, then the login infrastructure will prompt for the customer name.
	// If it is set, the customer will be set to that value always.
	// This property is also used for single sign on purposes.
	public static String CUSTOMER = null;

	// eg https://www.bizhub.com.au
	public static String SERVER_URL = null;
	// eg /bizhub/web
	public static String SKYVE_CONTEXT = null;
	// eg /init.biz
	public static String HOME_URI = null;

	// This is the path on the server file system of the web context root
	public static String SKYVE_CONTEXT_REAL_PATH = null;

	// Implementations of Key SKYVE classes
	public static String SKYVE_REPOSITORY_CLASS = null;
	public static String SKYVE_PERSISTENCE_CLASS = null;
	public static String SKYVE_CONTENT_MANAGER_CLASS = null;

	// The directory used for temp files for file uploads etc
	public static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

	public static boolean USING_JPA = false;

	// For conversations cache
	public static int MAX_CONVERSATIONS_IN_MEMORY = 1000;
	public static int CONVERSATION_EVICTION_TIME_MINUTES = 60;

	// For database
	public static Map<String, DataStore> DATA_STORES = new TreeMap<>();
	public static DataStore DATA_STORE = null;
	public static boolean DDL_SYNC = true;

	// For E-Mail
	public static String SMTP = null;
	public static String SMTP_PORT = null;
	public static String SMTP_UID = null;
	public static String SMTP_PWD = null;
	public static String SMTP_SENDER = null;
	// used to intercept all email and send to this test email account
	public static String SMTP_TEST_RECIPIENT = null;
	// used to switch whether to send an email or not - false to actually send the email
	public static boolean SMTP_TEST_BOGUS_SEND = false;

	// API Keys etc
	public static String GOOGLE_MAPS_V3_API_KEY = null;

	// null = prod, could be dev, test, uat or another arbitrary environment
	public static String ENVIRONMENT_IDENTIFIER = null;

	// Should scheduled jobs be manipulated by the database.
	public static boolean JOB_SCHEDULER = true;

	// Password hash algorithm
	public static String PASSWORD_HASHING_ALGORITHM = "MD5";

	// For versioning javascript/css etc for web site
	public static final String WEB_RESOURCE_FILE_VERSION = "20170628";
	public static final String SKYVE_VERSION = "20170628";
	public static final String SMART_CLIENT_DIR = "isomorphic110";

	private static String absoluteBasePath;
	private static final SecureRandom random = new SecureRandom();

	private static final String NUMBERS = "0123456789";
	private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
	private static final String ALPHA_NUMERIC = LETTERS + NUMBERS;

	public static String getAbsoluteBasePath() {
		if (absoluteBasePath == null) {
			if (APPS_JAR_DIRECTORY != null) {
				absoluteBasePath = APPS_JAR_DIRECTORY;
			} else {
				URL url = Thread.currentThread().getContextClassLoader().getResource("schemas/common.xsd");
				if (url == null) {
					UtilImpl.LOGGER.severe("Cannot determine absolute base path. Where is schemas/common.xsd?");
					ClassLoader cl = Thread.currentThread().getContextClassLoader();
					if (cl instanceof URLClassLoader) {
						UtilImpl.LOGGER.severe("The context classloader paths are:-");
						for (URL entry : ((URLClassLoader) cl).getURLs()) {
							UtilImpl.LOGGER.severe(entry.getFile());
						}
					} else {
						UtilImpl.LOGGER.severe("Cannot determine the context classloader paths...");
					}
				} else {
					absoluteBasePath = url.getPath();
					absoluteBasePath = absoluteBasePath.substring(0, absoluteBasePath.length() - 18); // remove schemas/common.xsd
					absoluteBasePath = absoluteBasePath.replace('\\', '/');
				}
			}
		}

		return absoluteBasePath;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> readJSONConfig(InputStream inputStream) throws Exception {
		String json = null;
		try (Scanner scanner = new Scanner(inputStream)) {
			json = scanner.useDelimiter("\\Z").next();
		}
		// Remove any C-style comments
		String commentsPattern = "(?s)\\/\\*(?:(\\*(?!\\/))|(?:[^\\*]))*\\*\\/|[^:]\\/\\/[^\\n\\r]*(?=[\\n\\r])";
		final Pattern pattern = Pattern.compile(commentsPattern, Pattern.MULTILINE);
		final Matcher m = pattern.matcher(json);
		json = m.replaceAll("");

		return (Map<String, Object>) JSON.unmarshall(null, json);
	}

	@SuppressWarnings("unchecked")
	public static final <T extends Serializable> T cloneBySerialization(T object) {
		return (T) SerializationHelper.clone(object);
		// try {
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// new ObjectOutputStream(baos).writeObject(object);
		// ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		// return (T) ois.readObject();
		// }
		// catch (Exception e) {
		// throw new IllegalArgumentException(e);
		// }
	}

	public static final <T extends Serializable> T cloneToTransientBySerialization(T object)
			throws Exception {
		if (object instanceof List<?>) {
			for (Object element : (List<?>) object) {
				if (element instanceof AbstractPersistentBean) {
					populateFully((AbstractPersistentBean) object);
				}
			}
		} else if (object instanceof AbstractPersistentBean) {
			populateFully((AbstractPersistentBean) object);
		}

		T result = cloneBySerialization(object);
		setTransient(result);

		return result;
	}

	/**
	 * Recurse the bean ensuring that everything is touched and loaded from the database.
	 * 
	 * @param bean The bean to load.
	 */
	public static void populateFully(final Bean bean) {
		User user = CORE.getUser();
		Customer customer = user.getCustomer();

		Module module = customer.getModule(bean.getBizModule());
		Document document = module.getDocument(customer, bean.getBizDocument());

		// Ensure that everything is loaded
		new BeanVisitor(false, true, false) {
			@Override
			protected boolean accept(String binding,
					Document documentAccepted,
					Document owningDocument,
					Relation owningRelation,
					Bean beanAccepted) {
				// do nothing - just visiting loads the instance from the database
				return true;
			}
		}.visit(document, bean, customer);
	}

	/**
	 * Recurse a bean to determine if anything has changed
	 */
	private static class ChangedBeanVisitor extends BeanVisitor {
		private boolean changed = false;

		private ChangedBeanVisitor() {
			// Don't check inverses as they aren't cascaded anyway
			super(false, false, false);
		}

		@Override
		protected boolean accept(String binding,
				Document documentAccepted,
				Document owningDocument,
				Relation owningRelation,
				Bean beanAccepted) {
			if (beanAccepted.isChanged()) {
				changed = true;
				if (UtilImpl.DIRTY_TRACE)
					UtilImpl.LOGGER.info(
							"UtilImpl.hasChanged(): Bean " + beanAccepted.toString() + " with binding " + binding + " is DIRTY");
				return false;
			}
			return true;
		}

		boolean isChanged() {
			return changed;
		}
	}

	/**
	 * Recurse the bean to determine if anything has changed.
	 * 
	 * @param bean The bean to test.
	 * @return if the bean, its collections or its aggregated beans have mutated or not
	 */
	public static boolean hasChanged(Bean bean) {
		User user = CORE.getUser();
		Customer customer = user.getCustomer();

		Module module = customer.getModule(bean.getBizModule());
		Document document = module.getDocument(customer, bean.getBizDocument());

		@SuppressWarnings("synthetic-access")
		ChangedBeanVisitor cbv = new ChangedBeanVisitor();
		cbv.visit(document, bean, customer);
		return cbv.isChanged();
	}

	/**
	 * Utility method that tries to properly initialise the persistence layer proxies used by lazy loading.
	 * 
	 * @param <T>
	 * @param possibleProxy The possible proxy
	 * @return the resolved proxy or possibleProxy
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deproxy(T possibleProxy) throws ClassCastException {
		if (possibleProxy instanceof HibernateProxy) {
			return (T) ((HibernateProxy) possibleProxy).getHibernateLazyInitializer().getImplementation();
		}

		return possibleProxy;
	}

	public static void setTransient(Object object) throws Exception {
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			for (Object element : list) {
				setTransient(element);
			}
		} else if (object instanceof AbstractPersistentBean) {
			AbstractPersistentBean bean = (AbstractPersistentBean) object;
			bean.setBizId(UUID.randomUUID().toString());
			bean.setBizLock(null);
			bean.setBizVersion(null);

			// set references transient if applicable
			Customer customer = AbstractPersistence.get().getUser().getCustomer();
			Module module = customer.getModule(bean.getBizModule());
			Document document = module.getDocument(customer, bean.getBizDocument());

			for (String referenceName : document.getReferenceNames()) {
				Reference reference = document.getReferenceByName(referenceName);
				if (reference.isPersistent()) {
					if (reference instanceof AssociationImpl) {
						AssociationImpl association = (AssociationImpl) reference;
						if (association.getType() == AssociationType.composition) {
							setTransient(BindUtil.get(bean, referenceName));
						}
					} else if (reference instanceof Collection) {
						Collection collection = (Collection) reference;
						if (collection.getType() != CollectionType.aggregation) {
							// set each element of the collection transient
							setTransient(BindUtil.get(bean, referenceName));
						}
					}
				}
			}
		}
	}

	// set the data group of a bean and all its children
	public static void setDataGroup(Object object, String bizDataGroupId) throws Exception {
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			for (Object element : list) {
				setDataGroup(element, bizDataGroupId);
			}
		} else if (object instanceof AbstractPersistentBean) {
			AbstractPersistentBean bean = (AbstractPersistentBean) object;
			bean.setBizDataGroupId(bizDataGroupId);

			// set the bizDatagroup of references if applicable
			Customer customer = AbstractPersistence.get().getUser().getCustomer();
			Module module = customer.getModule(bean.getBizModule());
			Document document = module.getDocument(customer, bean.getBizDocument());

			for (String referenceName : document.getReferenceNames()) {
				Reference reference = document.getReferenceByName(referenceName);
				if (reference.isPersistent()) {
					if (reference instanceof AssociationImpl) {
						AssociationImpl association = (AssociationImpl) reference;
						if (association.getType() == AssociationType.composition) {
							setDataGroup(BindUtil.get(bean, referenceName), bizDataGroupId);
						}
					} else if (reference instanceof Collection) {
						Collection collection = (Collection) reference;
						if (collection.getType() != CollectionType.aggregation) {
							// set each element of the collection transient
							setDataGroup(BindUtil.get(bean, referenceName), bizDataGroupId);
						}
					}
				}
			}
		}
	}

	/**
	 * Process and transform empty Strings.
	 * 
	 * @param value The String.
	 * @return null, if the trimmed value is empty, otherwise value.
	 */
	public static String processStringValue(String value) {
		String result = value;

		if (result != null) {
			result = result.trim();
			if (result.isEmpty()) {
				result = null;
			}
		}

		return result;
	}

	/**
	 * Make an instance of a document bean with random values for its properties.
	 * 
	 * @param <T> The type of Document bean to produce.
	 * @param user
	 * @param module
	 * @param document The document (corresponds to type T)
	 * @param depth How far to traverse the object graph - through associations and collections.
	 *        There are relationships that are never ending - ie Contact has Interactions which has User which has COntact
	 * @return The randomly constructed bean.
	 * @throws Exception
	 */
	public static <T extends Bean> T constructRandomInstance(User user,
			Module module,
			Document document,
			int depth)
			throws Exception {
		return UtilImpl.constructRandomInstance(user, module, document, 1, depth);
	}

	@SuppressWarnings("incomplete-switch") // content type missing from switch statement
	private static <T extends Bean> T constructRandomInstance(User user,
			Module module,
			Document document,
			int currentDepth,
			int maxDepth)
			throws Exception {
		T result = document.newInstance(user);

		for (Attribute attribute : document.getAllAttributes()) {
			String name = attribute.getName();
			AttributeType type = attribute.getAttributeType();

			switch (type) {
				case association:
					if (currentDepth < maxDepth) {
						AssociationImpl association = (AssociationImpl) attribute;
						Module associationModule = module;
						String associationModuleRef = module.getDocumentRefs().get(association.getDocumentName())
								.getReferencedModuleName();
						if (associationModuleRef != null) {
							associationModule = user.getCustomer().getModule(associationModuleRef);
						}
						Document associationDocument = associationModule.getDocument(user.getCustomer(),
								association.getDocumentName());
						BindUtil.set(result,
								name,
								UtilImpl.constructRandomInstance(user,
										associationModule,
										associationDocument,
										currentDepth + 1,
										maxDepth));
					}
					break;
				case bool:
					BindUtil.set(result, name, Boolean.FALSE);
					break;
				case collection:
					if (currentDepth < maxDepth) {
						Collection collection = (Collection) attribute;
						Module collectionModule = module;
						String collectionModuleRef = module.getDocumentRefs().get(collection.getDocumentName())
								.getReferencedModuleName();
						if (collectionModuleRef != null) {
							collectionModule = user.getCustomer().getModule(collectionModuleRef);
						}
						Document collectionDocument = collectionModule.getDocument(user.getCustomer(),
								collection.getDocumentName());
						@SuppressWarnings("unchecked")
						List<Bean> list = (List<Bean>) BindUtil.get(result, name);
						list.add(UtilImpl.constructRandomInstance(user,
								collectionModule,
								collectionDocument,
								currentDepth + 1,
								maxDepth));
						list.add(UtilImpl.constructRandomInstance(user,
								collectionModule,
								collectionDocument,
								currentDepth + 1,
								maxDepth));
					}
					break;
				case colour:
					BindUtil.set(result, name, "#FFFFFF");
					break;
				case date:
				case dateTime:
				case time:
				case timestamp:
					BindUtil.convertAndSet(result, name, new Date());
					break;
				case decimal10:
				case decimal2:
				case decimal5:
				case integer:
				case longInteger:
					BindUtil.convertAndSet(result, name, new Integer(random.nextInt(10000)));
					break;
				case enumeration:
					// pick a random value from the enum
					@SuppressWarnings("unchecked")
					Class<Enum<?>> clazz = (Class<Enum<?>>) Binder.getPropertyType(result, name);
					BindUtil.set(result, name, randomEnum(clazz, null));
					break;
				case geometry:
					BindUtil.set(result, name, new GeometryFactory().createPoint(new Coordinate(0, 0)));
					break;
				case id:
					BindUtil.set(result, name, UUID.randomUUID().toString());
					break;
				case markup:
				case memo:
					BindUtil.set(result, name, randomString(((int) (Math.random() * 255)) + 1));
					break;
				case text:
					Text text = (Text) attribute;

					// check if this string has a format mask
					if (text.getFormat() != null) {
						// check if it has a format mask and a regex, if so, prefer the regex
						if (text.getValidator() != null && text.getValidator().getRegularExpression() != null
								&& text.getValidator().getType() == null) {
							// return text matching the regex
							String xeger = randomRegex(text.getValidator().getRegularExpression());
							if (xeger != null) {
								BindUtil.set(result, name, xeger);
								continue;
							}
						}

						// return text matching the format mask
						BindUtil.set(result, name, randomFormat(text.getFormat()));
					} else if (text.getValidator() != null && text.getValidator().getRegularExpression() != null
							&& text.getValidator().getType() == null) {
						// check if this string has a regex and no validator type
						String xeger = randomRegex(text.getValidator().getRegularExpression());
						if (xeger != null) {
							BindUtil.set(result, name, xeger);
							continue;
						}
					} else {
						// check if this is an email address
						if (text.getValidator() != null && ValidatorType.email.equals(text.getValidator().getType())) {
							BindUtil.set(result, name, randomEmail(((LengthField) attribute).getLength()));
						} else if (text.getValidator() != null && text.getValidator().getRegularExpression() != null) {
							// check if this string has a regex via a validator type
							String xeger = randomRegex(text.getValidator().getRegularExpression());
							if (xeger != null) {
								BindUtil.set(result, name, xeger);
								continue;
							}
						} else {
							// return random text
							BindUtil.set(result, name, randomString(((LengthField) attribute).getLength()));
						}
					}
			}
		}
		return result;
	}

	private static String randomEmail(int length) {
		int addressLength = (int) Math.floor((length - 2) / 2);
		int domainLength = (int) Math.floor((length - 2) / 2) - 2;

		char[] address = new char[addressLength];
		for (int i = 0; i < addressLength; i++) {
			address[i] = Character.toChars(65 + (int) (Math.random() * 26))[0];
		}

		char[] domain = new char[domainLength];
		for (int i = 0; i < domainLength; i++) {
			domain[i] = Character.toChars(65 + (int) (Math.random() * 26))[0];
		}

		char[] code = new char[2];
		for (int i = 0; i < 2; i++) {
			code[i] = Character.toChars(65 + (int) (Math.random() * 26))[0];
		}

		return String.valueOf(address) + "@" + String.valueOf(domain) + "." + String.valueOf(code);
	}

	/**
	 * Returns a random value from the enum class
	 * 
	 * @param clazz The enum class
	 * @param currentValue The current int value of the enum so that it is not chosen again
	 * @return A random enum constant
	 */
	@SuppressWarnings("boxing")
	private static <T extends Enum<?>> T randomEnum(Class<T> clazz, Integer currentValue) {
		int x;
		if (currentValue != null) {
			do {
				x = random.nextInt(clazz.getEnumConstants().length);
			} while (x == currentValue);
		} else {
			x = random.nextInt(clazz.getEnumConstants().length);
		}

		return clazz.getEnumConstants()[x];
	}

	/**
	 * Returns a random string which complies to the format mask
	 * of the text attribute
	 * 
	 * @param textFormat The format to comply to
	 * @return A format compliant random string
	 */
	private static String randomFormat(TextFormat textFormat) {

		String mask = textFormat.getMask();
		String out = new String();

		for (int i = 0; i < mask.length(); i++) {
			char c = mask.charAt(i);
			switch (c) {
				case '#':
					out += NUMBERS.charAt(random.nextInt(NUMBERS.length()));
					break;
				case 'A':
					out += ALPHA_NUMERIC.charAt(random.nextInt(ALPHA_NUMERIC.length()));
					break;
				case 'L':
					out += LETTERS.charAt(random.nextInt(LETTERS.length()));
					break;
				default:
					out += c;
					break;
			}
		}

		return out;
	}

	/**
	 * Returns a random string which complies to the regular
	 * expression of the text attribute. Returns null if this
	 * cannot be achieved.
	 * 
	 * @param regularExpression The regular expression to comply to
	 * @return A regex compliant random string, or null
	 */
	private static String randomRegex(String regularExpression) {
		Generex generex = new Generex(regularExpression);
		// Generate random String matching the regex
		try {
			String result = generex.random();
			// strip boundaries
			if (result.startsWith("^") && result.endsWith("$")) {
				return StringUtils.substringBetween(result, "^", "$");
			}
			return result;
		} catch (Exception e) {
			Util.LOGGER.warning("Couldnt generate compliant string for expression " + regularExpression);
		}
		return null;
	}

	private static String randomString(int length) {
		char[] guts = new char[length];
		for (int i = 0; i < length; i++) {
			guts[i] = Character.toChars(65 + (int) (Math.random() * 26))[0];
		}

		return String.valueOf(guts);
	}
}
