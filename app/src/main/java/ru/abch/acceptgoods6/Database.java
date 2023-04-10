package ru.abch.acceptgoods6;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bosphere.filelogger.FL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Database {
    private static  String TAG = "Database";
    private static final String DB_NAME = "goodsdb";
    private static final int DB_VERSION = 15;
    private static final String DB_TABLE_BARCODES = "barcodes";
    private static final String DB_TABLE_GOODS = "goods";
    private static final String DB_TABLE_ALL_GOODS = "all_goods";
    private static final String DB_TABLE_PLACEDGOODS = "placed_goods";
    private static final String DB_TABLE_CELLS = "cells";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_EXCESSIVE = "excessive";
    private static final String COLUMN_STOREMAN = "storeman";
    private static final String COLUMN_GOODS_CODE = "goods_code";
    private static final String COLUMN_INPUT_CELL = "input_cell";
    private static final String COLUMN_OUTPUT_CELL  = "output_cell";
    private static final String COLUMN_QNT  = "qnt";
    private static final String COLUMN_MOVEGOODS_SCAN_TIME = "scan_time";
    private static final String COLUMN_BARCODE  = "barcode";
    private static final String COLUMN_GOODS_DESC = "goods_desc";
    private static final String COLUMN_GOODS_BRAND = "goods_brand";
    private static final String COLUMN_GOODS_ARTICLE = "goods_article";
    private static final String COLUMN_SENT  = "sent";
    private static final String COLUMN_STORE = "store";
    private static final String COLUMN_CELL_ID = "cell_id";
    private static final String COLUMN_CELL_NAME = "cell_name";
    private static final String COLUMN_CELL_DESCR = "cell_descr";
    private static final String COLUMN_CELL_TYPE = "cell_type";
    private static final String COLUMN_CELL_DISTANCE = "cell_distance";
    private static final String COLUMN_BOX_ID = "box_id";
    private static final String COLUMN_OUTPUT_CELL_ID = "output_cell_id";
    private static final String COLUMN_ZONEIN = "zonein";
    private static final String COLUMN_ZONEIN_DESCR = "zonein_descr";
    private static final String DB_TABLE_PACK = "pack";
    private static final String COLUMN_PACK_QNT  = "pack_qnt";
    private static final String COLUMN_MDOC  = "mdoc";
    private static final String DB_CREATE_BARCODES =
                    "create table " + DB_TABLE_BARCODES + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer " +
                    ");";
    private static final String DB_CREATE_GOODS =
            "create table " + DB_TABLE_GOODS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_OUTPUT_CELL + " text, " +
                    COLUMN_GOODS_ARTICLE + " text, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_GOODS_BRAND + " text, " +
                    COLUMN_BOX_ID + " text, " +
                    COLUMN_OUTPUT_CELL_ID + " text, " +
                    " constraint goodsinbox unique (" + COLUMN_GOODS_CODE + "," + COLUMN_BOX_ID + ")" +
                    ");";
    private static final String DB_CREATE_ALL_GOODS =
            "create table " + DB_TABLE_ALL_GOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_OUTPUT_CELL + " text, " +
                    COLUMN_GOODS_ARTICLE + " text, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_GOODS_BRAND + " text " +

                    ");";
    private static final String DB_CREATE_PLACEDGOODS =
            "create table " + DB_TABLE_PLACEDGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_STOREMAN + " integer, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_INPUT_CELL + " text, " +
                    COLUMN_MOVEGOODS_SCAN_TIME + " text, " +
                    COLUMN_SENT + " integer, " +
                    COLUMN_STORE + " text not null, " +
                    COLUMN_BOX_ID + " text, " +
                    COLUMN_EXCESSIVE + " integer, " +
                    COLUMN_MDOC + " text " +
                    ");";
    private static final String DB_CREATE_CELLS =
            "create table " + DB_TABLE_CELLS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_CELL_ID + " text not null, " +
                    COLUMN_CELL_NAME + " text not null, " +
                    COLUMN_CELL_DESCR + " text not null, " +
                    COLUMN_CELL_TYPE + " integer, " +
                    COLUMN_CELL_DISTANCE + " integer, " +
                    COLUMN_ZONEIN + " text, " +
                    COLUMN_ZONEIN_DESCR + " text" +
                    ");";
    private static final String DB_CREATE_PACK =
            "create table " + DB_TABLE_PACK + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_MDOC + " text, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_PACK_QNT + " integer, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_GOODS_ARTICLE + " text, " +
                    COLUMN_GOODS_BRAND + " text, " +
                    COLUMN_CELL_ID + " text " +
                    ");";
    private final Context mCtx;
    private DBHelper mDBHelper;
    private static SQLiteDatabase mDB;
//    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//    private static ConnectionClass connectionClass;
    Database(Context ctx) {
        mCtx = ctx;
    }

    void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        try {
            mDB = mDBHelper.getWritableDatabase();
//            connectionClass = new ConnectionClass();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
    }
    void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }


    public static void beginTransaction() {
        mDB.beginTransaction();
    }
    public static void endTransaction() {
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_BARCODES);
            db.execSQL(DB_CREATE_GOODS);
            db.execSQL(DB_CREATE_PLACEDGOODS);
            db.execSQL(DB_CREATE_CELLS);
            db.execSQL(DB_CREATE_ALL_GOODS);
            db.execSQL(DB_CREATE_PACK);
            Log.d(TAG, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade DB from " + oldVersion + " to " + newVersion);
            String dropPlacedGoods = "drop table if exists " + DB_TABLE_PLACEDGOODS;
            String dropGoods = "drop table if exists " + DB_TABLE_GOODS;
            String dropBarcodes = "drop table if exists " + DB_TABLE_BARCODES;
            String dropCells = "drop table if exists " + DB_TABLE_CELLS;
            String dropAllGoods = "drop table if exists " + DB_TABLE_ALL_GOODS;
            String dropPack = "drop table if exists " + DB_TABLE_PACK;
            if (oldVersion == 1 && newVersion == 2) db.execSQL(DB_CREATE_BARCODES);
            if (newVersion > 2) {
                db.execSQL(dropPlacedGoods);
                db.execSQL(dropGoods);
                db.execSQL(dropBarcodes);
                db.execSQL(dropCells);
                db.execSQL(dropAllGoods);
                db.execSQL(dropPack);
                db.execSQL(DB_CREATE_BARCODES);
                db.execSQL(DB_CREATE_GOODS);
                db.execSQL(DB_CREATE_PLACEDGOODS);
                db.execSQL(DB_CREATE_CELLS);
                db.execSQL(DB_CREATE_ALL_GOODS);
                db.execSQL(DB_CREATE_PACK);
            }
        }
    }
    static void clearCells() {
        mDB.delete(DB_TABLE_CELLS, null, null);
        FL.d(TAG,"Clear table cells");
    }
    public static long addCell(String cellId, String name, String descr, int type, int distance, String zonein, String zonein_descr) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CELL_ID, cellId);
        cv.put(COLUMN_CELL_NAME, name);
        cv.put(COLUMN_CELL_TYPE, type);
        cv.put(COLUMN_CELL_DESCR, descr);
        cv.put(COLUMN_CELL_DISTANCE, distance);
        cv.put(COLUMN_ZONEIN, zonein);
        cv.put(COLUMN_ZONEIN_DESCR, zonein_descr);
        try {
            ret = mDB.insert(DB_TABLE_CELLS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static Cell getCellByName(String name) {
        Cell ret = null;
        String table = DB_TABLE_CELLS;
        Cursor c = mDB.query(table, null, COLUMN_CELL_NAME + " like '" + name + "%'", null, null, null, null);
        if (c.moveToNext())
            ret = new Cell(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getInt(4),
                    c.getInt(5),
                    c.getString(6),
                    c.getString(7)
            );
        c.close();
        return ret;
    }
    public static Cell getCellById(String id) {
        Cell ret = null;
        String table = DB_TABLE_CELLS;
        if(id != null) {
            Cursor c = mDB.query(table, null, COLUMN_CELL_ID + " =?", new String[]{id}, null, null, null);
            if (c.moveToNext()) {
                ret = new Cell(
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getInt(4),
                        c.getInt(5),
                        c.getString(6),
                        c.getString(7)
                );
//                Log.d(TAG, "getCellByID() id " + id + " " + c.getString(3) + " " + c.getInt(5));
            }
            c.close();
        }
        return ret;
    }
    public static void clearData() {
        mDB.delete(DB_TABLE_BARCODES, null, null);
        mDB.delete(DB_TABLE_GOODS, null, null);
        mDB.delete(DB_TABLE_PACK, null, null);
        purgeSentData(false);
        FL.d(TAG,"Clear tables");
    }
    public static void clearAllGoods() {
        mDB.delete(DB_TABLE_ALL_GOODS, null, null);
        FL.d(TAG,"Clear all goods");
    }

    public static long addBarCode(String goodsCode, String barCode, int qnt) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_BARCODE, barCode);
        cv.put(COLUMN_QNT, qnt);
        try {
            ret = mDB.insert(DB_TABLE_BARCODES, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }

    public static long addGoods(String goodsCode, String desc, String cell, String article, int total, String brand, String cellId) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_GOODS_DESC, desc);
        cv.put(COLUMN_OUTPUT_CELL, cell);
        cv.put(COLUMN_GOODS_ARTICLE, article);
        cv.put(COLUMN_QNT, total);
        cv.put(COLUMN_GOODS_BRAND, brand);
        cv.put(COLUMN_BOX_ID, App.getCurrentBox().id);
        cv.put(COLUMN_OUTPUT_CELL_ID, cellId);
        try {
            ret = mDB.insert(DB_TABLE_GOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static GoodsRow getGoodsRow(String barcode) {
        GoodsRow ret = null;
        String goodsCode, tablePlaced = DB_TABLE_PLACEDGOODS;
        int qnt;
        String description, cell;
        Cursor c = mDB.query(DB_TABLE_BARCODES, null,COLUMN_BARCODE + "=?", new String[]{barcode},
                null, null, null, null );
        if (c.moveToFirst()) {
            goodsCode = c.getString(1);//
            Cursor cGoods = mDB.query(DB_TABLE_GOODS, null,COLUMN_GOODS_CODE + "=? and " + COLUMN_BOX_ID + "=?",
                    new String[]{goodsCode, App.getCurrentBox().id},null, null, null, null );
            if (cGoods.moveToFirst()) {
                description = cGoods.getString(2);
                cell = cGoods.getString(3);
                qnt = cGoods.getInt(5);
                Cursor c1 = mDB.query(tablePlaced, new String[] {"SUM(" + COLUMN_QNT + ")"},COLUMN_GOODS_CODE + "=? and " + COLUMN_BOX_ID + "=?",
                        new String[]{c.getString(1), App.getCurrentBox().id},null,null,null);
                int qPlaced = (c1.moveToNext())? c1.getInt(0) : 0;
                c1.close();
//                FL.d(TAG, "Found goods row code = " + goodsCode + " qnt " + qnt + " placed " + qPlaced);
                if (qnt > qPlaced) ret = new GoodsRow(cGoods.getString(1), qnt - qPlaced, cGoods.getString(2),
                        cGoods.getString(4), cGoods.getString(6), "", cGoods.getString(3));
//                Log.d(TAG, "Found goods row desc = " + description + " cell = " + cell);
            }
            cGoods.close();
        }
        c.close();
        return ret;
    }

    public static GoodsRow getGoodsRowById(String goodsCode) {
        GoodsRow ret = null;
        int total;
        String goodsTable = DB_TABLE_GOODS, tablePlaced = DB_TABLE_PLACEDGOODS;
        String cell;
        Cursor cGoods = mDB.query( goodsTable, null,COLUMN_GOODS_CODE + "=? and " + COLUMN_BOX_ID + "=?",
                new String[]{goodsCode, App.getCurrentBox().id},null, null, null, null );
        if (cGoods.moveToFirst()) {
//            description = cGoods.getString(2);
            cell = cGoods.getString(3);
            Cursor c1 = mDB.query(tablePlaced, new String[] {"SUM(" + COLUMN_QNT + ")"},COLUMN_GOODS_CODE + "=? and " + COLUMN_BOX_ID + "=?",
                    new String[]{cGoods.getString(1), App.getCurrentBox().id},null,null,null);
            int qPlaced = (c1.moveToNext())? c1.getInt(0) : 0;
            total = cGoods.getInt(5);
            c1.close();
            if(total > qPlaced) ret = new GoodsRow(cGoods.getString(1), total - qPlaced, cGoods.getString(2),
                    cGoods.getString(4), cGoods.getString(6), "", cGoods.getString(3));
//            FL.d(TAG, "Found goods row code = " + goodsCode + " total " + total + " placed " + qPlaced  + " cell = " + cell);
        }
        cGoods.close();
        return ret;
    }
    public static long addPlacedGoods(int storeman, String goodsId, String barcode, int qnt, String cell, String datetime) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN,storeman);
        cv.put(COLUMN_GOODS_CODE,goodsId);
        cv.put(COLUMN_BARCODE, barcode);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_INPUT_CELL, cell);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, datetime);
        cv.put(COLUMN_STORE, App.getStoreId());
        cv.put(COLUMN_SENT, 0);
        cv.put(COLUMN_BOX_ID, App.getCurrentBox().id);
        cv.put(COLUMN_EXCESSIVE, 0);
        try {
            ret = mDB.insert(DB_TABLE_PLACEDGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static long addPackGoods(int storeman, String goodsId, String barcode, int qnt, String cell, String datetime, String mdoc) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN,storeman);
        cv.put(COLUMN_GOODS_CODE,goodsId);
        cv.put(COLUMN_BARCODE, barcode);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_INPUT_CELL, cell);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, datetime);
        cv.put(COLUMN_STORE, App.getStoreId());
        cv.put(COLUMN_SENT, 0);
        cv.put(COLUMN_MDOC, mdoc);
        cv.put(COLUMN_EXCESSIVE, 0);
        try {
            ret = mDB.insert(DB_TABLE_PLACEDGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static long addExcessiveGoods(int storeman, String goodsId, String barcode, int qnt, String cell, String datetime,String lostGoodsCellId) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN, storeman);
        cv.put(COLUMN_GOODS_CODE, goodsId);
        cv.put(COLUMN_BARCODE, barcode);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_INPUT_CELL, cell);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, datetime);
        cv.put(COLUMN_STORE, App.getStoreId());
        cv.put(COLUMN_SENT, 0);
        cv.put(COLUMN_BOX_ID, lostGoodsCellId);
        cv.put(COLUMN_EXCESSIVE, 1);
        try {
            ret = mDB.insert(DB_TABLE_PLACEDGOODS, null, cv);
        } catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static long addExcessivePackGoods(int storeman, String goodsId, String barcode, int qnt, String cell, String datetime, String mdoc) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN, storeman);
        cv.put(COLUMN_GOODS_CODE, goodsId);
        cv.put(COLUMN_BARCODE, barcode);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_INPUT_CELL, cell);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, datetime);
        cv.put(COLUMN_STORE, App.getStoreId());
        cv.put(COLUMN_SENT, 0);
        cv.put(COLUMN_MDOC, mdoc);
        cv.put(COLUMN_EXCESSIVE, 1);
        try {
            ret = mDB.insert(DB_TABLE_PLACEDGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    static int addGoodsCount(){
        Cursor c = mDB.query(DB_TABLE_PLACEDGOODS, null, COLUMN_SENT + " =0", null, null, null, null);
        int ret = c.getCount();
        c.close();
        return ret;
    }
    static void beginTr() {
        mDB.beginTransaction();
    }
    static void endTr() {
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }
    static void clearGoods() {
        mDB.delete(DB_TABLE_PLACEDGOODS, null, null);
    }
    static int setGoodsSent(long rowId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SENT, 1);
        Log.d(TAG, "Row " + rowId + " sent");
        return mDB.update(DB_TABLE_PLACEDGOODS,cv,COLUMN_ID + " = ?", new String[]{String.valueOf(rowId)});
    }

    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
    public static void purgeSentData(boolean all) {
        String table = DB_TABLE_PLACEDGOODS;
        Cursor c = mDB.query(table, null,COLUMN_SENT + " =1", null, null, null,null);
        int count = c.getCount();
        if (count > 0) {
            while (c.moveToNext()) {
                long rowId = c.getLong(0);
                String boxId = c.getString(9);
                if(all || !App.getCurrentBox().id.equals(boxId)) {
                    mDB.delete(table, COLUMN_ID + " =?", new String[] {String.valueOf(rowId)});
                    FL.d(TAG, "Delete row " + rowId + " box id " + boxId);
                }
            }
        }
        c.close();
    }
    public static ArrayList<GoodsRow> getTaskList() {
        ArrayList<GoodsRow> ret = new ArrayList<>();
        Cursor c, c1;
        String tableTask = DB_TABLE_GOODS, tablePlaced = DB_TABLE_PLACEDGOODS;
        if(App.getCurrentBox() != null) {
            c = mDB.query(tableTask, null, COLUMN_BOX_ID + "=?", new String[]{App.getCurrentBox().id},
                    null, null, null);
            while (c.moveToNext()) {
                c1 = mDB.query(tablePlaced, new String[]{"SUM(" + COLUMN_QNT + ")"},
                        COLUMN_GOODS_CODE + "=? and " + COLUMN_BOX_ID + "=? and " + COLUMN_EXCESSIVE + "=0",
                        new String[]{c.getString(1), App.getCurrentBox().id}, null, null, null);
                int q = (c1.moveToNext()) ? c.getInt(5) - c1.getInt(0) : c.getInt(5);
                c1.close();
                if (q > 0) {
                    GoodsRow gr = new GoodsRow(c.getString(1), q, c.getString(2),
                            c.getString(4), c.getString(6), c.getString(8), c.getString(3));
                    ret.add(gr);
                }
            }
            c.close();
        }
//        Log.d(TAG,"getTaskList() size " + ret.size());
        return ret;
    }
    public static ArrayList<GoodsRow> getPlacedList() {
        ArrayList<GoodsRow> ret = new ArrayList<>();
        Cursor c;
        final String tablePlaced = DB_TABLE_PLACEDGOODS, tableCells = DB_TABLE_CELLS, tableGoods = DB_TABLE_GOODS;
        String sqlQuery = "select P." + COLUMN_GOODS_CODE
                + ",SUM(P." + COLUMN_QNT
                + "),P." + COLUMN_INPUT_CELL
                + ",C." + COLUMN_CELL_DESCR
                + ",G." + COLUMN_GOODS_DESC
                + ",G." + COLUMN_GOODS_ARTICLE
                + ",G." + COLUMN_GOODS_BRAND
                + " from " + tablePlaced +" as P"
                + " left join " + tableCells + " as C "
                + "on P." + COLUMN_INPUT_CELL + " = C." + COLUMN_CELL_ID
                + " left join " + tableGoods + " as G "
                + "on P." + COLUMN_GOODS_CODE + " = G." + COLUMN_GOODS_CODE
                + " where P." + COLUMN_BOX_ID + "=?"
                + " group by P." + COLUMN_GOODS_CODE + ",P." + COLUMN_INPUT_CELL;
        if(App.getCurrentBox() != null) {
            c = mDB.rawQuery(sqlQuery, new String[]{App.getCurrentBox().id});
            while (c.moveToNext()) {
//                Log.d(TAG, "Goods " + c.getString(4) + " brand " + c.getString(6) + " article " + c.getString(5) + " qnt " + c.getInt(1) + " cell " + c.getString(3));
                GoodsRow gr = new GoodsRow(c.getString(0), c.getInt(1), c.getString(4), c.getString(5), c.getString(6), c.getString(2), c.getString(3));
                ret.add(gr);
            }
            c.close();
        }
//        Log.d(TAG,"getPlacedList() size " + ret.size());
        return ret;
    }
    public static PrintLabelRequest goodsLabels(Cell cell) {
        PrintLabelRequest ret = null;
        GoodsLabel[] labels;
        Cursor c;
        String tableGM = DB_TABLE_GOODS;
        String sqlQuery = "select " + "GM." + COLUMN_GOODS_CODE + ",GM." + COLUMN_GOODS_DESC
                + ",GM." + COLUMN_GOODS_ARTICLE + ",GM." + COLUMN_QNT + ",GM." + COLUMN_OUTPUT_CELL
                + " from " + tableGM + " as GM";
//        Log.d(TAG,"Cell id " + cell.id + " descr " + cell.descr);
        c = mDB.rawQuery(sqlQuery, null );
        if (c.getCount() > 0) {
            int n = c.getCount();
            Log.d(TAG, "Found " + n +" goods in box " + cell.descr);
            labels = new GoodsLabel[n];
            for (int i = 0; i < n; i++) {
                c.moveToNext();
                GoodsLabel gl = new GoodsLabel(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                       "",
                        c.getInt(3),
                        App.getStoreMan(),
                        c.getString(4)
                );
                labels[i] = gl;
            }
            ret = new PrintLabelRequest(n,labels);
        }
        c.close();
        return ret;
    }
    public static int getBarcodeQnt(String barcode) {
        int ret = 0;
        String barcodeTable = DB_TABLE_BARCODES, goodsCode;
        Cursor c = mDB.query( barcodeTable, null,COLUMN_BARCODE + "=?", new String[]{barcode},
                null, null, null, null );
        if (c.moveToFirst()) {
            goodsCode = c.getString(1);
            ret = c.getInt(3);
//            Log.d(TAG, "Found goods position code = " + goodsCode + " qnt = " + ret);
        }
        if(ret == 0) ret++;
        return ret;
    }
    public static GoodsPosition[] searchGoods(String searchPattern) {
        GoodsPosition[] ret = null;
        int qnt, total;
        String goodsCode, description, cell, barcode, article;
        Cursor c = mDB.query( true, DB_TABLE_ALL_GOODS, new String[] {COLUMN_GOODS_CODE, COLUMN_GOODS_DESC, COLUMN_OUTPUT_CELL, COLUMN_GOODS_ARTICLE, COLUMN_QNT},
                COLUMN_GOODS_DESC + " like ? or " + COLUMN_GOODS_ARTICLE + " like ? COLLATE NOCASE",
                new String[] {"%" + searchPattern + "%","%" + searchPattern + "%"},null, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "Found " + count + " rows for " + searchPattern);
        if (count > 0) {
            ret = new GoodsPosition[count];
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                goodsCode = c.getString(0);
                description = c.getString(1);
                Cursor c1 = mDB.query(true, DB_TABLE_BARCODES,null,COLUMN_GOODS_CODE+" =?", new String[] {goodsCode},
                        null, null, null,"1");
                Log.d(TAG, "Barcode count = " + c1.getCount());
                barcode = "";
                qnt = 0;
                while (c1.moveToNext()) {
                    String id = c1.getString(1);
                    barcode = c1.getString(2);
                    qnt = c1.getInt(3);
//                    Log.d(TAG, "Id =" + id + " barcode =" + barcode + " qnt=" + qnt);
                }
                cell = c.getString(2);
                article = c.getString(3);
                total = c.getInt(4);
//                String cellOutId = (getCellByName(Config.getCellName(cell), App.getStoreId()) == null)? "" : getCellByName(Config.getCellName(cell), App.getStoreId()).id;
                GoodsPosition gp = new GoodsPosition(goodsCode, barcode, description, cell, qnt, article, total);
                ret[i] = gp;
                c.moveToNext();
                c1.close();
            }
            c.close();
        }
        return ret;
    }
    public static long addAllGoods(String goodsCode, String desc, String cell, String article, int total) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_GOODS_DESC, desc);
        cv.put(COLUMN_OUTPUT_CELL, cell);
        cv.put(COLUMN_GOODS_ARTICLE, article);
        cv.put(COLUMN_QNT, total);
        try {
            ret = mDB.insert(DB_TABLE_ALL_GOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static GoodsPosition searchGoodsById(String goodsId) {
        GoodsPosition ret = null;
        Cursor c = mDB.query(DB_TABLE_ALL_GOODS, new String[] {COLUMN_GOODS_CODE, COLUMN_GOODS_DESC, COLUMN_OUTPUT_CELL, COLUMN_GOODS_ARTICLE, COLUMN_QNT},
                COLUMN_GOODS_CODE + "=?", new String[] {goodsId}, null, null, null);
        if(c.moveToNext()) {
            ret = new GoodsPosition(c.getString(0),"", c.getString(1), c.getString(2),
                    0,c.getString(3),0);
        }
        return ret;
    }
    public static GoodsMove[] movesToUpload() {
        GoodsMove[] ret = null;
        Cursor c = mDB.query(DB_TABLE_PLACEDGOODS, null,
                COLUMN_SENT + " =0" , null, null, null, null);
        int count = c.getCount();
//        Log.d(TAG, "Found " + count + " excessive goods for upload");
        if (count > 0) {
            ret = new GoodsMove[count];
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                GoodsMove gm = new GoodsMove(c.getInt(1), c.getString(2), "", "",
                        c.getString(6), "", c.getString(9), c.getString(5));
                gm.qnt = c.getInt(4);
                gm.rowId = c.getLong(0);
                gm.dctNum = App.deviceUniqueIdentifier;
                ret[i] = gm;
                c.moveToNext();
            }
            c.close();
        }
        return ret;
    }
    public static int updateMoveGoods(int storeman, String goodsId,int qnt,String cellIn) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_QNT, qnt);
        return mDB.update(DB_TABLE_PLACEDGOODS, cv,COLUMN_STOREMAN + "=? and " + COLUMN_GOODS_CODE + "=? and " + COLUMN_INPUT_CELL + "=?",
                new String[] {String.valueOf(storeman), goodsId, cellIn});
    }

    public static int deleteMoveGoods(int storeman, String goodsId,String cellIn) {
        return mDB.delete(DB_TABLE_PLACEDGOODS, COLUMN_STOREMAN + "=? and " + COLUMN_GOODS_CODE + "=? and " + COLUMN_INPUT_CELL + "=?",
                new String[] {String.valueOf(storeman), goodsId, cellIn});
    }

    public static ArrayList<GoodsRow> goodsToUpload() {
        ArrayList<GoodsRow> ret = new ArrayList<>();
        Cursor c = mDB.query(DB_TABLE_PLACEDGOODS, null, COLUMN_SENT + " =0", null, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "Found " + count + " goods for upload");
        if (count > 0) {
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                String article = "", description = "";
                Cursor c1 = mDB.query(DB_TABLE_GOODS, new String[] {COLUMN_GOODS_DESC, COLUMN_GOODS_ARTICLE}, COLUMN_GOODS_CODE + "=?",
                        new String[] {c.getString(2)}, null, null, null);
                if(c1.moveToNext()) {
                    description = c1.getString(0);
                    article = c1.getString(1);
                }
                c1.close();
                GoodsRow gr = new GoodsRow(c.getString(2), c.getInt(5), description, article,"", "", c.getString(4));
                ret.add(gr);
                c.moveToNext();
            }
            c.close();
        }
        return ret;
    }
    public static long addPack(String goodsCode, String desc, String mdoc, String article, int qnt, int packQnt, String brand, String cellId) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_GOODS_DESC, desc);
        cv.put(COLUMN_MDOC, mdoc);
        cv.put(COLUMN_GOODS_ARTICLE, article);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_PACK_QNT, packQnt);
        cv.put(COLUMN_GOODS_BRAND, brand);
        cv.put(COLUMN_CELL_ID, cellId);
        try {
            ret = mDB.insert(DB_TABLE_PACK, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getMessage());
        }
        return ret;
    }
    public static ArrayList<GoodsRow> getPackList() {
        ArrayList<GoodsRow> ret = new ArrayList<>();
        Cursor c, c1, c2;
        PackDetail packDetail;
        PackDetail[] packDetails;
        final String tablePack = DB_TABLE_PACK;
        String rawQuery = "select distinct P." + COLUMN_GOODS_CODE +",P." + COLUMN_GOODS_DESC + ",P." +COLUMN_GOODS_ARTICLE + ",P." + COLUMN_GOODS_BRAND +
                ",P." + COLUMN_CELL_ID +",C." + COLUMN_CELL_DESCR + " from " + tablePack + " as P left join " +
                DB_TABLE_CELLS + " as C on P." + COLUMN_CELL_ID + "=C." + COLUMN_CELL_ID;
        /*
        c = mDB.query(tablePack, null, null, null,
                null, null, null);
 ",P." + COLUMN_QNT + ",P." + COLUMN_PACK_QNT +
         */
        c = mDB.rawQuery(rawQuery, null);
        Log.d(TAG, tablePack + " size " + c.getCount());
        while (c.moveToNext()) {
            int qnt = 0;
            c2 = mDB.query(tablePack, new String[] {COLUMN_ID, COLUMN_QNT, COLUMN_PACK_QNT, COLUMN_MDOC}, COLUMN_GOODS_CODE + "=?",
                    new String[] {c.getString(0)}, null, null, null);
            if(c2.getCount() > 0) {
                packDetails = new PackDetail[c2.getCount()];
                int i = 0;
                while (c2.moveToNext()) {
                    packDetail = new PackDetail();
                    packDetail.id = c2.getInt(0);
                    packDetail.qnt = c2.getInt(1);
                    packDetail.packQnt = c2.getInt(2);
                    packDetail.mdoc = c2.getString(3);
                    packDetail.goods_code = c.getString(0);
                    packDetails[i++] = packDetail;
                    qnt += Math.min(c2.getInt(1), c.getInt(2));
                }
            } else {
                packDetails = null;
            }
            c2.close();
            c1 = mDB.query(DB_TABLE_PLACEDGOODS, new String[]{"SUM(" + COLUMN_QNT + ")"},
                    COLUMN_GOODS_CODE + "=? and " + COLUMN_EXCESSIVE + "=0",
                    new String[]{c.getString(4)}, null, null, null);

            int q = (c1.moveToNext()) ? qnt - c1.getInt(0) : qnt;
            c1.close();
            if (q > 0) {
                GoodsRow gr = new GoodsRow(c.getString(0), q, c.getString(1),
                        c.getString(2), c.getString(3), c.getString(4), c.getString(5));
                gr.packDetails = packDetails;
                ret.add(gr);
            }
        }
        c.close();
        Log.d(TAG,"getPackList() size " + ret.size());
        return ret;
    }
    public static GoodsRow getPackRowById(String goodsCode) {
        GoodsRow ret = null;
        Cursor c;
        final String tablePack = DB_TABLE_PACK, tablePlaced = DB_TABLE_PLACEDGOODS;
        String rawQuery = "select P." + COLUMN_GOODS_CODE +",P." + COLUMN_GOODS_DESC + ",P." + COLUMN_GOODS_ARTICLE + ",P." + COLUMN_GOODS_BRAND +
                ",P." + COLUMN_MDOC +",PL." + COLUMN_MDOC + ", case when P." + COLUMN_QNT + ">P." + COLUMN_PACK_QNT + " then P." + COLUMN_PACK_QNT +
                " else P." + COLUMN_QNT + " end -sum(ifnull(PL." + COLUMN_QNT + ",0)) from " + tablePack + " as P left join " +
                tablePlaced + " as PL on P." + COLUMN_GOODS_CODE + "=PL." + COLUMN_GOODS_CODE + " and P." + COLUMN_MDOC + "=PL." + COLUMN_MDOC +
                " group by P." + COLUMN_MDOC +
                " having P." + COLUMN_GOODS_CODE + "=? and case when P." + COLUMN_QNT + ">P." + COLUMN_PACK_QNT + " then P." + COLUMN_PACK_QNT +
                " else P." + COLUMN_QNT +  " end -sum(ifnull(PL." + COLUMN_QNT + ",0)) > 0";
        c = mDB.rawQuery(rawQuery,new String[]{goodsCode});
        while (c.moveToNext()) {
            Log.d(TAG, c.getString(1) + " " + c.getString(2) + " " + c.getString(4) + " " + c.getString(5) +
                    " " + c.getInt(6));
        }
        return ret;
    }
    public static GoodsRow getPackRow(String barcode) {
        GoodsRow ret = null;
        String goodsCode;
        Cursor c = mDB.query(DB_TABLE_BARCODES, null,COLUMN_BARCODE + "=?", new String[]{barcode},
                null, null, null, null );
        if (c.moveToFirst()) {
            goodsCode = c.getString(1);
            Log.d(TAG, "Found " + goodsCode + " for " + barcode);
            ret = getPackRowById(goodsCode);
        } else {
            Log.d(TAG, "No code found");
        }
        return ret;
    }
}
