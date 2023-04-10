package ru.abch.acceptgoods6;

public class GoodsRow {
    public String id, description, article, brand, cellId, cell;
    public int qnt, distance;
    public PackDetail[] packDetails;
    GoodsRow(String id, int qnt, String description, String article, String brand, String cellId, String cell) {
        this.cell = cell;
        this.cellId = cellId;
        this.qnt = qnt;
        this.id = id;
        this.article = article;
        this.brand = brand;
        this.description = description;
        this.distance = (Database.getCellById(cellId) == null)? 0 : Database.getCellById(cellId).distance;
    }
    public int getDistance(int from) {
        return (from > distance)? from - distance : distance - from;
    }
}
