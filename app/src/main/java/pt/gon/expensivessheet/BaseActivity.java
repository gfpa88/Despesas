package pt.gon.expensivessheet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;

import pt.gon.expensivessheet.adapter.Preferences;

public abstract class BaseActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "BaseActivity";
    BaseActivity activity;

    final String oauthIdProd = "373460637778-ab200008rq1dc10mdjlnfmp6q150l98t.apps.googleusercontent.com";

    public ProgressDialog progress;


    abstract void start();
    abstract void loginResult();


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progress.dismiss();
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            boolean success = false;
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                success = handleSignInResult(task);
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            if (success) {
                if (!Preferences.helpEditDelete(this)) {
                    progress.dismiss();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    AlertDialog dialog;
                    builder.setView(R.layout.help_item_options);
                    builder.setTitle(R.string.dialog_welcome_title);
                    builder.setNeutralButton(R.string.close_button, (dialog1, which) -> {
                        Preferences.saveHelpEditDelete(this, true);
                        // finish();
                    });

                    dialog = builder.create();
                    dialog.show();
                }
                loginResult();
            } else {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                builderSingle.setTitle(R.string.error_getting_credentials);

                AlertDialog dialog;
                builderSingle.setPositiveButton(R.string.dialog_retry_button, (dialogInterface, i) ->
                        initDependencies());
                builderSingle.setNeutralButton(R.string.close_button, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                });
                dialog = builderSingle.create();
                dialog.show();
            }
        }
    }


    private boolean handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleCrendentialSingleton.getInstance().setAccount(account);
            GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential().setSelectedAccount(account.getAccount());

            Preferences.saveAccount(this, account.getAccount().name);
            Log.i(TAG, "sign in" + account.toString());
            return true;
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "handleSignInResult", e);
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
        }
        return false;
    }


    void initDependencies() {

        progress.setTitle(getString(R.string.dialog_loading));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(oauthIdProd)
                        .requestEmail()
                        .requestScopes(new Scope(SheetsScopes.SPREADSHEETS_READONLY))
                        .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
                        .requestScopes(new Scope(SheetsScopes.DRIVE_READONLY))
                        .requestScopes(new Scope(SheetsScopes.DRIVE_FILE))
                        .build();

        GoogleCrendentialSingleton.getInstance().setmGoogleSignInClient(GoogleSignIn.getClient(this, signInOptions));
        GoogleCrendentialSingleton.getInstance().setmGoogleAccountCredential(GoogleAccountCredential
                .usingOAuth2(this, Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE_READONLY, SheetsScopes.DRIVE_FILE))
                .setBackOff(new ExponentialBackOff()));

        if (Preferences.loadAccount(this) == null && GoogleCrendentialSingleton.getInstance().account == null) {
            progress.dismiss();

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog;
            builder.setView(R.layout.help);
            builder.setTitle(R.string.dialog_welcome_title);
            builder.setPositiveButton(R.string.dialog_welcome_next, (dialog12, which) -> {
                progress.show();
                Intent signInIntent = GoogleCrendentialSingleton.getInstance().mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
            builder.setNegativeButton(R.string.close_button, (dialog1, which) -> finish());

            dialog = builder.create();
            dialog.show();
        } else {
                Intent signInIntent = GoogleCrendentialSingleton.getInstance().mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
        }


    }

}