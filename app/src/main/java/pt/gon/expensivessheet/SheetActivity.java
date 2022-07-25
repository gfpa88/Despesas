package pt.gon.expensivessheet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import java.util.concurrent.atomic.AtomicBoolean;

import pt.gon.expensivessheet.databinding.ActivitySheetBinding;

public class SheetActivity extends AppCompatActivity {

private ActivitySheetBinding binding;

    String id;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivitySheetBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());


        id = getIntent().getExtras().getString("id");
        name = getIntent().getExtras().getString("name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_movimentos,R.id.navigation_home, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_sheet);

     //   NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(binding.navView, navController);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sheet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.shareSheet) {
            shareSheet();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public void shareSheet(){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        builder.setView(R.layout.share_spreadsheet);
        builder.setTitle("Partilhar folha");
        builder.setPositiveButton("Partilhar", (dialog12, which) -> {
            Dialog dialog2 = Dialog.class.cast(dialog12);
            EditText name = dialog2.findViewById(R.id.input_ss_name);

            shareSheetViaDrive(name.getText().toString());
        });
        builder.setNegativeButton("Fechar", (dialog1, which) -> dialog1.dismiss());

        dialog = builder.create();
        dialog.show();
    }

    public void shareSheetViaDrive(String email){

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A partilhar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        AtomicBoolean error = new AtomicBoolean(false);
        try {

            new Thread(() -> {
                // do background stuff here
                Drive service = new Drive.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                        .setApplicationName("ExpensivesSheet")
                        .build();

                Permission userPermission = new Permission()
                        .setType("user")
                        .setRole("writer");

                userPermission.setEmailAddress(email);
                try {
                    service.permissions().create(id, userPermission)
                            .setFields("id").execute();

                }catch (Exception e) {
                    // TODO(developer) - handle error appropriately
                    e.printStackTrace();
                    error.set(true);
                }

                runOnUiThread(()->{
                    progress.dismiss();
                    if(error.get()){
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro a enviar o convite!",
                                Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Convite enviado!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}