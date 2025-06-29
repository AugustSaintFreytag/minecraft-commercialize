package net.saint.commercialize.data.payment;

public enum PaymentMethod {
	INVENTORY, ACCOUNT;

	public PaymentMethod next() {
		var methods = values();
		var nextIndex = (this.ordinal() + 1) % methods.length;

		return methods[nextIndex];
	}
}
