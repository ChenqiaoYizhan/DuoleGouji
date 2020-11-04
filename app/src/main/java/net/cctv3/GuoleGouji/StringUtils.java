package net.cctv3.GuoleGouji;

import java.util.ArrayList;

public class StringUtils {
    public static ArrayList<String> assetsNames() {
        ArrayList<String> list = new ArrayList<>();
        String cardNames[] = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "Da", "Xiao"};
        for (String cardName : cardNames) {
            list.add("Card" + "_" + cardName + ".png");
        }
        list.add("Screenshot.png");
        return list;
    }
}
