package pt.gon.despesas.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.gon.despesas.SpreadSheet;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {

    public static List<SpreadSheet> loadSpreadSheatsList(Activity context){
        List<SpreadSheet> result = new ArrayList<>();
        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Set<String> list = prefs.getStringSet("spreadSheets", new HashSet<String>());
        for(String l : list){
            Gson gson = new Gson();
            result.add(gson.fromJson(l,SpreadSheet.class));
        }
        return result;
    }

    public static void removeSpreadSheet(Activity context, String id, String name){
        Set<String> set = new HashSet<>();
        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Set<String> list = prefs.getStringSet("spreadSheets", new HashSet<String>());
        for(String l : list){
            SpreadSheet s = new Gson().fromJson(l,SpreadSheet.class);
            if(!(s.getId().equals(id) && s.getName().equals(name))){
                set.add(l);
            }
        }
        cleanSpreadSheatsList(context);
        SharedPreferences.Editor scoreEditor = prefs.edit();
        scoreEditor.putStringSet("spreadSheets", set);
        scoreEditor.commit();
    }

    public static void saveSpreadSheatsList(Activity context, String name, String id){
        SpreadSheet ss = new SpreadSheet(id,name);

        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Set<String> set = new HashSet<>();
        set.addAll(prefs.getStringSet("spreadSheets", new HashSet<String>()));
        set.add(new Gson().toJson(ss));
        SharedPreferences.Editor scoreEditor = prefs.edit();
        scoreEditor.putStringSet("spreadSheets", set);
        scoreEditor.commit();
    }

    public static void saveAccount(Activity context, String name){

        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor scoreEditor = prefs.edit();
        scoreEditor.putString("account", name);
        scoreEditor.commit();
    }

    public static String loadAccount(Activity context){
        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        return prefs.getString("account",null);
    }

    public static void cleanSpreadSheatsList(Activity context){
        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Set<String> set = new HashSet<>();
        SharedPreferences.Editor scoreEditor = prefs.edit();
        scoreEditor.putStringSet("spreadSheets", set);
        scoreEditor.commit();
    }

    public static Date convertToDate(String dtStart){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return format.parse(dtStart);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertFromDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertFromSimpleDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date convertToSimpleDate(String dtStart){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(dtStart);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
