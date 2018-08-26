package net.cubedpixels.arionum.ui;

import java.io.File;
import java.util.Base64;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cubedpixels.arionum.ArionumMain;
import net.cubedpixels.arionum.ui.HomeView.Delta;

public class LoginView extends Application {
	@Override
	public void start(Stage primaryStage) {
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Arionum Wallet - Login");
		
		primaryStage.setTitle("Arionum Wallet - Login");
		
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
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

	public void setupIcon(Stage primaryStage) {
		primaryStage.getIcons().add(new Image(HomeView.class.getResourceAsStream("/arionum_icon.png")));
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
		Button b = (Button) root.lookup("#createbutton");
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				CreateView.showView();
				Platform.runLater(() -> primaryStage.close());
			}
		});
		b = (Button) root.lookup("#loginbutton");
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				TextField field1 = (TextField) root.lookup("#pkey");
				TextField field2 = (TextField) root.lookup("#prkey");

				if (field1.getText().isEmpty()) {
					new Modal("Needed field error", "You need to fill in the publickey to login!").show(primaryStage);
					return;
				}

				ArionumMain.getConfig().saveString("arionum_public_key", field1.getText());
				ArionumMain.getConfig().saveString("arionum_private_key", field2.getText());
				ArionumMain.initKeys(new Runnable() {

					@Override
					public void run() {

						HomeView.showView();
						Platform.runLater(() -> primaryStage.close());
					}
				});

			}
		});
		b = (Button) root.lookup("#choosebutton");
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				FileChooser fc = new FileChooser();
				File selectedFile = fc.showOpenDialog(primaryStage);
				if (selectedFile != null) {
					boolean okay = false;
					String content = ArionumMain.getConfig().getFileContent(selectedFile);
					if (content.startsWith("arionum"))
						okay = true;
					try {
						Base64.getDecoder().decode(content.getBytes());
						okay = true;
					} catch (Exception e) {
					}
					if (okay) {
						ArionumMain.getConfig().saveWalletFileFromFile(selectedFile);
						ArionumMain.initKeys(new Runnable() {
							@Override
							public void run() {
								HomeView.showView();
								Platform.runLater(() -> primaryStage.close());
							}
						});
					}
				}
			}
		});
	}

	public static void startView() {
		new LoginView().start(new Stage());
	}
}
