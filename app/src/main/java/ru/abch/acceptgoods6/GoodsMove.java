package ru.abch.acceptgoods6;

public class GoodsMove {
    String id, cellOut, cellIn, time, newBarcode, dctNum, cellOutId, cellInId;
    int storeman, qnt;
    long rowId;
    GoodsMove(int storeman, String id, String cellOut, String cellIn, String time, String newBarcode, String cellOutId, String cellInId){
        this.storeman = storeman;
        this.id = id;
        this.cellOut = cellOut;
        this.cellIn = cellIn;
        this.time = time;
        this.newBarcode = newBarcode;
        this.cellInId = cellInId;
        this.cellOutId = cellOutId;
    }
}
