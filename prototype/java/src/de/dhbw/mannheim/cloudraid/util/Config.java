package de.dhbw.mannheim.cloudraid.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Enables you to handle the program's configuration via a XML file.<br>
 * Singleton
 * 
 * @author Florian Bausch, Markus Holtermann
 */
public class Config extends HashMap<String, String> {
	/**
	 * The holder class for the singleton object.
	 * 
	 * @author Florian Bausch.
	 * 
	 */
	private static class Holder {
		public static final Config INSTANCE = new Config();
	}

	/**
	 * Maximum file size in MiB
	 */
	public static final int MAX_FILE_SIZE = 1024 * 1024 * 512;

	/**
	 * The path to the configuration file.
	 */
	private static final String CONFIG_PATH = System.getProperty("os.name")
			.contains("windows") ? System.getenv("APPDATA")
			+ "\\cloudraid\\config.xml" : System.getProperty("user.home")
			+ "/.config/cloudraid.xml";

	/**
	 * A File object of the configuration file.
	 */
	private static final File CONFIG_FILE = new File(CONFIG_PATH);

	/**
	 * A HashMap of allowed allowed ciphers. The keys of the map contain the
	 * available ciphers, while the value contains the maximum salt length.
	 */
	private static HashMap<String, Integer> allowedCiphers = new HashMap<String, Integer>();

	// TODO: remove after testing
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("You need to specify a password");
			System.exit(1);
		}
		Config c = Config.getInstance();
		c.init(args[0]);

		c.put("boolean.1", true, true);
		c.put("boolean.2", true);
		c.put("double.1", 1.0d / 7.0d, true);
		c.put("double.2", 1.0d / 7.0d);
		c.put("float.1", 1.0f / 13.0f, true);
		c.put("float.2", 1.0f / 13.0f);
		c.put("int.1", 42, true);
		c.put("int.2", 42);
		c.put("long.1", 9876543210l, true);
		c.put("long.2", 9876543210l);
		c.put("string.1", "Test string", true);
		c.put("string.2", "Test string");
		c.save();
		System.out.println(c.getBoolean("boolean.1", false));
		System.out.println(c.getBoolean("boolean.2", false));
		System.out.println(c.getDouble("double.1", 1.0d));
		System.out.println(c.getDouble("double.2", 1.0d));
		System.out.println(c.getFloat("float.1", 1.0f));
		System.out.println(c.getFloat("float.2", 1.0f));
		System.out.println(c.getInt("int.1", 1));
		System.out.println(c.getInt("int.2", 1));
		System.out.println(c.getLong("long.1", 1l));
		System.out.println(c.getLong("long.2", 1l));
		System.out.println(c.getString("string.1", "String"));
		System.out.println(c.getString("string.2", "String"));
	}

	private String encryption = "AES";
	private byte[] password;
	private Cipher cipher;
	private int keyLength;

	/**
	 * Contains the salts for configuration values that are stored encrypted
	 */
	private HashMap<String, String> salts = new HashMap<String, String>();

	/**
	 * Auto generated serialVersionUID
	 */
	private static final long serialVersionUID = -3740632761998756639L;

	/**
	 * Get the Singleton instance of Config.
	 * 
	 * @return The instance of Config
	 */
	public static synchronized Config getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * This function generates a salt that does not contain any characters that
	 * might break the XML context
	 * 
	 * @param length
	 *            The length that the salt should have
	 * @return Returns a salt as byte array of length <code>length</code>
	 */
	private static byte[] getXMLSaveSalt(int length) {
		byte[] b = new byte[length];
		Random r = new Random(System.currentTimeMillis());
		byte[] allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~!@#$%^*()-_+[]{};:,./?"
				.getBytes();
		int l = allowedChars.length;
		int i;
		for (i = 0; i < length; i++) {
			b[i] = allowedChars[r.nextInt(l)];
		}
		return b;
	}

	/**
	 * Creates a Config object that stores the configuration that is stored in
	 * the config file.
	 */
	private Config() {

		Config.allowedCiphers.put("AES", 256);

		if (!CONFIG_FILE.exists())
			return;

		// Create DocumentBuilder for XML files.
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		docBuilder.setErrorHandler(null);

		// Build the XML tree.
		Document doc;
		try {
			System.out.println("Path to config file: " + CONFIG_PATH);
			doc = docBuilder.parse(CONFIG_FILE);
		} catch (SAXException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// we read the encryption algorithm from the config file. If and only if
		// it is given and it is an allowed cipher, we use it. Otherwize we set
		// it to a default value <i>AES</i>!
		this.encryption = doc.getDocumentElement().getAttribute("encryption");
		if (this.encryption.isEmpty()
				|| !isAlgorithmsAvailable(this.encryption)) {
			this.encryption = "AES";
		}

		// Iterate over all "entry" nodes and collect the information.
		NodeList list = doc.getDocumentElement().getElementsByTagName("entry");
		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);
			this.put(node.getAttribute("name").trim(), node.getTextContent()
					.trim());
			// If there is a salt stored, store it in an extra HashMap.
			if (node.hasAttribute("salt"))
				this.salts.put(node.getAttribute("name").trim(), node
						.getAttribute("salt").trim());
		}
	}

	/**
	 * Reads a setting from the settings list. If there is a salt available for
	 * the certain key, we use it to decrypt the value.
	 * 
	 * @param key
	 *            The specific key of the configuration value
	 * @return A string representation of the setting. If any exception occurs
	 *         <code>null</code> is returned
	 */
	private synchronized String get(String key) {
		try {
			if (this.salts.containsKey(key)) {
				// We have to decrypt the value with the salt and the password
				byte[] salt = this.salts.get(key).getBytes();
				if (salt.length != this.keyLength) {
					// the salt does not fulfill the requirements for this
					// crypto algorithm
					return null;
				}
				// we replace the first part of the salt by the user password
				System.arraycopy(this.password, 0, salt, 0,
						Math.min(this.password.length, salt.length));
				SecretKeySpec skeySpec = new SecretKeySpec(salt, "AES");
				this.cipher.init(Cipher.DECRYPT_MODE, skeySpec);
				byte[] value = new sun.misc.BASE64Decoder().decodeBuffer(super
						.get(key));
				String r = new String(this.cipher.doFinal(value));
				return r;
			} else {
				// The content is available in plain text
				String value = super.get(key).replace("&quot", "\"")
						.replace("&lt;", "<").replace("&gt;", ">")
						.replace("&amp;", "&");
				return value;
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the boolean representation of the value of a stored key, if it is
	 * possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The boolean representation of the value or
	 *         <code>defaultVal</code>
	 */
	public synchronized boolean getBoolean(String key, boolean defaultVal) {
		String str = this.get(key);
		if (str == null) {
			return defaultVal;
		}
		try {
			return Boolean.parseBoolean(str);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	/**
	 * Returns the double representation of the value of a stored key, if it is
	 * possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The float representation of the value or <code>defaultVal</code>
	 */
	public synchronized double getDouble(String key, double defaultVal) {
		String str = this.get(key);
		if (str == null) {
			return defaultVal;
		}
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	/**
	 * Returns the float representation of the value of a stored key, if it is
	 * possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The float representation of the value or <code>defaultVal</code>
	 */
	public synchronized float getFloat(String key, float defaultVal) {
		String str = this.get(key);
		if (str == null) {
			return defaultVal;
		}
		try {
			return Float.parseFloat(str);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	/**
	 * Returns the int representation of the value of a stored key, if it is
	 * possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The int representation of the value or <code>defaultVal</code>
	 */
	public synchronized int getInt(String key, int defaultVal) {
		String str = this.get(key);
		if (str == null) {
			return defaultVal;
		}
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	/**
	 * Returns the int representation of the value of a stored key, if it is
	 * possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The int representation of the value or <code>defaultVal</code>
	 */
	public synchronized long getLong(String key, long defaultVal) {
		String str = this.get(key);
		if (str == null) {
			return defaultVal;
		}
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	/**
	 * Returns the String representation of the value of a stored key.<br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The String representation of the value or <code>defaultVal</code>
	 */
	public synchronized String getString(String key, String defaultVal) {
		String str = this.get(key);
		if (str == null) {
			return defaultVal;
		} else
			return str;
	}

	public void init(String password) {
		this.password = password.getBytes();

		try {
			this.cipher = Cipher.getInstance(this.encryption);
			// TODO this might return Integer.MAX_VALUE. See
			// http://docs.oracle.com/javase/6/docs/api/javax/crypto/Cipher.html#getMaxAllowedKeyLength(java.lang.String)
			// this.keyLength = Cipher.getMaxAllowedKeyLength(this.encryption);
			this.keyLength = Config.allowedCiphers.get(this.encryption) / 8;
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			e1.printStackTrace();
		}

	}

	private boolean isAlgorithmsAvailable(String algorithm) {
		// Provider[] providers = Security.getProviders();
		// ArrayList<String> ciphers = new ArrayList<String>();
		// String algo;
		// for (int i = 0; i != providers.length; i++) {
		// Iterator<Object> it = providers[i].keySet().iterator();
		// while (it.hasNext()) {
		// String entry = (String) it.next();
		// if (entry.startsWith("Alg.Alias.Cipher.")) {
		// algo = entry.substring("Alg.Alias.Cipher.".length());
		// if (algo.indexOf(" ") == -1) {
		// // The algorithm does not contain a whitespace
		// ciphers.add(algo);
		// }
		// }
		// }
		// }
		// return ciphers.contains(algorithm);

		return Config.allowedCiphers.keySet().contains(algorithm);
	}

	/**
	 * Checks if there <code>key</code> is set.
	 * 
	 * @param key
	 *            The key to check.
	 * @return <code>true</code> if it exists.
	 */
	public synchronized boolean keyExists(String key) {
		return super.keySet().contains(key);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String)
	 */
	public synchronized void put(String key, boolean value) {
		this.put(key, String.valueOf(value), false);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String, boolean)
	 */
	public synchronized void put(String key, boolean value, boolean encrypted) {
		this.put(key, String.valueOf(value), encrypted);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String)
	 */
	public synchronized void put(String key, double value) {
		this.put(key, String.valueOf(value), false);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String, boolean)
	 */
	public synchronized void put(String key, double value, boolean encrypted) {
		this.put(key, String.valueOf(value), encrypted);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String)
	 */
	public synchronized void put(String key, float value) {
		this.put(key, String.valueOf(value), false);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String, boolean)
	 */
	public synchronized void put(String key, float value, boolean encrypted) {
		this.put(key, String.valueOf(value), encrypted);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String)
	 */
	public synchronized void put(String key, int value) {
		this.put(key, String.valueOf(value), false);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String, boolean)
	 */
	public synchronized void put(String key, int value, boolean encrypted) {
		this.put(key, String.valueOf(value), encrypted);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String)
	 */
	public synchronized void put(String key, long value) {
		this.put(key, String.valueOf(value), false);
	}

	/**
	 * @see de.dhbw.mannheim.cloudraid.util.Config#put(String, String, boolean)
	 */
	public synchronized void put(String key, long value, boolean encrypted) {
		this.put(key, String.valueOf(value), encrypted);
	}

	/**
	 * Sets the value of a certain parameter in the config. When the config is
	 * written to a file the last value that was set for the <code>key</code> is
	 * stored.
	 * 
	 * @param key
	 *            A unique identification key for this setting.
	 * @param value
	 *            The value that shall be stored.
	 */
	@Override
	public synchronized String put(String key, String value) {
		return this.put(key, value, false);
	}

	/**
	 * Sets the value of a certain parameter in the config. When the config is
	 * written to a file the last value that was set for the <code>key</code> is
	 * stored.
	 * 
	 * If anything during encryption fails, we store the configuration value
	 * unencrypted. This is a lack of security but inevitable!
	 * 
	 * @param key
	 *            A unique identification key for this setting.
	 * @param value
	 *            The value that shall be stored.
	 * @param encrypted
	 *            If set to true, the value will be encrypted with a the salted
	 *            user password.
	 * @return Returns the value that was stored at the key before overriding.
	 */
	public synchronized String put(String key, String value, boolean encrypted) {

		if (encrypted) {
			// We have to decrypt the value with the salt and the password
			byte[] salt = Config.getXMLSaveSalt(this.keyLength);
			byte[] salt2 = new byte[salt.length];
			System.arraycopy(salt, 0, salt2, 0, salt.length);

			// we replace the first part of the salt by the user password
			System.arraycopy(this.password, 0, salt2, 0,
					Math.min(this.password.length, salt2.length));
			SecretKeySpec skeySpec = new SecretKeySpec(salt2, "AES");

			// If we run into a encryption error, we will store the value in
			// plain text!
			String v = new sun.misc.BASE64Encoder().encode(value.getBytes());
			try {
				// All actions after encoding the encrypted byte array are
				// expected to work. But if something fails after adding the
				// salt to the global salt list, we will remove that later.
				this.cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
				v = new sun.misc.BASE64Encoder().encode(cipher.doFinal(value
						.getBytes()));
				this.salts.put(key, new String(salt));
				return super.put(key, v);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}

		}
		if (this.salts.containsKey(key)) {
			this.salts.remove(key);
		}
		return super.put(key, value.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;"));
	}

	/**
	 * Writes the config to a file.
	 */
	public void save() {
		try {
			if (!CONFIG_FILE.getParentFile().exists())
				CONFIG_FILE.getParentFile().mkdirs();

			// Create the output writer.
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					CONFIG_PATH));

			// Write standard conform XML code.
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.newLine();
			writer.write("<root encryption=\"" + this.encryption + "\">");
			Set<String> keys = this.keySet();
			for (String k : keys) {
				writer.newLine();
				String saltString = this.salts.get(k);
				String salt = saltString == null || saltString.equals("") ? ""
						: " salt=\"" + saltString + "\"";
				writer.write("\t<entry name=\"" + k + "\"" + salt + ">"
						+ super.get(k) + "</entry>");
			}
			writer.newLine();
			writer.write("</root>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}