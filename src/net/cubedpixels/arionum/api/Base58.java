package net.cubedpixels.arionum.api;

import java.io.StringReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import javafx.scene.image.Image;
import net.cubedpixels.arionum.ArionumMain;

public class Base58 {

	// Base58 String encryption and decryption
	// ->STRING TO BYTE -> BYTE TO STRING

	public abstract static class CallBackSigner {
		public abstract void onDone(String signed, String unix, String val, String fee, String msg);
	}

	public static abstract class QRCallback {
		public abstract void onQRgenerate(Image i);
	}

	private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

	private static final int BASE_256 = 256;
	private static final int BASE_58 = ALPHABET.length;
	private static String diggest = "6C2W1aZ5vrtgMFdokBMgvczQjf1eeRn4xpbmKNeHYqGADRVbgjkKtfh";

	private static final char[] hexArray = "0123456789abcdef".toCharArray();

	private static final int[] INDEXES = new int[128];

	static {
		for (int i = 0; i < INDEXES.length; i++) {
			INDEXES[i] = -1;
		}
		for (int i = 0; i < ALPHABET.length; i++) {
			INDEXES[ALPHABET[i]] = i;
		}
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return String.valueOf(hexChars);
	}

	private static String coin2pem() {
		byte[] data = Base58.decode(ArionumMain.getPrivateKey());
		data = Base64.getEncoder().encode(data);
		String dast = new String(data);
		String dat = "";
		int temp = 64;
		for (int i = 0; i < dast.length(); i++) {
			dat += dast.charAt(i);
			if (temp - i == 0) {
				temp += 64;
				dat += "\n";
			}
		}
		return "-----BEGIN EC PRIVATE KEY-----\n" + dat + "\n-----END EC PRIVATE KEY-----\n";
	}

	
	public static String pem2coin(String data)
	{
	    data=data.replace("-----BEGIN PUBLIC KEY-----", "");
	    data=data.replace("-----END PUBLIC KEY-----", "");
	    data=data.replace("-----BEGIN EC PRIVATE KEY-----", "");
	    data=data.replace("-----END EC PRIVATE KEY-----", "");
	    data=data.replace("\n", "");
	    byte[] datas= DatatypeConverter.parseBase64Binary(data);
	    return Base58.encode(datas);
	}

	private static byte[] copyOfRange(byte[] source, int from, int to) {
		byte[] range = new byte[to - from];
		System.arraycopy(source, from, range, 0, range.length);

		return range;
	}

	public static byte[] decode(String input) {
		if (input.length() == 0) {
			return new byte[0];
		}

		byte[] input58 = new byte[input.length()];
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);

			int digit58 = -1;
			if (c >= 0 && c < 128) {
				digit58 = INDEXES[c];
			}
			if (digit58 < 0) {
				throw new RuntimeException("Not a Base58 input: " + input);
			}

			input58[i] = (byte) digit58;
		}

		int zeroCount = 0;
		while (zeroCount < input58.length && input58[zeroCount] == 0) {
			++zeroCount;
		}

		byte[] temp = new byte[input.length()];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input58.length) {
			byte mod = divmod256(input58, startAt);
			if (input58[startAt] == 0) {
				++startAt;
			}

			temp[--j] = mod;
		}

		while (j < temp.length && temp[j] == 0) {
			++j;
		}
		byte[] done = copyOfRange(temp, j - zeroCount, temp.length);
		return done;
	}

	private static byte divmod256(byte[] number58, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number58.length; i++) {
			int digit58 = (int) number58[i] & 0xFF;
			int temp = remainder * BASE_58 + digit58;

			number58[i] = (byte) (temp / BASE_256);

			remainder = temp % BASE_256;
		}

		return (byte) remainder;
	}

	private static byte divmod58(byte[] number, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number.length; i++) {
			int digit256 = (int) number[i] & 0xFF;
			int temp = remainder * BASE_256 + digit256;

			number[i] = (byte) (temp / BASE_58);

			remainder = temp % BASE_58;
		}

		return (byte) remainder;
	}

	public static String encode(byte[] input) {
		if (input.length == 0) {
			return "";
		}

		input = copyOfRange(input, 0, input.length);

		int zeroCount = 0;
		while (zeroCount < input.length && input[zeroCount] == 0) {
			++zeroCount;
		}

		byte[] temp = new byte[input.length * 2];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input.length) {
			byte mod = divmod58(input, startAt);
			if (input[startAt] == 0) {
				++startAt;
			}

			temp[--j] = (byte) ALPHABET[mod];
		}

		while (j < temp.length && temp[j] == ALPHABET[0]) {
			++j;
		}

		while (--zeroCount >= 0) {
			temp[--j] = (byte) ALPHABET[0];
		}

		byte[] output = copyOfRange(temp, j, temp.length);
		return new String(output);
	}

	public static void generateQR(QRCallback callback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					URL u = new URL(new String(Base64.getDecoder()
							.decode("aHR0cDovL2N1YmVkcGl4ZWxzLm5ldC9xci9nZW5lcmF0b3IucGhw".getBytes())) + "?Address="
							+ ArionumMain.getAddress() + "&PrivateKey=" + ArionumMain.getPrivateKey() + "&PublicKey="
							+ ArionumMain.getPublicKey());
					callback.onQRgenerate(new Image(u.toString()));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static String getDiggest() {
		return diggest;
	}

	public static PrivateKey getPrivateKeyFromECBigIntAndCurve(BigInteger s, String curveName) {
		X9ECParameters ecCurve = ECNamedCurveTable.getByName(curveName);
		ECParameterSpec ecParameterSpec = new ECNamedCurveSpec(curveName, ecCurve.getCurve(), ecCurve.getG(),
				ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
		ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(s, ecParameterSpec);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("EC");
			return keyFactory.generatePrivate(privateKeySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getSHA256(String data) {
		StringBuilder sb = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(data.getBytes());
			byte[] byteData = md.digest();
			sb.append(bytesToHex(byteData));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static byte[] getSHA512RAW(byte[] data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(data);
		return md.digest();
	}

	public static byte[] signMessageWithKey(byte[] unsigned) {
		Security.addProvider(new BouncyCastleProvider());

		System.out.println("====================================");
		System.out.println(coin2pem());
		System.out.println("====================================");

		try {
			StringReader s = new StringReader(coin2pem());
			PEMParser parser = new PEMParser(s);
			PEMKeyPair kp = (PEMKeyPair) parser.readObject();
			parser.close();
			AsymmetricKeyParameter privKey = PrivateKeyFactory.createKey(kp.getPrivateKeyInfo());
			AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(null, privKey);

			ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
			signature.initSign(getPrivateKeyFromECBigIntAndCurve(privateKey.getD(), "secp256k1"));
			signature.update(unsigned);

			byte[] signatureBytes = signature.sign();
			return signatureBytes;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}