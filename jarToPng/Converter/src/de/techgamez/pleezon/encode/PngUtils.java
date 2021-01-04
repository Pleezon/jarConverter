package de.techgamez.pleezon.encode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Base64;

public class PngUtils {


    public static byte[] dec(BufferedImage bi){
        StringBuilder ret = new StringBuilder();
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
                int a = c.getAlpha();
                if(a==0)return decode(ret.toString());
                ret.append((char)r).append((char)g).append((char)b);
            }
        }

        return decode(ret.toString());
    }


    private static byte[] decode(String s){
        return Base64.getDecoder().decode(s.trim());
    }

}