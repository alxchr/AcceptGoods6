package ru.abch.acceptgoods6;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.pm.PackageInfoCompat;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class App extends Application {
    static App instance = null;
    public static final String appName = "UniversalAcceptGoods";
    static String TAG = "App";
    static SharedPreferences sp;
    private static int storeMan;
    private static String dctNum;
    private static final String storeIndexKey = "store_index", storeIdKey = "store_id", storeNameKey = "store_name", storeManKey = "store_man", dctNumKey = "dctnum";
//    GregorianCalendar calendar;
    public static Database db;
    public static String deviceUniqueIdentifier;
    public static String packageName;
    public static final int START = 0, SELECTGOODS = 1, PUTGOODS = 2, MAINCYCLE = 3, SELECTEXCESSIVEGOODS = 4, MAX_RETRY = 3, WAIT_SOURCE = 5;
    public static int state = 0;
    public static int currentDistance = 0;
    public static String currentBoxName, currentBoxId;
    private static final String boxNameKey = "box_name", boxIdKey = "box_id";
    /*
    public static String[] ids = new String[] {"     2   ", "     JCTR", "    10SSR", "    12SPR", "    2MSPR", "    1BSPR", "    1ISPR",
            "    1LSPR", "    1OSPR", "    1PSPR", "    1CSPR", "    1SSPR", "    1USPR", "    15SPR", "    1TSPR", "    28SPR",
            "    27SPR", "    2BSPR", "    2FSPR", "    2GSPR", "    2DSPR", "    2HSPR", "    2ISPR", "    2JSPR"};

     */
//    public static String[] storeCode = new String[] {"1908","1909","1907","1901","1920","1902","1906","1904","1903","1905","1900","1900",
//            "1900","1910","1900","1911","1912","1913","1914","1915","1916","1917","1918","1919"};
    public static long versionCode = 0;
//    public static HashMap<String, String> storeMap;
//    public static HashMap<String, String> codeMap;
    public static Warehouse2[] warehouses;
    public static Warehouse2 warehouse = null;
    private static final String storeKey = "store";
    private static boolean packMode;
    private static final String packModeKey = "pack_mode", packIdKey = "pack_id", packNumKey = "pack_num";
    private static String packId, packNum;
    @Override
    public void onCreate() {
        super.onCreate();
        packageName = this.getPackageName();
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(packageName, 0);
            versionCode = PackageInfoCompat.getLongVersionCode(pInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
        deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        instance = this;
        sp = getSharedPreferences();
//        storeIndex = sp.getInt(storeIndexKey, -1);
//        storeId = sp.getString(storeIdKey,"    12SPR");
//        storeName = sp.getString(storeNameKey,"");
        storeMan = sp.getInt(storeManKey, 0);
        dctNum = sp.getString(dctNumKey,"");
        currentBoxName = sp.getString(boxNameKey,"");
        currentBoxId = sp.getString(boxIdKey,"");
        String storeJSON = sp.getString(storeKey, "");
        if(!storeJSON.isEmpty()) {
            warehouse = getWarehouse(storeJSON);
            Log.d(TAG,"Start application store id =" + warehouse.id + " store " + warehouse.descr + " current box name " + currentBoxName + " id " + currentBoxId);
        }
        packMode = sp.getBoolean(packModeKey, false);
        packId = sp.getString(packIdKey,"");
        packNum = sp.getString(packNumKey,"");
        db = new Database(this);
        db.open();
    }
    public static SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        return sharedPrefs;
    }
    public static App get() {
        return instance;
    }
//    public static int getStoreIndex() {return storeIndex;};
    public static String getStoreId() {
        String ret = (warehouse == null)? "    12SPR" : warehouse.id;
        Log.d(TAG, "Get store id =" + ret);
        return ret;
    }
    public static String getStoreName(){return warehouse.descr;}
    public static int getStoreMan() { return storeMan;}
    /*
    public static void setStoreIndex(int i) {
        storeIndex = i;
        sp.edit().putInt(storeIndexKey, i).apply();
    }


    public static void setStoreId(String id) {
        storeId = id;
        Log.d(TAG, "Set store id =" + id);
        sp.edit().putString(storeIdKey, id).apply();
    }
    public static void setStoreName(String name) {
        storeName = name;
        sp.edit().putString(storeNameKey, name).apply();
    }

     */
    public static void setStoreMan(int sm) {
        storeMan = sm;
        sp.edit().putInt(storeManKey, sm).apply();
    }
    public static void setDctNum(String dct) {
        dctNum = dct.substring(0,4);
        sp.edit().putString(dctNumKey,dctNum).apply();
    }
    public static String getDctNum() {
        return deviceUniqueIdentifier;
    }
    public static Cell getCurrentBox() {
        Cell ret = null;
        if(currentBoxName.length() > 0) {
            ret = Database.getCellByName(currentBoxName);
        }
        return ret;
    }
    public static void setCurrentBox(Cell cell) {
        if(cell == null) {
            currentBoxName = "";
            currentBoxId = "";
            Log.d(TAG, "Set null box");
        } else {
            currentBoxName = cell.name;
            currentBoxId = cell.id;
        }
        sp.edit().putString(boxNameKey,currentBoxName).commit();
        sp.edit().putString(boxIdKey,currentBoxId).commit();
    }
    public static App getInstance() {
        return instance;
    }
    /*
    public static String getStoreCode() {
        return storeCode[storeIndex];
    }

     */
    public static Warehouse2 getWarehouse(String storeJSON) {
        Warehouse2 ret;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ret  = gson.fromJson(storeJSON, Warehouse2.class);
        return ret;
    }
    public static void setWarehouse(Warehouse2 wh) {
        Gson gson = new Gson();
        String storeJSON = gson.toJson(wh);
        Log.d(TAG, "Store " + storeJSON);
        sp.edit().putString(storeKey,storeJSON).commit();
        warehouse = wh;
    }
    public static boolean getPackMode() {
        return packMode;
    }
    public static void setPackMode(boolean mode) {
        sp.edit().putBoolean(packModeKey, mode).commit();
        packMode = mode;
    }
    public static String getCurrentPackId() {
        return packId;
    }
    public static void setCurrentPackId(String id) {
        sp.edit().putString(packIdKey, id).commit();
        packId = id;
    }
    public static String getCurrentPackNum() {
        return packNum;
    }
    public static void setCurrentPackNum(String num) {
        sp.edit().putString(packNumKey, num).commit();
        packNum = num;
    }
}
