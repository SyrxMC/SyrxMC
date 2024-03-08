package br.com.syrxmc.bot.utils;

public class Utils {

    public static String convertToShortScale(long number) {
        if (number < 0) {
            return "-" + convertToShortScale(-number);
        }
        if (number < 1_000) {
            return Long.toString(number);
        }
        if (number < 1_000_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        if (number < 1_000_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        }
        if (number < 1_000_000_000_000L) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        }
        return String.format("%.1fT", number / 1_000_000_000_000.0);
    }
}
