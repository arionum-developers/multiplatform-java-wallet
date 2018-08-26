package net.cubedpixels.arionum.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import javafx.embed.swing.JFXPanel;

public class InitUI {

	public static JFrame window;

	@SuppressWarnings({ })
	public static void initUI() {
		setupAppleLogo();
		window = new JFrame("Loading Arionum...");
		window.setAlwaysOnTop(true);
		window.setUndecorated(true);
		window.setIconImage(new ImageIcon(InitUI.class.getClassLoader().getResource("arionum_icon.png")).getImage());
		window.setBackground(new Color(0, 0, 0, 0));
		window.getContentPane().setBackground(new Color(0, 0, 0, 0));
		ImageIcon icon = new ImageIcon(InitUI.class.getClassLoader().getResource("aro-main-white.png"));
		JLabel jLabel4 = new JLabel(icon);
		window.getContentPane().add(jLabel4, SwingConstants.CENTER);
		window.setSize(icon.getIconWidth(), icon.getIconHeight());
		centreWindow(window);
		window.setVisible(true);
		new JFXPanel();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setupAppleLogo() {
		try {
			Class util = Class.forName("com.apple.eawt.Application");
		    Method getApplication = util.getMethod("getApplication", new Class[0]);
		    Object application = getApplication.invoke(util);
		    Class params[] = new Class[1];
		    params[0] = Image.class;
		    Method setDockIconImage = util.getMethod("setDockIconImage", params);
		    URL url = InitUI.class.getClassLoader().getResource("arionum_icon.png");
		    Image image = Toolkit.getDefaultToolkit().getImage(url);
		    setDockIconImage.invoke(application, image);
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		} catch (InvocationTargetException e) {
		} catch (IllegalAccessException e) {
		}
	}

	public static void centreWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2) - 80;
		frame.setLocation(x, y);
	}
}