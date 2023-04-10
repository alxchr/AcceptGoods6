package ru.abch.acceptgoods6;

public class PackResult {
	public boolean success;
	public int counter;
	public AcceptGoodsRow[] rows;
	public PackResult(boolean success, int counter) {
		this.success = success;
		this.counter = counter;
		this.rows = null;
	}
}
