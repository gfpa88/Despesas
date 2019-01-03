package pt.gon.despesas;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import pt.gon.despesas.adapter.Preferences;
import pt.gon.despesas.adapter.SpreadSheetAdapter;

public class MainActivity extends AppCompatActivity {


    private List<SpreadSheet> spreadSheetList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SpreadSheetAdapter mAdapter;
    MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        loadSpreadSheatsList();
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
// 3. Get the AlertDialog from create()
        dialog.show();
    }
}
