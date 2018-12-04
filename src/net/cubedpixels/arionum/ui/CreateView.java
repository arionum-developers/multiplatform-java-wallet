package net.cubedpixels.arionum.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cubedpixels.arionum.ArionumMain;
import net.cubedpixels.arionum.api.ArionumKeyPair;
import net.cubedpixels.arionum.api.AroApi;
import net.cubedpixels.arionum.ui.HomeView.Delta;

public class CreateView extends Application {
	@Override
	public void start(Stage primaryStage) {
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Arionum Wallet - Create Wallet");
		
		primaryStage.setTitle("Arionum Wallet - Create Wallet");
		
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Create.fxml"));
			Scene scene = new Scene(root, 345, 568);
			primaryStage.setResizable(false);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);

			setupIcon(primaryStage);
			setupNavbar(root, primaryStage);
			registerButtons(root, primaryStage);

			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setupIcon(Stage prStage) {
		prStage.getIcons().add(new Image(HomeView.class.getResourceAsStream("/arionum_icon.png")));
		MenuBar menuBar = new MenuBar();Platform.runLater(() -> menuBar.setUseSystemMenuBar(true));
	}

	private void setupNavbar(Parent root, Stage primaryStage) {
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
			}
		});
		root.lookup("#hidebutton").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.setIconified(true);
			}
		});
	}

	public void registerButtons(Parent root, Stage primaryStage) {
		Button b = (Button) root.lookup("#loginbutton");
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				LoginView.startView();
				Platform.runLater(() -> primaryStage.close());
			}
		});

		b = (Button) root.lookup("#createbutton");
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				PasswordField field1 = (PasswordField) root.lookup("#pw1");
				PasswordField field2 = (PasswordField) root.lookup("#pw2");

				if (field1.getText().length() < 4) {
					new Modal("Password security error", "Please enter an Password with more than 4 Characters")
							.show(primaryStage);
					return;
				}
				if (!field1.getText().equals(field2.getText())) {
					new Modal("Password match error", "The password you entered did not match").show(primaryStage);
					return;
				}

				// TODO -> CREATE WALLET
				ArionumKeyPair keyPair = AroApi.ecKeyPairGenerator();
				if(keyPair == null)
					return;
				String walletFileContent = "arionum:" + keyPair.getPrivateKey() + ":" + keyPair.getPublicKey();
				String password = field1.getText();
				try {
					walletFileContent = AroApi.encrypt(walletFileContent, password);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ArionumMain.getConfig().saveWalletFileFromString(walletFileContent);
				ArionumMain.initKeys(new Runnable() {

					@Override
					public void run() {

						HomeView.showView();
						Platform.runLater(() -> primaryStage.close());
					}
				});
			}
		});
		b = (Button) root.lookup("#createwithoutbutton");
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				// TODO -> CREATE WALLET
				ArionumKeyPair keyPair = AroApi.ecKeyPairGenerator();
				if(keyPair == null)
					return;
				String walletFileContent = "arionum:" + keyPair.getPrivateKey() + ":" + keyPair.getPublicKey();
				ArionumMain.getConfig().saveWalletFileFromString(walletFileContent);
				ArionumMain.initKeys(new Runnable() {

					@Override
					public void run() {

						HomeView.showView();
						Platform.runLater(() -> primaryStage.close());
					}
				});
			}
		});

	}

	public static void showView() {
		new CreateView().start(new Stage());
	}
}
