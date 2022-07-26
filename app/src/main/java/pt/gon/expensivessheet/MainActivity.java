package pt.gon.expensivessheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.adapter.SpreadSheetAdapter;

public class MainActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "MainActivity";
    private List<SpreadSheet> spreadSheetList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SpreadSheetAdapter mAdapter;
    MainActivity activity;

    final String oauthIdProd = "373460637778-ab200008rq1dc10mdjlnfmp6q150l98t.apps.googleusercontent.com";


    boolean floatExpanded = false;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fabExpand = findViewById(R.id.fabExpand);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);
        FloatingActionButton fab = findViewById(R.id.fab);
        fabExpand.setOnClickListener(view -> {
            if (!floatExpanded) {
                floatExpanded = true;
                fabSearch.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.purple_500)));
            } else {
                floatExpanded = false;
                fabSearch.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorAccent)));
            }
        });
        fab.setOnClickListener(view -> {
            floatExpanded = false;
            fabSearch.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorAccent)));
            createNewSheet();
        });
        fabSearch.setOnClickListener(view -> {
            floatExpanded = false;
            fabSearch.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorAccent)));
            fetchSheetsFromDrive();
        });

        recyclerView = findViewById(R.id.recycler_spreedSheet);

        mAdapter = new SpreadSheetAdapter(activity, spreadSheetList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        initDependencies();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            boolean success = handleSignInResult(task);
            if (success) {
                loadSpreadSheatsList();
            } else {
                Toast.makeText(getApplicationContext(), "Ocorreu um erro a Obter as credenciais de acesso!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    private boolean handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleCrendentialSingleton.getInstance().account = account;
            GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential.setSelectedAccount(account.getAccount());
            GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential.setSelectedAccountName(account.getAccount().name);
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
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(oauthIdProd)
                        .requestScopes(new Scope(SheetsScopes.SPREADSHEETS_READONLY))
                        .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
                        .requestScopes(new Scope(SheetsScopes.DRIVE_READONLY))
                        .requestScopes(new Scope(SheetsScopes.DRIVE_FILE))
                        .build();

        GoogleCrendentialSingleton.getInstance().mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential = GoogleAccountCredential
                .usingOAuth2(this, Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE_READONLY, SheetsScopes.DRIVE_FILE))
                .setBackOff(new ExponentialBackOff());

        if (GoogleCrendentialSingleton.getInstance().account == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog;
            builder.setView(R.layout.help);
            builder.setTitle(R.string.dialog_welcome_title);
            builder.setPositiveButton(R.string.dialog_welcome_next, (dialog12, which) -> {
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

    public void loadSpreadSheatsList() {

        spreadSheetList = Preferences.loadSpreadSheatsList(this);
        mAdapter.notifyDataSetChanged();
        mAdapter = new SpreadSheetAdapter(activity, spreadSheetList);
        recyclerView.setAdapter(mAdapter);
    }

    public void createNewSheet() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        builder.setView(R.layout.add_spreadsheet);
        builder.setTitle(R.string.dialog_new_sheet_title);
        builder.setPositiveButton(R.string.add_button, (dialog12, which) -> {
            Dialog dialog2 = Dialog.class.cast(dialog12);
            EditText name = dialog2.findViewById(R.id.input_ss_name);

            createNewSheetFromTemplate(name.getText().toString());
        });
        builder.setNegativeButton(R.string.close_button, (dialog1, which) -> dialog1.dismiss());

        dialog = builder.create();
        dialog.show();
    }


    public void createNewSheetFromTemplate(String name) {

        AtomicBoolean error = new AtomicBoolean(false);
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.dialog_create_sheet_title));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        try {

            new Thread(() -> {
                // do background stuff here
                Drive driveService = new Drive.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                        .setApplicationName(getString(R.string.app_name))
                        .build();


                // Upload file photo.jpg on drive.
                File fileMetadata = new File();
                fileMetadata.setName(name.contains("espesas") ? name : "Despesas_" + name);
                File file = null;
                try {

                    file = driveService.files().copy("11e2kQPOZzim96wphESu3LaKAAoucuv6k5S_f3dQiJms", fileMetadata)
                            .setFields("id,name")
                            .execute();

                    Preferences.saveSpreadSheatsList(activity, file.getName(), file.getId());


                } catch (GoogleJsonResponseException e) {
                    // TODO(developer) - handle error appropriately
                    System.err.println("Unable to upload file: " + e.getDetails());
                    error.set(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    error.set(true);
                }

                if (!error.get() && file != null) {
                    try {
                        Sheets service = new Sheets.Builder(new NetHttpTransport(),
                                GsonFactory.getDefaultInstance(),
                                GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                                .setApplicationName(getString(R.string.app_name))
                                .build();
                        List<Object> cat = new ArrayList<>();
                        cat.add(GoogleCrendentialSingleton.getInstance().getAccount().getDisplayName());

                        ValueRange insert = new ValueRange();
                        insert.setValues(Arrays.asList(cat));
                        insert.setRange(getString(R.string.sheet_tab_persons));

                        service.spreadsheets().values().append(file.getId(), getString(R.string.sheet_tab_persons), insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(() -> {
                    progress.dismiss();
                    if (error.get()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.global_error),
                                Toast.LENGTH_LONG).show();
                    }
                    loadSpreadSheatsList();
                    // OnPostExecute stuff here
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchSheetsFromDrive() {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.dialog_loading));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        try {

            new Thread(() -> {
                boolean error = false;
                // do background stuff here
                Drive driveService = new Drive.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                        .setApplicationName(getString(R.string.app_name))
                        .build();

                List<File> files = new ArrayList<>();
                try {
                    Drive.Files.List request = driveService.files().list()
                            //.setPageSize(100)
                            // Available Query parameters here:
                            //https://developers.google.com/drive/v3/web/search-parameters
                            .setQ("mimeType = 'application/vnd.google-apps.spreadsheet' and (name contains 'Despesas' or name contains 'Expensives')   and trashed = false")
                            .setFields("nextPageToken, files(id, name)");

                    FileList result = request.execute();

                    files = result.getFiles();

                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }

                List<File> finalFiles = files;
                boolean finalError = error;
                runOnUiThread(() -> {
                    progress.dismiss();
                    if (finalError) {
                        Toast.makeText(getApplicationContext(), getString(R.string.global_error),
                                Toast.LENGTH_LONG).show();
                    } else {
                        chooseSheetFromDrive(finalFiles);
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseSheetFromDrive(final List<File> files) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(R.string.dialog_import_sheet_title);

        List<String> accountName = new ArrayList<>();
        for (File a :
                files) {
            accountName.add(a.getName());
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, accountName);

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {

            dialog.dismiss();
            validateFile(files.get(which));

        });

        builderSingle.show();
    }

    public void validateFile(File file) {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        try {

            new Thread(() -> {
                boolean error = false;
                // do background stuff here
                Sheets service = new Sheets.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                        .setApplicationName(getString(R.string.app_name))
                        .build();

                try {

                    List<List<Object>> versionTab = service.spreadsheets().values().get(file.getId(), "Version!A1:A2").execute().getValues();
                    String name = versionTab.get(0).get(0).toString();
                    String version = versionTab.get(1).get(0).toString();
                    if (!name.equals("expensivessheet")) {
                        error = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }

                boolean finalError = error;
                runOnUiThread(() -> {

                    progress.dismiss();
                    if (finalError) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                        builderSingle.setTitle("Folha de cálculo não compatível!!");

                        AlertDialog dialog;
                        builderSingle.setNeutralButton("Fechar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        dialog = builderSingle.create();
                        dialog.show();
                    } else {
                        Preferences.saveSpreadSheatsList(activity, file.getName(), file.getId());
                        loadSpreadSheatsList();
                    }
                    // OnPostExecute stuff here
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            progress.dismiss();
            Toast.makeText(getApplicationContext(), getString(R.string.global_error),
                    Toast.LENGTH_LONG).show();
        }
    }
}