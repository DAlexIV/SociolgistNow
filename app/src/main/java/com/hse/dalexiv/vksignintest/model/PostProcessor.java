package com.hse.dalexiv.vksignintest.model;

/**
 * Created by dalex on 11/3/2015.
 */
public class PostProcessor {
    public static String findTime(String text) throws NullPointerException {
        if (text == null)
            throw new NullPointerException("Text is null");
        for (int i = 0; i < text.length(); ++i)
            if (text.charAt(i) == ':') {
                if (i + 2 >= text.length())
                    continue;
                if (i - 2 < 0)
                    continue;
                try {
                    int p = Integer.parseInt(text.substring(i - 1, i));
                    int f = Integer.parseInt(text.substring(i + 1, i + 2));
                    int s = Integer.parseInt(text.substring(i + 2, i + 3));

                    try {
                        int pp = Integer.parseInt(text.substring(i - 2, i - 1));
                        return String.valueOf(pp) + String.valueOf(p) + ":" + String.valueOf(f) + String.valueOf(s);
                    } catch (NumberFormatException e) {
                        return String.valueOf(p) + ":" + String.valueOf(f) + String.valueOf(s);
                    }
                } catch (NumberFormatException e) {
                    continue; // Going further if is not a number
                }
            }
        return null;
    }
}
