package net.cubedpixels.arionum.miner;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

public class ArionumHasher implements Thread.UncaughtExceptionHandler {

	// TODO -> LIFECYCLE BOOLEANS
	private boolean active = false;
	private boolean suspended = false;
	private boolean forceStop = false;

	private boolean isInitiated = false;
	private boolean doPause = false;
	private Argon2 argon2;
	private MessageDigest messageDigest;
	private Nonce nonce;

	// TODO-> HASHER VARS FOR NONCE / SHARE UPDATES
	private String data;
	private long difficulty;
	private long neededDL;
	private long height;
	private String pool_key;

	// TODO-> HARDFORK 80K
	private boolean doMine = true;

	// TODO-> NEW ARGON2 PARAMS
	private int hf_argon_t_cost = 1;
	private int hf_argon_m_cost = 524288;
	private int hf_argon_para = 1;

	private ArionumMiner minerInstance;

	public ArionumHasher() {
		minerInstance = ArionumMiner.getInstance();
	}

	public void initiate() {
		// TODO -> SETUP ARGON2
		active = true;
		isInitiated = true;

		System.out.println("Initiating Hasher -> " + this);

		argon2 = Argon2Factory.create(Argon2Types.ARGON2i, 16, 32);
		try {
			messageDigest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			// TODO -> CATCH OLD PHONES WITHOUT SHA-512
		}

		Thread.setDefaultUncaughtExceptionHandler(this);

		forceStop = false;
		suspended = false;
		doCycle();
	}

	public void doCycle() {
		// TODO -> GENERATE NONCE
		while (active && !forceStop && !suspended) {
			if (forceStop)
				return;
			if (doPause || !doMine) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			// TODO -> GENERATE ARGON2 HASH
			final Nonce usedNonce = nonce;
			String base = usedNonce.getNonce();
			String result = argon2.hash(hf_argon_t_cost, hf_argon_m_cost, hf_argon_para, base);

			// TODO -> LOOP ARGON2 HASH + NONCE 5 TIMES WITH SHA512
			String hash = base + result;
			byte[] hashBytes = null;
			hashBytes = messageDigest.digest(hash.getBytes());
			for (int i = 0; i < 5; i++) {
				hashBytes = messageDigest.digest(hashBytes);
			}

			// TODO -> GET DL FROM BYES
			StringBuilder duration = new StringBuilder(25);
			duration.append(hashBytes[10] & 0xFF).append(hashBytes[15] & 0xFF).append(hashBytes[20] & 0xFF)
					.append(hashBytes[23] & 0xFF).append(hashBytes[31] & 0xFF).append(hashBytes[40] & 0xFF)
					.append(hashBytes[45] & 0xFF).append(hashBytes[55] & 0xFF);

			long finalDuration = new BigInteger(duration.toString()).divide(new BigInteger(difficulty + ""))
					.longValue();

			ArionumMiner.currentHashes++;

			ArionumMiner.getInstance().setOverallHashes(ArionumMiner.getOverallHashes() + 1);
			String type = "CPU";
			if (hf_argon_m_cost < 500000)
				type = "GPU";

			if (finalDuration <= neededDL) {
				System.out.println("NONCE: " + nonce.getNonce());
				System.out.println("ENCODED: " + result);
				minerInstance.submitShare(nonce.getNonceRaw(), result, finalDuration, difficulty, height, type);
				refreshNonce();
			}

		}
		System.runFinalization();
		Runtime.getRuntime().gc();
		System.gc();
		active = false;
	}

	public void refreshNonce() {
		nonce = new Nonce(getPool_key(), data, difficulty + "", 32);
	}

	// TODO -> UPDATE HASHER
	public void updateHasher(String data, String pool_key, long difficulty, long neededDL, long height, boolean doMine,
			int hf_argon_t_cost, int hf_argon_m_cost, int hf_argon_para) {
		this.data = data;
		this.difficulty = difficulty;
		this.neededDL = neededDL;
		this.pool_key = pool_key;

		this.doMine = doMine;

		this.hf_argon_t_cost = hf_argon_t_cost;
		this.hf_argon_m_cost = hf_argon_m_cost;
		this.hf_argon_para = hf_argon_para;

		if (this.height != height) {
			this.height = height;
			refreshNonce();
		}

		this.height = height;
	}

	public void setForceStop(boolean forceStop) {
		this.forceStop = forceStop;
	}

	public String getPool_key() {
		return pool_key;
	}

	public void doPause(boolean doPause) {
		this.doPause = doPause;
	}

	public boolean isActive() {
		return active;
	}

	public boolean hasForceStop() {
		return forceStop;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public boolean isInitiated() {
		return isInitiated;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		System.runFinalization();
		Runtime.getRuntime().gc();
		System.gc();

		System.out.println("HASHER " + this + " FAILED FOR " + throwable.getMessage());

		active = false;
		forceStop = true;

		boolean contains = ArionumMiner.getInstance().hashers.contains(this);

		ArionumMiner.getInstance().removeHasher(this);
		if (!throwable.getMessage().contains("Memory allocation error") && contains)
			ArionumMiner.getInstance().createHasher(data, pool_key, difficulty, neededDL, height, doMine,
					hf_argon_t_cost, hf_argon_m_cost, hf_argon_para);
	}
}
