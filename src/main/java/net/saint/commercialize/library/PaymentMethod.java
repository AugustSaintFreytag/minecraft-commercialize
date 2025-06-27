package net.saint.commercialize.library;

public enum PaymentMethod {
	INVENTORY, ACCOUNT;

	public PaymentMethod next() {
		var methods = values();
		var nextIndex = (this.ordinal() + 1) % methods.length;

		return methods[nextIndex];
	}
}
