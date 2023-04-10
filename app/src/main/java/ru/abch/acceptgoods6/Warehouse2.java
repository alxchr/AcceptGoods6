package ru.abch.acceptgoods6;

public class Warehouse2 extends Warehouse {
	public Warehouse2(String id, String storeCode, String descr, boolean isShop) {
		super(id, storeCode, descr);
		this.isShop = isShop;
	}
	public boolean isShop;
}
