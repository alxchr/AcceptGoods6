package ru.abch.acceptgoods6;

public class CellsResult {
	public boolean success;
	public int counter;
	public Cell[] cells;
	public CellsResult(boolean success, int counter) {
		this.success = success;
		this.counter = counter;
		this.cells = null;
	}
}
