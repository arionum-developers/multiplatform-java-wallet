package net.cubedpixels.arionum.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;

@SuppressWarnings("deprecation")
public class ArionumKeyPair {

	private String publicKey;
	private String privateKey;
	private KeyPair keyPair;

	public ArionumKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
		try {
			String der = privateKeyToDER(keyPair.getPrivate());
			System.out.println(der + "CHECK: " + check(der));

			String ders = publicKeyToDER(keyPair.getPublic());

			this.privateKey = Base58.pem2coin(der);
			this.publicKey = Base58.pem2coin(ders);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean check(String der) {
		try {
			PEMParser parser = new PEMParser(new StringReader(der));
			PEMKeyPair kp = (PEMKeyPair) parser.readObject();
			parser.close();
			AsymmetricKeyParameter privKey = PrivateKeyFactory.createKey(kp.getPrivateKeyInfo());
			AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(null, privKey);

			ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
			signature.initSign(Base58.getPrivateKeyFromECBigIntAndCurve(privateKey.getD(), "secp256k1"));
			signature.update("test".getBytes());
			signature.sign();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String privateKeyToDER(PrivateKey key) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PEMWriter pemWriter = new PEMWriter(new OutputStreamWriter(bos));
		pemWriter.writeObject(key);
		pemWriter.close();

		return new String(bos.toByteArray());
	}

	public static String publicKeyToDER(PublicKey key) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PEMWriter pemWriter = new PEMWriter(new OutputStreamWriter(bos));
		pemWriter.writeObject(key);
		pemWriter.close();

		return new String(bos.toByteArray());
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

}