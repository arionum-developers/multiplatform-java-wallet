package net.cubedpixels.arionum.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import javafx.embed.swing.JFXPanel;

public class InitUI {

	public static JFrame window;

	public static void initUI() {
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
	public static void centreWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2) - 80;
		frame.setLocation(x, y);
	}
}