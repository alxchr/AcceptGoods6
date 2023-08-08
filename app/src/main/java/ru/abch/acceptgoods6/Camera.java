package ru.abch.acceptgoods6;

public class Camera {
	public String descr, ip, direction, store;
	public int num;
	public Camera(String store, int num, String descr, String ip, String direction) {
		this.store = store;
		this.num = num;
		this.descr = descr;
		this.ip = ip;
		this.direction = direction;
	}
}
