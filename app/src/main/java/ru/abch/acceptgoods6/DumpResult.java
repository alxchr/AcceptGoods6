package ru.abch.acceptgoods6;

public class DumpResult {
	public boolean success;
	public int counter;
	public long[] rows;
	public DumpResult(boolean success, int counter) {
		this.success = success;
		this.counter = counter;
		this.rows = null;
	}
}
