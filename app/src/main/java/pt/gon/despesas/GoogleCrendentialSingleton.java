package pt.gon.despesas;

import android.accounts.Account;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class GoogleCrendentialSingleton {

    private static GoogleCrendentialSingleton instance;


    GoogleSignInClient mGoogleSignInClient;
    GoogleAccountCredential mGoogleAccountCredential;
    Account account;

    public static GoogleCrendentialSingleton getInstance()
    {
        // Return the instance
        if (instance == null)
        {
            // Create the instance
            instance = new GoogleCrendentialSingleton();
        }
        return instance;
    }

    private GoogleCrendentialSingleton()
    {
        // Constructor hidden because this is a singleton
    }

}
