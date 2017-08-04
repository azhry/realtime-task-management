package com.example.acer.plnwunderlist;

import android.provider.ContactsContract;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ryan Fadholi on 04/08/2017.
 * <p>
 * Container class for conversion/formatting/etc. that's used in more than 1 activity.
 */

public class AppHelper {

    public static Map<String, String> validateJSONMap(Map<String, String> map) {
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            if (pairs.getValue() == null) {
                map.put(pairs.getKey(), "");
            }
        }
        return map;
    }

    public static String formatDate(Calendar c, boolean isVerbose) {

        /*
        * Verbose means regardless of time gap, the date will be displayed as is.
        * Whereas in non-verbose, when appropriate the date will be simplified.
        *
        * For example, if current date is 24 July and due date is 25 July
        * Verbose: Due Mon, 25-07-2017
        * !Verbose: Due Tomorrow
        */

        Date dueDate = c.getTime();
        Date today = null;

        String dayOfWeek = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        SimpleDateFormat dateString = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateOnlyFormatter = new SimpleDateFormat("yyyy-MM-dd");


        if (!isVerbose) {
            //Get ONLY today's date, without the timestamp.
            try {
                today = dateOnlyFormatter.parse(dateOnlyFormatter.format(new Date()));
            } catch (ParseException e) {
                Log.e("TODAY FORMAT", "Error when trying to get current date: " + e);
            }

            long timeDiff = dueDate.getTime() - today.getTime();
            int daysDiff = (int) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

            if (daysDiff == 0) {
                return "today";
            } else if (daysDiff == 1) {
                return "tomorrow";
            } else if (daysDiff < 7) {
                return "in a week";
            }

        }

        return dayOfWeek + ", " + dateString.format(c.getTime());
    }

    public static String formatDate(Date d, boolean isVerbose) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        return formatDate(cal, isVerbose);
    }
}
