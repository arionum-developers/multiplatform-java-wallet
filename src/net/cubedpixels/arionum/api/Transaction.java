package net.cubedpixels.arionum.api;

import java.util.Date;

public class Transaction {

	private String id;
	private String from;
	private String to;
	private String message;

	private String action;
	private Date date;

	private double value;
	private double fee;
	private long confirmations;

	public Transaction(String id, String from, String to, String message, String action, Date date, double value,
			double fee, long confirmations) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.message = message;

		this.action = action;
		this.date = date;

		this.value = value;
		this.fee = fee;
		this.confirmations = confirmations;
	}

	public String getId() {
		return id;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getMessage() {
		return message;
	}

	public String getAction() {
		return action;
	}

	public Date getDate() {
		return date;
	}

	public double getValue() {
		return value;
	}

	public double getFee() {
		return fee;
	}

	public long getConfirmations() {
		return confirmations;
	}
}
