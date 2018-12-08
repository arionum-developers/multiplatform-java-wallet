package net.cubedpixels.arionum.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Notification extends Application {

	
	private static String value;
	private static String from;
	private static String type;

	@Override
	public void start(Stage primaryStage) {
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Arionum Wallet - Dashboard");
		
		primaryStage.setTitle("Arionum Wallet - Notification");
		
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Notification.fxml"));
			Scene scene = new Scene(root, 405, 90);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			
			primaryStage.setAlwaysOnTop(true);
			
			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();

			primaryStage.setX(bounds.getMinX()+bounds.getWidth() - 0);
			primaryStage.setY(bounds.getMinY()+bounds.getHeight() - 90);
			
			
			{
				int i = 0;
				for(Node n : root.getChildrenUnmodifiable())
				{
					if(n instanceof Text)
					{
						Text t = (Text) n;
						if(i==0)
						{
							i++;
							if(type == "credit")
								t.setText("Received "+value+" ARO from");
							else
								t.setText("Sent "+value+" ARO to");
						}else{
							t.setText(from);
						}
					}
				}
			}

			primaryStage.setWidth(0);
			primaryStage.show();

			
			
			ChangeListener<Number> widthListener = new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					
					int i = 0;
					for(Node n : root.getChildrenUnmodifiable())
					{
						if(n instanceof Text)
						{
							int nullable = 405;
							Text t = (Text) n;
							if(i==0)
							{
								i++;
							}else{
								t.setTranslateX(-nullable*2+newValue.doubleValue()*2);
							}
						}
					}
				}
			};


			primaryStage.widthProperty().addListener(widthListener);
			
			
			Platform.runLater(() -> root.requestFocus());
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					//ANIM
					double width = 0;
					double accel = 0.01;
					while(width < 405)
					{
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						accel+=0.009;
						width+=accel;
						final double w2 = width;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								primaryStage.setWidth(w2);
								primaryStage.setX(bounds.getMinX()+bounds.getWidth() - w2);
							}
						});
					}
					
					try {
						Thread.sleep(2*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							primaryStage.hide();
						}
					});
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void showView(String value, String from, String type) {
		Notification.value = value;
		Notification.from = from;
		Notification.type = type;
		new Notification().start(new Stage());
	}

	
}
