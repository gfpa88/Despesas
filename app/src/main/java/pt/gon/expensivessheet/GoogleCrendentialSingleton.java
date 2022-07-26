package pt.gon.expensivessheet;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import pt.gon.expensivessheet.ws.RetrofitClient;

public class GoogleCrendentialSingleton {

    private static GoogleCrendentialSingleton instance;

    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;
    GoogleAccountCredential mGoogleAccountCredential;

    public synchronized static GoogleCrendentialSingleton getInstance() {
        if(instance == null) {
            synchronized (GoogleCrendentialSingleton.class ){
                if (instance == null){
                    instance = new GoogleCrendentialSingleton();
                }
            }
        }
        return instance;
    }

    private GoogleCrendentialSingleton()
    {
        // Constructor hidden because this is a singleton
    }

    public GoogleSignInAccount getAccount() {
        return account;
    }

    public GoogleSignInClient getmGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public GoogleAccountCredential getmGoogleAccountCredential() {
        return mGoogleAccountCredential;
    }

    public void setAccount(GoogleSignInAccount account) {
        this.account = account;
    }

    public void setmGoogleSignInClient(GoogleSignInClient mGoogleSignInClient) {
        this.mGoogleSignInClient = mGoogleSignInClient;
    }

    public void setmGoogleAccountCredential(GoogleAccountCredential mGoogleAccountCredential) {
        this.mGoogleAccountCredential = mGoogleAccountCredential;
    }
}
