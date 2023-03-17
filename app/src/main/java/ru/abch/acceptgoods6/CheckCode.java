package ru.abch.acceptgoods6;

public class CheckCode {
    /*
    private static String scanPattern = "UI%*\w+(CTR|LLR|SS4|SPR|SSR|LKT)";
    private static String shortNumber = "\\d{4}";
    private static String tinyNumber ="\\d-\\d+";

     */
    private static String cellNumber = "19\\w+";
    private static String goodsCode = "28\\w+";
    private static String cell = "\\d+\\.\\d+";
    private static String goodsCode39 = "G\\.*\\w+\\.*\\w+";
    /*
    public static boolean checkCode(String code){
        return code.matches(scanPattern);
    }
    public static boolean checkShortNumber(String code){
        return code.matches(shortNumber);
    }
    public static boolean checkTinyNumber(String code){
        return code.matches(tinyNumber);
    }

     */
    public static boolean checkCell(String code){
        return code.matches(cellNumber) && code.length() == 13;
    }
    public static boolean checkGoods(String code){
        return code.matches(goodsCode);
    }
    public static boolean checkCellStr(String code){
        return code.matches(cell);
    }
    public static boolean checkGoods39(String code){
        return code.matches(goodsCode39);
    }
}
