package full.movie.tubem.player;

import android.content.Context;
import android.preference.PreferenceManager;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Localization {
    private Localization() {
    }

    public static Locale getPreferredLocale(Context context) {
        String languageCode = PreferenceManager.getDefaultSharedPreferences(context).getString(String.valueOf(R.string.search_language_key), context.getString(R.string.default_language_value));
        if (languageCode.length() == 2) {
            return new Locale(languageCode);
        }
        if (!languageCode.contains("_")) {
            return Locale.getDefault();
        }
        return new Locale(languageCode.substring(0, 2), languageCode.substring(languageCode.indexOf("_"), languageCode.length()));
    }

    public static String localizeViewCount(long viewCount, Context context) {
        Locale locale = getPreferredLocale(context);
        return String.format(context.getResources().getString(R.string.view_count_text), new Object[]{NumberFormat.getInstance(locale).format(viewCount)});
    }

    public static String localizeNumber(long number, Context context) {
        return NumberFormat.getInstance(getPreferredLocale(context)).format(number);
    }

    private static String formatDate(String date, Context context) {
        Locale locale = getPreferredLocale(context);
        Date datum = null;
        try {
            datum = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DateFormat.getDateInstance(2, locale).format(datum);
    }

    public static String localizeDate(String date, Context context) {
        return String.format(context.getResources().getString(R.string.upload_date_text), new Object[]{formatDate(date, context)});
    }
}
