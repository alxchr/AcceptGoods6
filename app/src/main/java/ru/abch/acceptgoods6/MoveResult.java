package ru.abch.acceptgoods6;

class MoveResult {
    public boolean success;
    public int counter;
    public GoodsMove[] Moves;
    public MoveResult(boolean success, int counter) {
        this.success = success;
        this.counter = counter;
        this.Moves = null;
    }
}
