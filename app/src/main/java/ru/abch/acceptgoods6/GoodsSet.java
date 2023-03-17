package ru.abch.acceptgoods6;

class GoodsSet {
    public boolean success;
    public int counter;
    public GoodsRow[] goodsRows;
    public GoodsSet(boolean success, int counter) {
        this.success = success;
        this.counter = counter;
        this.goodsRows = null;
    }
}
