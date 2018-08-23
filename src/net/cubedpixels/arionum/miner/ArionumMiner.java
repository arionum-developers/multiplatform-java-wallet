package net.cubedpixels.arionum.miner;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONObject;

import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import net.cubedpixels.arionum.ArionumMain;
import net.cubedpixels.arionum.ui.Modal;

public class ArionumMiner {

	private static boolean running = false;

	// TODO -> STATS
	private static long overallHashes;
	private static long startTimeInMillis;
	private static double lastHashrate = 0;
	private static long minDL;
	private int hasherClock = 0;
	public static boolean ds;

	// TODO -> INSTANCE
	private static ArionumMiner instance;

	// TODO -> MINING INFO
	private static long currentBlock;
	public String pool;
	public String minerName;
	public String publicKey;
	public String address;
	public int threads = 0;

	// TODO -> GET
	private TextArea textField;

	// TODO -> HASHERS AND LISTS
	public ArrayList<ArionumHasher> hashers = new ArrayList<>();
	private ArrayList<Thread> threadCollection = new ArrayList<>();

	public ArionumMiner() {
		instance = this;
	}

	// TODO -> START MINER
	public void start(TextArea textField, String pool, int threads) {

		this.pool = pool;
		this.textField = textField;
		this.threads = threads;

		notify("Preparing start...");

		// TODO: -> USER DATA
		String deviceId = "";
		deviceId = getHardwareID();
		setStartTimeInMillis(System.currentTimeMillis());
		minerName = "AroMiner " + deviceId.substring(0, 5);
		notify("Miner-Name: " + minerName);
		address = ArionumMain.getAddress();
		notify("Address: " + address);
		publicKey = address;
		notify("Public-Key: " + publicKey);

		running = true;
		createUpdateThread();
		notify("Miner running...");
	}

	public String getHardwareID() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
			byte[] hwid = networkInterface.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < hwid.length; i++) {
				sb.append(String.format("%02X%s", hwid[i], (i < hwid.length - 1) ? "-" : ""));
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public void startHashers() {
		for (int i = 0; i < threads; i++) {
			// TODO -> SETUP HASHER
			ArionumHasher hasher = new ArionumHasher();
			hashers.add(hasher);
		}
		updateHashers();
	}

	public void stop() {
		// TODO -> STOP ALL ACTIVIES AND RECOURSES OF MINER
		running = false;
		for (ArionumHasher hasher : hashers) {
			hasher.setForceStop(true);
		}

		threadCollection.clear();
		hashers.clear();
	}

	long lastnotice = 0;

	public void submitShare(final String nonce, String argon, final long submitDL, final long difficulty, long height,
			String type) {
		// TODO -> SEND URL REQUEST TO POOL
		System.out.println("Submitting Share...");

		try {

			URL url = new URL(getPool() + "?q=submitNonce");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());

			StringBuilder data = new StringBuilder();

			data.append(URLEncoder.encode("argon", "UTF-8")).append("=")
					.append(URLEncoder.encode(argon.substring(type.equalsIgnoreCase("CPU") ? 30 : 29), "UTF-8"))
					.append("&");
			data.append(URLEncoder.encode("nonce", "UTF-8")).append("=").append(URLEncoder.encode(nonce, "UTF-8"))
					.append("&");
			data.append(URLEncoder.encode("private_key", "UTF-8")).append("=")
					.append(URLEncoder.encode(publicKey, "UTF-8")).append("&");
			data.append(URLEncoder.encode("public_key", "UTF-8")).append("=")
					.append(URLEncoder.encode(publicKey, "UTF-8")).append("&");
			data.append(URLEncoder.encode("address", "UTF-8")).append("=").append(URLEncoder.encode(address, "UTF-8"))
					.append("&");

			data.append(URLEncoder.encode("height", "UTF-8")).append("=").append(height);

			System.out.println("MAKING REQUEST WITH DATA: " + data);

			out.writeBytes(data.toString());

			out.flush();
			out.close();

			BufferedReader b = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String content = b.readLine();

			JSONObject jsO = new JSONObject(content);

			String status = jsO.getString("status");
			notify("Share found: " + argon);
			if (status.equalsIgnoreCase("ok")) {
				// TODO -> ACCEPT
				notify("Share accepted: " + argon);

				if (submitDL <= 240) {
					// TODO -> FOUND
					notify("Block found: " + argon);
				}
			} else {
				notify("Share rejected: " + argon);

			}
		} catch (Exception e) {
			// TODO -> HANDLE EXCEPTION
			e.printStackTrace();
			if (System.currentTimeMillis() - lastnotice > 5000) {
				lastnotice = System.currentTimeMillis();
				new Modal("Pool URL could not get resolved!", "The url you specified couldnt get resolved!")
						.show(new Stage());
			}
		}
	}

	public void createHasher(final String data, final String pool_key, final long difficulty, final long neededDL,
			final long height, final boolean doMine, final int hf_argon_t_cost, final int hf_argon_m_cost,
			final int hf_argon_para) {
		final ArionumHasher hasher = new ArionumHasher();
		hashers.add(hasher);
		if (!hasher.isActive()) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					hasher.updateHasher(data, pool_key, difficulty, neededDL, height, doMine, hf_argon_t_cost,
							hf_argon_m_cost, hf_argon_para);
					hasher.refreshNonce();
					System.out.println("-> CREATING HASHER ->" + hasher + " METHOD: createHasher");
					hasher.initiate();
				}
			});
			threadCollection.add(thread);
			thread.setDaemon(true);
			thread.setName("Arionum Hasher Thread");
			thread.setPriority(10);
			thread.start();
		}
	}

	public static int currentHashes = 0;
	public static String lastType = "CPU";
	private static ArrayList<Double> hashTime = new ArrayList<>();

	private double calculateAverage(ArrayList<Double> marks) {
		double sum = 0;
		if (!marks.isEmpty()) {
			for (Double mark : marks) {
				sum += mark;
			}
			return sum / marks.size();
		}
		return sum;
	}

	public double getCurrentHashrate() {
		ArrayList<Double> savedHashes = new ArrayList<>();
		if (hashTime.size() >= 6) {
			savedHashes.clear();
			savedHashes.add(hashTime.get(1));
			savedHashes.add(hashTime.get(2));
			savedHashes.add(hashTime.get(3));
			savedHashes.add(hashTime.get(4));
			savedHashes.add(hashTime.get(5));
			hashTime.clear();
			hashTime.addAll(savedHashes);
		}
		hashTime.add((double) (currentHashes / 2));
		double currentHashRate = calculateAverage(hashTime);
		hashTime.set(hashTime.size() - 1, currentHashRate);
		currentHashes = 0;
		return currentHashRate;
	}

	public void createUpdateThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				startHashers();
				while (running) {
					try {
						Thread.sleep(1000 * 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!running)
						return;
					updateHashers();
				}
				System.out.println("UPDATE THREAD DIED!");
			}
		});
		thread.setDaemon(true);
		thread.setName("Arionum Miner Update Thread");
		thread.setPriority(10);
		thread.start();
		threadCollection.add(thread);
	}

	public void updateHashers() {
		// TODO -> UPDATE HASHERS WITH NEW VARS

		hasherClock++;
		// TODO -> GET DATA FROM POOL!

		String data = "";
		String pool_key = "";
		long difficulty = 0;
		long neededDL = 0;
		long height = 0;

		// TODO->HARDFORK 80K
		boolean doMine = true;
		int hf_argon_t_cost = 1;
		int hf_argon_m_cost = 524288;
		int hf_argon_para = 1;

		String url = null;
		try {
			url = getPool() + "?q=info&worker=" + URLEncoder.encode(getMinerName(), "UTF-8");
			if (hasherClock > 30 * 3) {
				hasherClock = 0;
				url += "&address=" + ArionumMain.getAddress() + "&hashrate=" + getLastHashrate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			@SuppressWarnings("resource")
			String content = new Scanner(uc.getInputStream()).nextLine();
			JSONObject o = new JSONObject(content);
			JSONObject jsonData = (JSONObject) o.get("data");
			String localData = (String) jsonData.get("block");
			data = localData;
			BigInteger localDifficulty = new BigInteger((String) jsonData.get("difficulty"));
			difficulty = localDifficulty.longValue();
			long limitDL = Long.parseLong(jsonData.get("limit").toString());
			neededDL = limitDL;
			long localHeight = jsonData.getLong("height");
			height = localHeight;
			String publicpoolkey = jsonData.getString("public_key");
			pool_key = publicpoolkey;

			if (jsonData.getString("recommendation") != null) {
				String recomm = jsonData.getString("recommendation");
				if (!recomm.equals("mine")) {
					doMine = false;
					notify("Waiting for MN-Block...");
				}
				int argon_mem = jsonData.getInt("argon_mem");
				int argon_threads = jsonData.getInt("argon_threads");
				int argon_time = jsonData.getInt("argon_time");

				if (doMine) {
					String type = "CPU";
					if (argon_mem < 500000)
						type = "GPU";

					if (!lastType.equals(type)) {
						lastType = type;
						System.out.println("RESETING SCORE");
						hashTime.clear();
					}

					int hasherss = 0;
					for (ArionumHasher hasher : hashers)
						if (!hasher.isSuspended() && hasher.isActive())
							hasherss++;
					notify("Hashers:" + hasherss + "  |   Type:" + type + "   |   Hashrate: " + getCurrentHashrate());
				}

				hf_argon_m_cost = argon_mem;
				hf_argon_para = argon_threads;
				hf_argon_t_cost = argon_time;
			} else {
				notify("Mining on outdated pool!");
			}

		} catch (Exception e) {
			// TODO -> HANDLE FAILED URL RESPONSE
			e.printStackTrace();
			if (System.currentTimeMillis() - lastnotice > 5000) {
				lastnotice = System.currentTimeMillis();
				new Modal("Pool URL could not get resolved!", "The url you specified couldnt get resolved!")
						.show(new Stage());
			}
			return;
		}

		ArionumMiner.minDL = neededDL;
		ArionumMiner.currentBlock = height;

		String type = "CPU";
		if (hf_argon_m_cost < 500000 && doMine)
			type = "GPU";

		for (final ArionumHasher hasher : hashers) {
			hasher.updateHasher(data, pool_key, difficulty, neededDL, height, doMine, hf_argon_t_cost, hf_argon_m_cost,
					hf_argon_para);
			if (!hasher.isInitiated()) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						hasher.initiate();
					}
				});
				threadCollection.add(thread);
				thread.setDaemon(true);
				thread.setName("Arionum Hasher Thread");
				thread.setPriority(10);
				thread.start();
			}
		}

		// TODO -> DISABLE HASHERS FOR GPU BLOCK TO MAKE 4THREADS FASTER

		int maxhashers = threads / hf_argon_para;
		if (maxhashers < 1)
			maxhashers = 1;

		int active = 0;
		for (final ArionumHasher hasher : hashers) {
			if (type.equalsIgnoreCase("GPU")) {
				if (!hasher.isSuspended())
					active++;
				if (active > maxhashers && !hasher.isSuspended()) {
					System.out.println("SUSPENING HASHER: " + hasher);
					hasher.setSuspended(true);
				}
			} else {
				if (hasher.isSuspended()) {
					hasher.setSuspended(false);
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							hasher.initiate();
						}
					});
					threadCollection.add(thread);
					thread.setDaemon(true);
					thread.setName("Arionum Hasher Thread");
					thread.setPriority(10);
					thread.start();
				}
			}
		}
	}

	public void notify(String text) {
		textField.setText(textField.getText() + "\nArionum > " + text);
		textField.positionCaret(textField.getText().length());
	}

	public String getAddress() {
		return address;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public int getThreads() {
		return threads;
	}

	public void removeHasher(ArionumHasher hasher) {
		hashers.remove(hasher);
	}

	public static long getStartTimeInMillis() {
		return startTimeInMillis;
	}

	public static void setStartTimeInMillis(long startTimeInMillis) {
		ArionumMiner.startTimeInMillis = startTimeInMillis;
	}

	public static long getMinDL() {
		return minDL;
	}

	public static long getCurrentBlock() {
		return currentBlock;
	}

	public static long getOverallHashes() {
		return overallHashes;
	}

	public static boolean isRunning() {
		return running;
	}

	public static ArionumMiner getInstance() {
		return instance;
	}

	public static double getLastHashrate() {
		return lastHashrate;
	}

	public static void setLastHashrate(double lastHashrate) {
		ArionumMiner.lastHashrate = lastHashrate;
	}

	public void setOverallHashes(long overallHashes) {
		ArionumMiner.overallHashes = overallHashes;
	}

	public String getMinerName() {
		return minerName;
	}

	public String getPool() {
		if (pool == null)
			return null;
		if (pool.contains(".php"))
			return pool;
		else
			return pool + "/mine.php";
	}

}
