package ru.abch.acceptgoods6;

public class WarehousesResult {
	public boolean success;
	public int counter;
	public Warehouse[] Warehouses;
	public WarehousesResult(boolean success, int counter) {
		this.success = success;
		this.counter = counter;
		this.Warehouses = null;
	}
}
