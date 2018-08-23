package net.cubedpixels.arionum.miner;

import java.security.SecureRandom;
import java.util.Base64;

public class Nonce {

	private String nonce;
	private String nonceRaw;
	private byte[] nonceBYTE = new byte[16];

	public Nonce(String publicKey, String data, String difficultyString, int length) {

		SecureRandom random = new SecureRandom();
		String encNonce = null;
		StringBuilder hashBase;
		random.nextBytes(nonceBYTE);
		encNonce = new String(Base64.getEncoder().encode(nonceBYTE));
		char[] nonceChar = encNonce.toCharArray();
		StringBuilder nonceSb = new StringBuilder(encNonce.length());

		for (char ar : nonceChar) {
			if (ar >= '0' && ar <= '9' || ar >= 'a' && ar <= 'z' || ar >= 'A' && ar <= 'Z') {
				nonceSb.append(ar);
			}
		}

		hashBase = new StringBuilder(length);
		hashBase.append(publicKey).append("-");
		hashBase.append(nonceSb).append("-");
		hashBase.append(data).append("-");
		hashBase.append(difficultyString);

		nonce = hashBase.toString();
		nonceRaw = nonceSb.toString();
	}

	public String getNonce() {
		return nonce;
	}

	public String getNonceRaw() {
		return nonceRaw;
	}
}
