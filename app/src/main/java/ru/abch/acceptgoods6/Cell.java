package ru.abch.acceptgoods6;

public class Cell {
	public String id, name, descr, zonein, zonein_descr;
	public int type, distance;
	public Cell(String id, String name, String descr, int type, int distance, String zonein, String zonein_descr) {
		this.id = id;
		this.name = name;
		this.descr = descr;
		this.type = type;
		this.distance = distance;
		this.zonein = zonein;
		this.zonein_descr = zonein_descr;
	}
	public int getDistance(int from) {
		int ret = (from > distance)? from - distance : distance - from;
		return ret;
	}
}
