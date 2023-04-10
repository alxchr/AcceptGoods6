package ru.abch.acceptgoods6;

public class Config {
    static String TAG = "Config";

    public static final String ip = "tomcat2.comtt.ru";
    public static final int port = 8443;
    public static final String scheme = "https";
/*
    public static final String ip = "192.168.21.244";
    public static final int port = 8080;
    public static final String scheme = "http";
    public static final String goodsPath = "/accept5/goods/";
*/
    public static final String labelsPath = "/goodscollection/labeltest/ZD410";
    public static final String goodsPath = "/accept5/box/";
    public static final String allGoodsPath = "/move3/goods/";
    public static final String postGoodsPath = "/accept5/goods/";
    public static final String barcodesPath = "/move3/barcodes/";
    public static final String cellsPath = "/accept5/cells/";
    public static final String dumpPath = "/accept5/dump/";
    public static final String dumpExcessivePath = "/move3/dump/";
    public static final String postExcessivePath = "/move3/goods/";
    public static final String warehousesPath = "/move3/warehouses2/";
    public static final String packPath = "/accept5/pack/";
    public static final long timeShift = 946666800000L;
    public static final boolean tts = true;
    public static long toComttTime(long t) {
        return (t - timeShift)/1000;
    }
    public static long toJavaTime(long t) {
        return t*1000 + timeShift;
    }
    public static final long weekInMillis = 7*24*3600*1000;
    public static String formatCell(String c) {
        String ret ="";
        int i = 0;
        if (Integer.parseInt(c.substring(2,6)) == 0) {
            String s = c.substring(6,12);
            while (s.substring(0, 1).equals("0") && s.length() > 0 ) {
                s = s.substring(1);
            }
            i = 0;
            while (!s.substring(i, i+1).equals("0") && s.length() > 0) {
                ret += s.substring(i, i + 1);
                i++;
            }
            if (s.substring(i+1, i+2).equals("0")) {
                ret += s.substring(i, i+1);
                i++;
            }
            ret +="-";
            ret += Integer.parseInt(s.substring(++i));
        } else {
            ret = Integer.parseInt(c.substring(4, 6)) + "-" + Integer.parseInt(c.substring(6, 9));
        }
        return ret;
    }
    public static int getQty(String goods) {
        return Integer.parseInt(goods.substring(2,4));
    }
    public static final int maxDataCount = 30;
    public static final int offlineTimeout = 10 * 60 * 1000;    // 10 min
    public static String getCellName(String code) {
        String result;
        int left, right, separator;
        if (code.startsWith("19000")) {
            result = code.substring(5, 12);
            while (result.length() > 2 && result.startsWith("0")) result = result.substring(1);
            if(result.length() > 2) {
                separator = result.indexOf("00");
//                Log.d(TAG, "Result 00 " + result);
                if (separator > 0 && separator < result.length() - 2) {
                    left = Integer.parseInt(result.substring(0,separator + 1));
                    right = Integer.parseInt(result.substring(separator + 2));
                    result = String.format("%02d",left) + String.format("%03d",right);
//                    Log.d(TAG, "separator " + separator + " left " + left + " right " + right + " result " + result);
                } else {
                    separator = result.indexOf('0');
//                    Log.d(TAG, "Result 0 " + result);
                    if (separator < result.length() - 1 && separator > 0) {
                        left = Integer.parseInt(result.substring(0, separator));
                        right = Integer.parseInt(result.substring(separator + 1));
                        result = String.format("%02d", left) + String.format("%03d", right);
//                        Log.d(TAG, "separator " + separator + " left " + left + " right " + right + " result " + result);
                    } else {
                        result = null;
                    }
                }
            } else {
                result = null;
            }
        } else {
            result = code.substring(4, 9);
        }
        return result;
    }
}
