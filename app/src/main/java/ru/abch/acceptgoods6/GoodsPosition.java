package ru.abch.acceptgoods6;

public class GoodsPosition {
    String barcode, description, cell, id, article, time, store, dctNum;
    public int qnt, total, overflow;
    long rowId;
    public String cellId, mdoc, pallet, ipcam, storemanId;
    GoodsPosition(String id, String barcode, String description, String cell, int qnt, String article, int total){
        this.barcode = barcode;
        this.description = description;
        this.cell = cell;
        this.qnt = qnt;
        this.id = id;
        this.article = article;
        this.total = total;
        this.time = "";
        this.cellId = "";
    }
    public String getArticle() {
        return article;
    }
    public String getId() {
        return id;
    }

    public int getQnt() {
        return qnt;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public String getCell() {
        return cell;
    }

    void setCell(String cell) {
        this.cell = cell;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setQnt(int qnt) {
        this.qnt = qnt;
    }
}
