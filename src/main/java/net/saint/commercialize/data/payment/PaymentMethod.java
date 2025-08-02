package net.saint.commercialize.data.payment;

public enum PaymentMethod {
	INVENTORY, ACCOUNT, SPECIFIED_ACCOUNT;

	public PaymentMethod next() {
		var methods = values();
		var nextIndex = (this.ordinal() + 1) % methods.length;

		return methods[nextIndex];
	}
}
