package net.cubedpixels.arionum.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cubedpixels.arionum.ArionumMain;
import net.cubedpixels.arionum.api.ApiRequest;
import net.cubedpixels.arionum.api.ApiRequest.RequestFeedback;
import net.cubedpixels.arionum.api.AroApi;
import net.cubedpixels.arionum.api.Base58;
import net.cubedpixels.arionum.api.Base58.QRCallback;
import net.cubedpixels.arionum.api.Config;
import net.cubedpixels.arionum.api.Transaction;
import net.cubedpixels.arionum.miner.ArionumMiner;

public class HomeView extends Application {

	private ArrayList<Page> pages = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) {
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		primaryStage.setTitle("Arionum Wallet - Dashboard");
		
		try {
			Parent root = FXMLLoader.load(getClass().getResource("HomeScreen.fxml"));
			Scene scene = new Scene(root, 714, 419);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setResizable(false);
			primaryStage.setScene(scene);

			setupNavbar(root, primaryStage);
			setupAddressAndAlias(root);
			setupTransactions(root);
			setupIcon(primaryStage);

			primaryStage.show();
			transactionTimer(root, primaryStage);
			Platform.runLater(() -> root.requestFocus());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupAddressAndAlias(Parent root) {
		TextField addressline = (TextField) root.lookup("#addressline");
		addressline.setEditable(false);
		addressline.setText(ArionumMain.getAddress());
	}

	public void setupIcon(Stage prStage) {
		prStage.getIcons().add(new Image(HomeView.class.getResourceAsStream("/arionum_icon.png")));
	}

	private void setupNavbar(Parent root, Stage primaryStage) {
		Pane homeViewPane = ((Pane) root.lookup("#HomeView"));
		Pane sendViewPane = ((Pane) root.lookup("#SendView"));
		Pane minerViewPane = ((Pane) root.lookup("#MinerView"));
		Pane infoViewPane = ((Pane) root.lookup("#InfoView"));
		pages.add(new Page("Home", homeViewPane) {
		});
		pages.add(new Page("Send", sendViewPane) {
		});
		pages.add(new Page("Miner", minerViewPane) {
		});
		pages.add(new Page("Info", infoViewPane) {
		});

		ArrayList<Pane> buttons = new ArrayList<>();
		buttons.add(((Pane) root.lookup("#HomeButton")));
		buttons.add(((Pane) root.lookup("#SendButton")));
		buttons.add(((Pane) root.lookup("#MinerButton")));
		buttons.add(((Pane) root.lookup("#InfoButton")));

		for (Pane p : buttons)
			p.setOnMouseReleased(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					Platform.runLater(() -> root.requestFocus());
					showPage(p.getId().replace("Button", ""));
					event.consume();
				}
			});

		// DRAG

		Pane navbar = (Pane) root.lookup("#Navigation");

		final Delta dragDelta = new Delta();
		navbar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				dragDelta.x = primaryStage.getX() - mouseEvent.getScreenX();
				dragDelta.y = primaryStage.getY() - mouseEvent.getScreenY();
			}
		});
		navbar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.setX(mouseEvent.getScreenX() + dragDelta.x);
				primaryStage.setY(mouseEvent.getScreenY() + dragDelta.y);
			}
		});

		root.lookup("#closebutton").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.close();
				System.exit(1);
			}
		});
		root.lookup("#hidebutton").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.setIconified(true);
			}
		});

		setupButtons(root, primaryStage);
		setupFields(root);
	}

	public void setupButtons(Parent root, Stage primaryStage) {
		root.lookup("#logout").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				ArionumMain.getConfig().deleteString("arionum_public_key");
				ArionumMain.getConfig().deleteString("arionum_private_key");
				ArionumMain.getConfig().deleteWalletFile();
				LoginView.startView();
				Platform.runLater(() -> primaryStage.close());
			}
		});
		root.lookup("#sendtransaction").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				TextField address_field = (TextField) root.lookup("#tofield");
				TextField value_field = (TextField) root.lookup("#valuefield");
				TextField message_field = (TextField) root.lookup("#messagefield");

				String address = address_field.getText();
				double dvalue = Double.parseDouble(value_field.getText());
				String message = message_field.getText();
				String version = "1";

				if (address.length() < 26)
					version = "2";

				AroApi.makeTransaction(address, dvalue, message, version, new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								setupTransactions(root);
							}
						});
					}
				});
			}
		});

		// minerbox
		// startbutton
		TextField field = (TextField) root.lookup("#poolURL");
		field.setText("http://aro.cool/");
		root.lookup("#startbutton").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (ArionumMiner.isRunning()) {
					ArionumMiner.getInstance().stop();
					((Button) root.lookup("#startbutton")).setText("Start");
				} else {
					TextArea area = (TextArea) root.lookup("#minerbox");
					ArionumMiner miner = new ArionumMiner();

					int max = Runtime.getRuntime().availableProcessors();
					long maxMemory = Runtime.getRuntime().maxMemory();
					int MAXramThreads = (int) ((maxMemory / 0x100000L) / 512);
					if (MAXramThreads <= max)
						max = (int) (MAXramThreads);
					miner.start(area, field.getText(), max);
					if (ArionumMiner.isRunning()) {
						((Button) root.lookup("#startbutton")).setText("Stop");
					}
				}
			}
		});

		TextField youralias = (TextField) root.lookup("#youralias");
		Button createalias = (Button) root.lookup("#createalias");

		AroApi.getAlias(new Runnable() {

			@Override
			public void run() {
				String alias = ArionumMain.getAlias();
				if (alias.equals("none")) {
					createalias.setVisible(true);
					createalias.setDisable(false);
				} else {
					youralias.setVisible(true);
					youralias.setDisable(false);
					youralias.setText(alias);
				}
			}
		});

		Button walletstorage = (Button) root.lookup("#walletstorage");
		walletstorage.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				try {
					Desktop.getDesktop().open(Config.getWalletFileLocation().getParentFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	public void setupFields(Parent root) {
		TextField field = (TextField) root.lookup("#pkeyfield");
		field.setText(ArionumMain.getPublicKey());
		field = (TextField) root.lookup("#prkeyfield");
		field.setText(ArionumMain.getPrivateKey());

		ImageView imageView = (ImageView) root.lookup("#qrimage");
		Base58.generateQR(new QRCallback() {
			@Override
			public void onQRgenerate(Image i) {
				imageView.setImage(i);
			}
		});
	}

	private void setupBalance(Parent root) {
		Text balancevalue = (Text) root.lookup("#balancevalue");
		ApiRequest.requestFeedback(new RequestFeedback() {

			@Override
			public void onFeedback(JSONObject object) throws JSONException {
				String value = object.get("data").toString();
				balancevalue.setText(value + "ARO");
			}
		}, "getBalance", new ApiRequest.Argument("account", ArionumMain.getAddress()));
	}

	public void transactionTimer(Parent root, Stage s) {
		new Thread(new Runnable() {
			int i = 0;

			@Override
			public void run() {
				while (s.isShowing()) {
					try {
						Thread.sleep(1000 * 1);
						if (i > 60) {
							i = 0;
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									setupTransactions(root);
								}
							});
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("THREAD DIED!");
			}
		}).start();
	}

	private void setupTransactions(Parent root) {
		setupBalance(root);

		@SuppressWarnings("unchecked")
		TableView<Transaction> tableView = (TableView<Transaction>) root.lookup("#tableview");

		for (TableColumn<Transaction, ?> tc : tableView.getColumns()) {
			tc.setCellValueFactory(new PropertyValueFactory<>(tc.getText().toLowerCase()));
		}

		tableView.setRowFactory(tv -> {
			TableRow<Transaction> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {

				}
			});
			return row;
		});

		ApiRequest.requestFeedback(new RequestFeedback() {

			@Override
			public void onFeedback(JSONObject object) throws JSONException {
				tableView.getItems().clear();

				JSONArray array = object.getJSONArray("data");
				int size = array.length();
				for (int i = 0; i < size; i++) {
					JSONObject o = array.getJSONObject(i);

					String id = o.getString("id");
					String from = o.getString("src");
					String to = o.getString("dst");
					String message = "";
					Object msg = o.get("message");
					if (msg != null && msg instanceof String) {
						message = o.getString("message");
					}
					String action = o.getString("type");
					String date = "";
					long date_long = o.getLong("date");

					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(date_long * 1000);

					int mYear = calendar.get(Calendar.YEAR);
					int mMonth = calendar.get(Calendar.MONTH);
					int mDay = calendar.get(Calendar.DAY_OF_MONTH);

					String mHour = calendar.get(Calendar.HOUR_OF_DAY) + "";
					String mMinute = calendar.get(Calendar.MINUTE) + "";
					if (mHour.length() < 2)
						mHour = "0" + mHour;
					if (mMinute.length() < 2)
						mMinute = "0" + mMinute;

					date = mDay + "-" + mMonth + "-" + mYear + " " + mHour + ":" + mMinute;

					String value = doubleVal(o.getDouble("val"));
					String fee = doubleVal(o.getDouble("fee"));
					String confirmations = doubleVal(o.getDouble("confirmations"));

					tableView.getItems()
							.add(new Transaction(id, from, to, message, action, date, value, fee, confirmations));
				}
			}
		}, "getTransactions", new ApiRequest.Argument("account", ArionumMain.getAddress()),
				new ApiRequest.Argument("limit", "20"));
	}

	public static void showView() {
		new HomeView().start(new Stage());
	}

	// UTILS

	public void showPage(String name) {
		for (Page p : pages) {
			if (p.getName().equalsIgnoreCase(name)) {
				p.onEnable();
				p.getLayout().setVisible(true);
				p.getLayout().setDisable(false);
			} else {
				if (p.getLayout().isVisible()) {
					p.onDisable();
				}
				p.getLayout().setVisible(false);
				p.getLayout().setDisable(true);
			}

		}
	}

	public static int getDecimals(double d) {
		String[] splitt = (d + "").split("\\.");
		if (splitt.length <= 1)
			return 0;
		String after = splitt[1];
		while (after.lastIndexOf("0") == after.length())
			after = after.substring(0, after.length() - 1);
		if (after.length() > 10)
			return 10;
		return after.length();
	}

	public static String doubleVal(final double d) {
		int afterlength = getDecimals(d);
		String temp = "";
		for (int i = 0; i < afterlength; i++)
			temp += "#";
		DecimalFormat format = new DecimalFormat("0." + temp);

		return format.format(d).replace(",", ".");
	}

	public static String doubleVal(final Double d) {
		return d == null ? "" : doubleVal(d.doubleValue());
	}

	public static class Delta {
		double x, y;
	}
}
