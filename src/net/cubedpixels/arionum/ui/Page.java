package net.cubedpixels.arionum.ui;

import javafx.scene.layout.Pane;

public abstract class Page {
	private Pane layout;
	private String name;

	public Page(String name, Pane layout) {
		this.name = name;
		this.layout = layout;
	}

	public Pane getLayout() {
		return layout;
	}

	public String getName() {
		return name;
	}

	public void onDisable() {
	}

	public void onEnable() {
	}
}