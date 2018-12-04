package net.cubedpixels.arionum;

import java.util.Base64;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.cubedpixels.arionum.api.AroApi;
import net.cubedpixels.arionum.api.Config;
import net.cubedpixels.arionum.ui.CreateView;
import net.cubedpixels.arionum.ui.HomeView;
import net.cubedpixels.arionum.ui.InitUI;
import net.cubedpixels.arionum.ui.Modal;
import net.cubedpixels.arionum.ui.Notification;
import net.cubedpixels.arionum.ui.Modal.TextFieldCallback;

public class ArionumMain {

	private static String peerURL;

	private static String alias = "";
	private static String address = "";
	private static String publicKey = "";
	private static String privateKey = "";
	private static Config config;

	public ArionumMain() {
		init();
	}

	public static void main(String[] args) {
		System.out.println("Starting Arionum Wallet...");
		InitUI.initUI();
		new ArionumMain();
	}

	public void init() {
		
		config = new Config();
		peerURL = "http://wallet.arionum.com/";
		System.out.println("Initing UI....");

		System.out.println("Starting checks...");
		// CHECK IF ALREADY LOGGED IN
		if (!config.getWalletFile().exists() && config.getString("arionum_public_key") == null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					CreateView.showView();
					InitUI.window.setVisible(false);
				}
			});
		} else if (config.getString("arionum_public_key") != null || config.getWalletFile().exists()) {
			initKeys(new Runnable() {
				@Override
				public void run() {
					HomeView.showView();
					InitUI.window.setVisible(false);
				}
			});
		}
	}

	public static void initKeys(Runnable callback) {
		if (config.getString("arionum_public_key") != null) {
			publicKey = config.getString("arionum_public_key");
			privateKey = config.getString("arionum_private_key");
			address = AroApi.getAddress(getPublicKey());
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					callback.run();
				}
			});
		} else {
			String content = getConfig().getWalletFileContent();
			if (content.startsWith("arionum")) {
				String[] sp = content.split(":");
				publicKey = sp[2];
				privateKey = sp[1];
				address = AroApi.getAddress(getPublicKey());
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						callback.run();
					}
				});
			} else {
				decryptKeyFromFile(content, new TextFieldCallback() {

					@Override
					public void onCallback(String content) {
						String[] sp = content.split(":");
						publicKey = sp[2];
						privateKey = sp[1];
						address = AroApi.getAddress(getPublicKey());
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								callback.run();
							}
						});
					}
				});
			}
		}
	}

	private static TextFieldCallback tfc = null;

	private static void decryptKeyFromFile(String content, TextFieldCallback callBack) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				Modal m = new Modal("Wrong Password! Try again...", "Enter the password for your wallet file.")
						.withPasswordField().withCustomButtonText("Enter");
				tfc = new Modal.TextFieldCallback() {
					@Override
					public void onCallback(String pw) {
						if (pw.isEmpty()) {
							new Modal("No Password entered!", "Shutting down!Bye bye..... :C").show(new Stage());
							System.exit(0);
							return;
						}
						String c = checkPasswordAndReturn(content, pw);
						if (c == "WRONG")
							m.show(new Stage(), tfc);
						else
							callBack.onCallback(c);
					}
				};

				new Modal("Enter Password", "Enter the password for your wallet file.").withPasswordField()
						.withCustomButtonText("Enter").show(new Stage(), tfc);
			}
		});
	}

	private static String checkPasswordAndReturn(String content, String pw) {
		byte[] base = Base64.getDecoder().decode(content);
		try {
			String decrypted = AroApi.decrypt(base, pw);
			if (decrypted.startsWith("arionum"))
				return decrypted;
			else
				return "WRONG";
		} catch (Exception e) {
			e.printStackTrace();
			return "WRONG";
		}
	}

	public static Config getConfig() {
		return config;
	}

	public static String getPublicKey() {
		return publicKey;
	}

	public static String getPrivateKey() {
		return privateKey;
	}
	
	public static String getAlias() {
		return alias;
	}
	
	public static void setAlias(String alias) {
		ArionumMain.alias = alias;
	}

	public static String getAddress() {
		return address;
	}

	public static String getPeerURL() {
		return peerURL;
	}

}
