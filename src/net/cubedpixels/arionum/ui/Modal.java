package net.cubedpixels.arionum.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cubedpixels.arionum.ui.HomeView.Delta;

public class Modal {

	private String title;
	private String content;
	private String customButtonText;
	private boolean textfield;
	private boolean passwordfield;
	private boolean disabledtextfield;
	private String textfieldcontent = "";

	public Modal(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public Modal withCustomButtonText(String text) {
		customButtonText = text;
		return this;
	}

	public Modal withTextField() {
		textfield = true;
		return this;
	}

	public Modal withPasswordField() {
		passwordfield = true;
		return this;
	}

	public Modal withDisabledTextField() {
		disabledtextfield = true;
		return this;
	}

	public Modal withTextFieldContent(String content) {
		textfieldcontent = content;
		return this;
	}

	public void setupIcon(Stage primaryStage) {
		primaryStage.getIcons().add(new Image(HomeView.class.getResourceAsStream("/arionum_icon.png")));
	}

	public void show(Stage owner, TextFieldCallback... callbacks) {
		Stage primaryStage = new Stage();
		
		primaryStage.setTitle("Arionum Wallet - Alert");
		
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Modal.fxml"));
			Scene scene = new Scene(root, 435, 161);
			primaryStage.setResizable(false);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.initOwner(owner);
			primaryStage.initModality(Modality.APPLICATION_MODAL);

			setupButton(root, primaryStage);
			setText(root);
			setupIcon(primaryStage);

			InitUI.window.setVisible(false);
			primaryStage.showAndWait();
			for (TextFieldCallback tfc : callbacks)
				if(!textfield && passwordfield)
					tfc.onCallback(((TextField) root.lookup("#passwordfield")).getText());
				else if(textfield && !passwordfield)
					tfc.onCallback(((TextField) root.lookup("#textfield")).getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setText(Parent root) {
		Text t = (Text) root.lookup("#title");
		Text t1 = (Text) root.lookup("#message");

		t.setText(getTitle());
		t1.setText(getContent());
	}

	public static abstract class TextFieldCallback {
		public abstract void onCallback(String content);
	}

	private void setupButton(Parent root, Stage primaryStage) {

		Pane navbar = (Pane) root.lookup("#main");

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

		Button b = (Button) root.lookup("#closebutton");

		root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				primaryStage.close();
				ev.consume();
			}
		});

		if (customButtonText != null && !customButtonText.isEmpty())
			b.setText(customButtonText);
		b.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.close();
			}
		});
		if (textfield) {
			TextField tf = (TextField) root.lookup("#textfield");
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					tf.requestFocus();
				}
			});
			tf.setVisible(true);
			tf.setDisable(false);
			if (disabledtextfield)
				tf.setEditable(false);
			if (!textfieldcontent.isEmpty())
				tf.setText(textfieldcontent);
		}
		if (passwordfield) {
			TextField tf = (TextField) root.lookup("#passwordfield");
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					tf.requestFocus();
				}
			});
			tf.setVisible(true);
			tf.setDisable(false);
		}
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

}
