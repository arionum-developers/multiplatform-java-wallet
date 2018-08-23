package net.cubedpixels.arionum.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.prefs.Preferences;

public class Config {

	private static Preferences configLocation;
	private static File walletFileLocation = new File(
			System.getProperty("user.home") + "/.Arionum-Wallet/wallet.aro");

	public Config() {
		configLocation = Preferences.userNodeForPackage(Config.class);
		walletFileLocation.getParentFile().mkdirs();
	}

	public void saveString(String key, String value) {
		configLocation.put(key, value);
	}

	public String getString(String key) {
		String propertyValue = configLocation.get(key, null);
		return propertyValue;
	}

	public void deleteString(String key) {
		configLocation.remove(key);
	}

	public File getWalletFile() {
		return walletFileLocation;
	}

	public String getWalletFileContent() {
		FileReader file = null;
		try {
			file = new FileReader(walletFileLocation);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner sc = new Scanner(file);
		String content = "";
		while (sc.hasNextLine())
			content += sc.nextLine();
		sc.close();
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public String getFileContent(File f) {
		FileReader file = null;
		try {
			file = new FileReader(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner sc = new Scanner(file);
		String content = "";
		while (sc.hasNextLine())
			content += sc.nextLine();
		sc.close();
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public void saveWalletFileFromFile(File fileLocation) {
		try {
			if (walletFileLocation.exists())
				Files.copy(walletFileLocation.toPath(),
						walletFileLocation.getParentFile().toPath()
								.resolve("WALLET_BACKUP_" + System.currentTimeMillis() / 1000 / 1000),
						StandardCopyOption.REPLACE_EXISTING);
			walletFileLocation.delete();
			Files.copy(fileLocation.toPath(), walletFileLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void deleteWalletFile() {
		if (walletFileLocation.exists())
			try {
				Files.copy(walletFileLocation.toPath(),
						walletFileLocation.getParentFile().toPath()
								.resolve("WALLET_BACKUP_" + System.currentTimeMillis() / 1000),
						StandardCopyOption.REPLACE_EXISTING);
				walletFileLocation.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void saveWalletFileFromString(String content) {
		try {
			if (walletFileLocation.exists())
				Files.copy(walletFileLocation.toPath(),
						walletFileLocation.getParentFile().toPath()
								.resolve("WALLET_BACKUP_" + System.currentTimeMillis() / 1000 / 1000),
						StandardCopyOption.REPLACE_EXISTING);
			walletFileLocation.delete();
			Files.write(walletFileLocation.toPath(), content.getBytes(), StandardOpenOption.WRITE,
					StandardOpenOption.CREATE_NEW);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static File getWalletFileLocation() {
		return walletFileLocation;
	}
}
