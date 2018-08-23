package net.cubedpixels.arionum.api;

public class Transaction {

	private String id;
	private String from;
	private String to;
	private String message;

	private String action;
	private String date;

	private String value;
	private String fee;
	private String confirmations;

	public Transaction(String id, String from, String to, String message, String action, String date, String value,
			String fee, String confirmations) {
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

	public String getDate() {
		return date;
	}

	public String getValue() {
		return value;
	}

	public String getFee() {
		return fee;
	}

	public String getConfirmations() {
		return confirmations;
	}
}
