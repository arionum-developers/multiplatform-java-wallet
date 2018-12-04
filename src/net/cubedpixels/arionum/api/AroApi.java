package net.cubedpixels.arionum.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.cubedpixels.arionum.ArionumMain;
import net.cubedpixels.arionum.api.ApiRequest.RequestFeedback;
import net.cubedpixels.arionum.api.Base58.CallBackSigner;
import net.cubedpixels.arionum.ui.Modal;

public class AroApi {

	// TODO -> TRANSACTION RELATED VARS
	private static String message = "";
	private static String signature = "";
	private static String unixTime = "";
	private static String val = "";

	public static String decrypt(byte[] encryptedIvTextBytes, String key) throws Exception {
		String decrypted = "";
		//////////////////////////////////////////////////////////
		byte[] iv = Arrays.copyOfRange(encryptedIvTextBytes, 0, 16);
		byte[] base64encoded = Arrays.copyOfRange(encryptedIvTextBytes, 16, encryptedIvTextBytes.length);
		byte[] decoded = Base64.getDecoder().decode(base64encoded);
		//////////////////////////////////////////////////////////
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, makeKey(key), makeIv(iv));
		decrypted = new String(cipher.doFinal(decoded));

		return new String(decrypted);
	}

	public static String getMD5(File file) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			FileInputStream s = new FileInputStream(file);
			String digest = getDigest(s, md, 2048);
			s.close();
			return digest;
		} catch (Exception e) {
			return "";
		}
	}

	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	public static String asHex(byte[] buf) {
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	private static String getDigest(InputStream is, MessageDigest md, int byteArraySize)
			throws NoSuchAlgorithmException, IOException {

		md.reset();
		byte[] bytes = new byte[byteArraySize];
		int numBytes;
		while ((numBytes = is.read(bytes)) != -1) {
			md.update(bytes, 0, numBytes);
		}
		byte[] digest = md.digest();
		String result = new String(asHex(digest));
		return result;
	}

	public static ArionumKeyPair ecKeyPairGenerator() {
		Security.addProvider(new BouncyCastleProvider());
		try {
			KeyPair keyPair;
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
			ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
			keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
			keyPair = keyPairGenerator.generateKeyPair();
			ArionumKeyPair akp = new ArionumKeyPair(keyPair);
			if (akp.getPublicKey() == null)
				return null;
			return akp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String encrypt(String plainText, String key) throws Exception {
		byte[] clean = plainText.getBytes();
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// Generating IV.
		int ivSize = 16;
		byte[] iv = new byte[ivSize];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		cipher.init(Cipher.ENCRYPT_MODE, makeKey(key), makeIv(iv));
		byte[] encrypted = cipher.doFinal(clean);
		//////////////////////////////////////////////////////
		byte[] base64 = Base64.getEncoder().encode(encrypted);
		byte[] combined = new byte[iv.length + base64.length];

		System.arraycopy(iv, 0, combined, 0, iv.length);
		System.arraycopy(base64, 0, combined, iv.length, base64.length);

		String s = Base64.getEncoder().encodeToString(combined);
		return s;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void fixKeyLength() {
		String errorString = "Failed manually overriding key-length permissions.";
		int newMaxKeyLength;
		try {
			if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
				Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
				Constructor con = c.getDeclaredConstructor();
				con.setAccessible(true);
				Object allPermissionCollection = con.newInstance();
				Field f = c.getDeclaredField("all_allowed");
				f.setAccessible(true);
				f.setBoolean(allPermissionCollection, true);

				c = Class.forName("javax.crypto.CryptoPermissions");
				con = c.getDeclaredConstructor();
				con.setAccessible(true);
				Object allPermissions = con.newInstance();
				f = c.getDeclaredField("perms");
				f.setAccessible(true);
				((Map) f.get(allPermissions)).put("*", allPermissionCollection);

				c = Class.forName("javax.crypto.JceSecurityManager");
				f = c.getDeclaredField("defaultPolicy");
				f.setAccessible(true);
				Field mf = Field.class.getDeclaredField("modifiers");
				mf.setAccessible(true);
				mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				f.set(null, allPermissions);

				newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
			}
		} catch (Exception e) {
			throw new RuntimeException(errorString, e);
		}
		if (newMaxKeyLength < 256)
			throw new RuntimeException(errorString); // hack failed
	}

	public static String getAddress(String publicKey) {
		byte[] work = publicKey.getBytes();
		for (int i = 0; i < 9; i++) {
			work = Base58.getSHA512RAW(work);
		}
		return Base58.encode(work);
	}

	public static void getAlias(Runnable run) {
		ApiRequest.requestFeedback(new RequestFeedback() {

			@Override
			public void onFeedback(JSONObject object) throws JSONException {
				String alias = "none";
				if (object.has("data")) {
					String o = object.get("data").toString();
					if (o == "null" || o == "false")
						alias = "none";
					else
						alias = o;
				}
				ArionumMain.setAlias(alias);
				run.run();
			}
		}, "getAlias", new ApiRequest.Argument("account", ArionumMain.getAddress()));
	}

	public static void getSignature(final String address, final String msgs, final double val, final long unix,
			final String version, final CallBackSigner callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				double fee = 0;
				if (fee == 0)
					fee = val * 0.0025;
				if (fee > 10)
					fee = 10;
				if (fee < 0.00000001)
					fee = 0.00000001;

				if (val == 0.00000001 && ArionumMain.getAddress() == address)
					fee = 10;
				DecimalFormat format = new DecimalFormat("0.########");
				String vals = format.format(val);
				if (!vals.contains(",") && !vals.contains("."))
					vals += ",0";
				while (vals.split(",")[1].length() < 8)
					vals += "0";
				vals = vals.replace(",", ".");
				String fees = format.format(fee);
				if (!fees.contains(",") && !fees.contains("."))
					fees += ",0";
				fees = fees.replace(".", ",");
				while (fees.split(",")[1].length() < 8)
					fees += "0";
				fees = fees.replace(",", ".");
				String date = unix + "";

				String done = vals + "-" + fees + "-" + address + "-" + msgs + "-" + version + "-"
						+ ArionumMain.getPublicKey() + "-" + date + "";

				System.out.println("UNSIGNED: " + done);
				try {
					byte[] signed = Base58.signMessageWithKey(done.getBytes());
					String signed_encoded = Base58.encode(signed);
					System.out.println("SIGN: " + signed_encoded);
					callback.onDone(signed_encoded, unix + "", vals, fees, msgs);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static AlgorithmParameterSpec makeIv(byte[] iv) {
		return new IvParameterSpec(iv);
	}

	private static Key makeKey(String key1) {
		fixKeyLength();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] key = md.digest(key1.getBytes("UTF-8"));
			return new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void makeTransaction(final String addressTO, double value, String message2, final String version,
			final Runnable run) {
		long UNIX = System.currentTimeMillis() / 1000;
		getSignature(addressTO, message2, value, UNIX, version, new Base58.CallBackSigner() {
			@Override
			public void onDone(String signed1, String unix1, String val1, String fee1, String message1) {
				signature = signed1;
				val = val1;
				unixTime = unix1;
				message = message1;

				ApiRequest.requestFeedback(new ApiRequest.RequestFeedback() {
					@Override
					public void onFeedback(final JSONObject object) throws JSONException {
						run.run();
						final Object data = object.get("data");
						// TODO -> HANDLE ERROR CHECK OF TRANSACTION
						if (object == null || object.toString().contains("error")) {

							// TODO -> DISPLAY DIALOG WITH ERROR AND TRY TO
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									new Modal("Error while sending Transaction", "Message: " + data.toString())
											.show(new Stage());
								}
							});

							// TODO -> HANDLE SUCCESS OF TRANSACTION
						} else {

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									new Modal("Transaction sent!", "Your transaction ID:").withTextField()
											.withDisabledTextField().withTextFieldContent(data.toString())
											.show(new Stage());
								}
							});

						}

					}
				}, "send", new ApiRequest.Argument("val", val), new ApiRequest.Argument("dst", addressTO),
						new ApiRequest.Argument("public_key", ArionumMain.getPublicKey()),
						new ApiRequest.Argument("signature", signature), new ApiRequest.Argument("date", unixTime),
						new ApiRequest.Argument("message", message), new ApiRequest.Argument("version", version));

			}
		});
	}

}
