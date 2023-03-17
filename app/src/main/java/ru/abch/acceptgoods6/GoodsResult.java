package ru.abch.acceptgoods6;

class GoodsResult {
    public boolean success;
    public int counter;
    public GoodsPosition[] Goods;
    public GoodsResult(boolean success, int counter) {
        this.success = success;
        this.counter = counter;
        this.Goods = null;
    }
    public int storeman;
}
