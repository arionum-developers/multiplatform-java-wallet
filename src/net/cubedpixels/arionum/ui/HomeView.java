package net.cubedpixels.arionum.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
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
		primaryStage.initStyle(StageStyle.UNDECORATED);

		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Arionum Wallet - Dashboard");

		primaryStage.setTitle("Arionum Wallet - Dashboard");

		try {
			Parent root = FXMLLoader.load(getClass().getResource("HomeScreen.fxml"));
			Scene scene = new Scene(root, 714, 419);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setResizable(true);
			primaryStage.setScene(scene);

			setupNavbar(root, primaryStage);
			setupAddressAndAlias(root);
			setupTransactions(root);
			setupIcon(primaryStage);
			
			ResizeHelper.addResizeListener(primaryStage);
			onViewChange(primaryStage, root);

			primaryStage.show();
			transactionTimer(root, primaryStage);
			Platform.runLater(() -> root.requestFocus());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateToNewValueWidth(Parent root, double oldValue, double newValue) {
		AnchorPane roots = (AnchorPane) root;
		ArrayList<Node> nodes = getWidthNodes(roots);

		roots.prefWidth((double) newValue);
		roots.minWidth((double) newValue);
		roots.maxWidth((double) newValue);

		int resetValue = -714;

		((ImageView)root.lookup("#closebutton")).setX(resetValue+newValue-14);
		((ImageView)root.lookup("#resizebutton")).setX(resetValue+newValue-14);
		((ImageView)root.lookup("#hidebutton")).setX(resetValue+newValue-14);
		
		
		

		((Text)root.lookup("#balancevalue")).setX(resetValue+newValue-14);
		((Text)root.lookup("#balance-text")).setX(resetValue+newValue-14);
		
		((Text)root.lookup("#alias_text")).setX(resetValue+newValue-14);
		((TextField)root.lookup("#youralias")).setTranslateX(resetValue+newValue-14);
		((Button)root.lookup("#createalias")).setTranslateX(resetValue+newValue-14);
		
		
		((TextField)root.lookup("#addressline")).setPrefWidth(
				-((TextField)root.lookup("#addressline")).getBoundsInParent().getMinX()+
				((Text)root.lookup("#alias_text")).getBoundsInParent().getMinX() - 10);


		((Text)root.lookup("#send_title")).setTranslateX(resetValue/2 +newValue/2);
		((Text)root.lookup("#send_desc")).setTranslateX(resetValue/2 +newValue/2);
		

		((TextField)root.lookup("#tofield")).setPrefWidth(-((TextField)root.lookup("#tofield")).getBoundsInParent().getMinX()*2 +newValue);

		((TextField)root.lookup("#valuefield")).setPrefWidth(-600 +newValue);
		((TextField)root.lookup("#messagefield")).setPrefWidth(-400 +newValue);

		((Button)root.lookup("#sendtransaction")).setTranslateX(resetValue+newValue-14);
		

		((TextArea)root.lookup("#minerbox")).setPrefWidth(-((TextArea)root.lookup("#minerbox")).getBoundsInParent().getMinX()*2 +newValue);
		((Button)root.lookup("#startbutton")).setTranslateX(resetValue+newValue-14);

		((Text)root.lookup("#miner_title")).setTranslateX(resetValue/2 +newValue/2);
		((Text)root.lookup("#miner_desc")).setTranslateX(resetValue/2 +newValue/2);
		

		((Text)root.lookup("#info_title")).setTranslateX(resetValue/2 +newValue/2);
		

		((TextField)root.lookup("#pkeyfield")).setPrefWidth(-((TextField)root.lookup("#pkeyfield")).getBoundsInParent().getMinX()*2 +newValue);
		((TextField)root.lookup("#prkeyfield")).setPrefWidth(-((TextField)root.lookup("#prkeyfield")).getBoundsInParent().getMinX()*2 +newValue);
		

		((Button)root.lookup("#backup")).setTranslateX(resetValue+newValue-14);
		((Button)root.lookup("#walletstorage")).setTranslateX(resetValue+newValue-14);
		

		((Text)root.lookup("#wallet_QR_text")).setTranslateX(resetValue/2 +newValue/2);
		((ImageView)root.lookup("#qrimage")).setTranslateX(resetValue/2 +newValue/2);
		
		
		for (Node node : nodes) {
			if (node instanceof Pane) {
				Pane pane = (Pane) root.lookup("#" + node.getId());

				pane.setPrefWidth((double) newValue);
				pane.setMinWidth((double) newValue);
				pane.setMaxWidth((double) newValue);
			}
			if (node instanceof TableView) {
				@SuppressWarnings("rawtypes")
				TableView pane = (TableView) node;
				pane.setPrefWidth((double) newValue);
				pane.setMinWidth((double) newValue);
				pane.setMaxWidth((double) newValue);
			}
		}
	}

	private void updateToNewValueHeight(Parent root, double oldValue, double newValue) {
		AnchorPane roots = (AnchorPane) root;
		ArrayList<Node> nodes = getHeightNodes(roots);

		roots.setPrefHeight((double) newValue);
		roots.setMinHeight((double) newValue);
		roots.setMaxHeight((double) newValue);

		int resetValue = -419;

		((TextArea)root.lookup("#minerbox")).setPrefHeight(-((TextArea)root.lookup("#minerbox")).getBoundsInParent().getMinY() +newValue -50);
		((Button)root.lookup("#startbutton")).setTranslateY(resetValue + newValue - 30);
		

		((Button)root.lookup("#logout")).setTranslateY(resetValue+newValue-30);
		((Button)root.lookup("#backup")).setTranslateY(resetValue+newValue-30);
		((Button)root.lookup("#walletstorage")).setTranslateY(resetValue+newValue-30);
		
		((Text)root.lookup("#wallet_QR_text")).setTranslateY(resetValue+newValue-30);
		((ImageView)root.lookup("#qrimage")).setTranslateY(resetValue+newValue-30);
		
		for (Node node : nodes) {
			if (node instanceof Pane) {
				Pane pane = (Pane) node;
				pane.setPrefHeight((double) newValue);
				pane.setMinHeight((double) newValue);
				pane.setMaxHeight((double) newValue);
			}
			if (node instanceof TableView) {
				@SuppressWarnings("rawtypes")
				TableView pane = (TableView) node;
				pane.setPrefHeight((double) newValue);
				pane.setMinHeight((double) newValue);
				pane.setMaxHeight((double) newValue);
			}
		}
	}

	public static ArrayList<Node> getWidthNodes(Pane root) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(root.lookup("#Navigation"));
		nodes.add(root.lookup("#navbar"));
		Pane homeViewPane = ((Pane) root.lookup("#HomeView"));
		Pane sendViewPane = ((Pane) root.lookup("#SendView"));
		Pane minerViewPane = ((Pane) root.lookup("#MinerView"));
		Pane infoViewPane = ((Pane) root.lookup("#InfoView"));
		nodes.add(homeViewPane);
		nodes.add(sendViewPane);
		nodes.add(minerViewPane);
		nodes.add(infoViewPane);

		nodes.add(root.lookup("#tableview"));
		return nodes;
	}

	public static ArrayList<Node> getHeightNodes(Pane root) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(root.lookup("#tableview"));
		return nodes;
	}

	private void onViewChange(Stage stage, Parent root) {
		ChangeListener<Number> widthListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				stage.setWidth((double) newValue);
				stage.setMinWidth((double) 714);

				updateToNewValueWidth(root, (double) oldValue, (double) newValue);

			}
		};
		ChangeListener<Number> heightListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				stage.setHeight((double) newValue);
				stage.setMinHeight((double) 419);

				updateToNewValueHeight(root, (double) oldValue, (double) newValue);
			}
		};

		stage.widthProperty().addListener(widthListener);
		stage.heightProperty().addListener(heightListener);
	}

	private void setupAddressAndAlias(Parent root) {
		TextField addressline = (TextField) root.lookup("#addressline");
		addressline.setEditable(false);
		addressline.setText(ArionumMain.getAddress());
	}

	public void setupIcon(Stage prStage) {
		prStage.getIcons().add(new Image(HomeView.class.getResourceAsStream("/arionum_icon.png")));
		MenuBar menuBar = new MenuBar();
		Platform.runLater(() -> menuBar.setUseSystemMenuBar(true));
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
		

		
		root.lookup("#resizebutton").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(!maximized)
				{
					xprevious = primaryStage.getX();
					yprevious = primaryStage.getY();
					widthprevious = primaryStage.getWidth();
					heightprevious = primaryStage.getHeight();
					
				Screen screen = Screen.getPrimary();
				Rectangle2D bounds = screen.getVisualBounds();
				primaryStage.setX(bounds.getMinX());
				primaryStage.setY(bounds.getMinY());
				primaryStage.setWidth(bounds.getWidth());
				primaryStage.setHeight(bounds.getHeight());
				
				
				}else{
					primaryStage.setX(xprevious);
					primaryStage.setY(yprevious);
					primaryStage.setWidth(widthprevious);
					primaryStage.setHeight(heightprevious);
				}
				maximized = !maximized;
			}
		});

		
		setupButtons(root, primaryStage);
		setupFields(root);
	}
	boolean maximized = false;
	double xprevious,yprevious,widthprevious,heightprevious;

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

				if (address.length() < 26) {
					address = address.toUpperCase();
					version = "2";
				}

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
		field.setText("https://aro.cool/");
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
					if (max < 1)
						max = 1;
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

		createalias.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				new Modal("Alias Setup", "Enter your wished Alias (a-zA-Z0-9|>4|<26)").withTextField()
						.show(primaryStage, new Modal.TextFieldCallback() {

							@Override
							public void onCallback(String content) {
								String newstr = content.replaceAll("[^A-Za-z]+", "").toUpperCase();
								if (newstr.length() < 4 || newstr.length() > 25) {
									new Modal("Alias Error [" + newstr + "]",
											"The Alias has to be greater than 4 and smaller than 26 characters!")
													.show(primaryStage);
									return;
								}

								new Modal("Recheck your Alias", "Please enter your Alias again: [" + newstr + "]")
										.withTextField().show(primaryStage, new Modal.TextFieldCallback() {

											@Override
											public void onCallback(String content) {
												if (content.toUpperCase().equals(newstr)) {
													AroApi.makeTransaction(ArionumMain.getAddress(), 0.00000001, newstr,
															"3", new Runnable() {
																@Override
																public void run() {
																}
															});
												} else {
													new Modal("Alias Error", "The Alias doesnt match!")
															.show(primaryStage);
												}
											}
										});
								;

							}
						});
				;
			}
		});

		Button walletstorage = (Button) root.lookup("#walletstorage");
		walletstorage.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				try {
					File myfile = Config.getWalletFileLocation();
					String path = myfile.getAbsolutePath();
					File dir = new File(path).getParentFile();
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().open(dir);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		Button backup = (Button) root.lookup("#backup");
		backup.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				FileChooser fc = new FileChooser();
				fc.setInitialFileName("wallet.aro");
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Wallet files (*.aro)",
						"*.aro");
				fc.setSelectedExtensionFilter(extFilter);
				File selectedFile = fc.showSaveDialog(primaryStage);
				if (selectedFile != null) {
					if (ArionumMain.getConfig().getWalletFile().exists())
						ArionumMain.getConfig().copyWalletTo(selectedFile);
					else {
						String walletFileContent = "arionum:" + ArionumMain.getPrivateKey() + ":"
								+ ArionumMain.getPublicKey();
						ArionumMain.getConfig().saveWalletFileFromString(walletFileContent, selectedFile);
					}
					new Modal("Done", "File was saved!").show(primaryStage);
				}
			}
		});

	}

	public void setupFields(Parent root) {
		
		TextField field = (TextField) root.lookup("#pkeyfield");
		field.setText(ArionumMain.getPublicKey());
		field = (TextField) root.lookup("#prkeyfield");
		field.setText("*Hover to show*");
		
		final TextField f = field;
		f.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				f.setText(ArionumMain.getPrivateKey());
			}
		});
		f.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				f.setText("*Hover to show*");
			}
		});

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
						} else
							i++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("THREAD DIED!");
				System.exit(0);
			}
		}).start();
	}
	
	public String lastTransID = "";

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
					
					if(i == 0 && lastTransID != "" && lastTransID != id)
					{
						lastTransID = id;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								Notification.showView(doubleVal(o.getDouble("val")),(o.getString("type") == "credit")?from:to
										,o.getString("type"));
							}
						});
					}else if(i == 0 && lastTransID == "") lastTransID = id;
					
					if (msg != null && msg instanceof String) {
						message = o.getString("message");
					}
					String action = o.getString("type");
					String date = "";
					long date_long = o.getLong("date");

					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(date_long * 1000);

					int mYear = calendar.get(Calendar.YEAR);
					int mMonth = calendar.get(Calendar.MONTH) + 1;
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
				new ApiRequest.Argument("limit", "1000"));
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
