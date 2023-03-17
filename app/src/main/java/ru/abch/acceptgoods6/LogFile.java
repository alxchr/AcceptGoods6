package ru.abch.acceptgoods6;

public class LogFile {
	public String dirName, fileName;
	public String body;

	public LogFile(String dirName, String fileName, String body) {
		this.dirName = dirName;
		this.fileName = fileName;
		this.body = body;
	}
}
