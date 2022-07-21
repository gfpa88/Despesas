package pt.gon.despesas;

import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gon.despesas.adapter.Preferences;
import pt.gon.despesas.adapter.SpreadSheetAdapter;
import pt.gon.despesas.ws.ApiCallBack;
import pt.gon.despesas.ws.RetrofitClient;

public class MainActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "MainActivity" ;
    private List<SpreadSheet> spreadSheetList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SpreadSheetAdapter mAdapter;
    MainActivity activity;
    GoogleSignInClient mGoogleSignInClient;
    GoogleAccountCredential mGoogleAccountCredential;

    String oauthIdDev = "713935152996-3r52ti5ft0t8m7c1foc6enthiflgm22b.apps.googleusercontent.com";

    String oauthIdProd = "713935152996-benuarkk1c14ivvnv68uvfk98v6c1rli.apps.googleusercontent.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDependencies();
        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               addSpreadSheet();
            }
        });

        recyclerView = findViewById(R.id.recycler_spreedSheet);

        mAdapter = new SpreadSheetAdapter(activity, spreadSheetList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        Account[] accounts = GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential.getAllAccounts();


        if(accounts.length== 0){
            Intent signInIntent = GoogleCrendentialSingleton.getInstance().mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else {
            if(Preferences.loadAccount(activity) == null){
                chooseAccount(accounts);
            }else{
                setAccount();
                loadSpreadSheatsList();
            }
        }
    }

    private void setAccount() {
        List<Account> accountsList = Arrays.asList(GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential.getAllAccounts());
        for (Account a :
                accountsList) {
            if(a.name.equals(Preferences.loadAccount(activity))){
                GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential.setSelectedAccount(a);
                GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential.setSelectedAccountName(a.name);
                GoogleCrendentialSingleton.getInstance().account = a;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);


            Account[] accounts = mGoogleAccountCredential.getAllAccounts();
            chooseAccount(accounts);
            // Create the sheets API client
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i(TAG, "sign in"+ account.toString());
            // Signed in successfully, show authenticated UI.
         //   updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG,"handleSignInResult" ,e);
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
          //  updateUI(null);
        }
    }


    void initDependencies() {
        GoogleSignInOptions signInOptions =
               new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                       .requestEmail()
                       .requestIdToken(oauthIdProd)
                       .requestScopes(new Scope(SheetsScopes.SPREADSHEETS_READONLY))
                       .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
                        .build();

        GoogleCrendentialSingleton.getInstance().mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential = GoogleAccountCredential
                .usingOAuth2(this, Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY))
                .setBackOff(new ExponentialBackOff());

    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadNewVersion();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    public void loadSpreadSheatsList(){

        spreadSheetList = Preferences.loadSpreadSheatsList(this);
        mAdapter.notifyDataSetChanged();
        mAdapter = new SpreadSheetAdapter(activity, spreadSheetList);
        recyclerView.setAdapter(mAdapter);
    }

    public void chooseAccount(Account[] accounts) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Qual a conta a usar?");

        List<Account> accountsList = Arrays.asList(accounts);
        List<String> accountName = new ArrayList<>();
        for (Account a :
                accountsList) {
            accountName.add(a.name);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, accountName);

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);

                Preferences.saveAccount(activity,strName);

                setAccount();
                loadSpreadSheatsList();
                dialog.dismiss();
            }
        });

        builderSingle.show();
    }

    public void addSpreadSheet(){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        builder.setView(R.layout.add_spreadsheet);
        builder.setTitle("Adicionar Folha");
        builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialog dialog2 = Dialog.class.cast(dialog);
                EditText id = dialog2.findViewById(R.id.input_ss_id);
                EditText name = dialog2.findViewById(R.id.input_ss_name);
                Preferences.saveSpreadSheatsList(activity,name.getText().toString(),id.getText().toString());
                dialog.dismiss();
                loadSpreadSheatsList();
            }
        });
        builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void downloadNewVersion(){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        RetrofitClient.getInstance().getVersion(new ApiCallBack() {
            @Override
            public void onSuccess(Object value) {
                progress.dismiss();

                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;
                    String v = ((String) value).trim().replace("\n","");
                    final String newVersion = value!= null ? v : null;
                    if(newVersion != null && !version.equals(newVersion)){
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        AlertDialog dialog;
                        builder.setMessage("Deseja fazer download da nova versão?");
                        builder.setTitle("Atualização");
                        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                              //  DownloadManager d = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                                String url = ("https://github.com/gfpa88/Despesas/raw/master/apk/despesas_"+newVersion+".apk");
                              //  DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                               // req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                               // d.enqueue(req);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                        });
                        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        dialog = builder.create();
                        dialog.show();
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                progress.dismiss();
            }
        });
    }
}
