package pt.gon.expensivessheet.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class Utils {
    public static String getLocaleStringResource(Locale requestedLocale, int resourceId, Context context) {
        String result;

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(requestedLocale);
        result = context.createConfigurationContext(config).getText(resourceId).toString();

        return result;
    }

    public static Locale getLocale(String version){
        if(version.equals("pt")){
            return Locale.forLanguageTag("pt");
        }else{
           return Locale.ENGLISH;
        }
    }
    public static Double convertTransactionValue(String valor) {
        try{
            return Double.parseDouble(valor);
        }catch (Exception e){
            try{
                return Double.parseDouble(valor.replace(",","."));
            }catch (Exception e1){
                try{
                    return Double.parseDouble(valor.replace(".",","));
                }catch (Exception e2){
                }
            }
        }
        return null;
    }
}
