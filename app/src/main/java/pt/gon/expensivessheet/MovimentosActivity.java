package pt.gon.expensivessheet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import pt.gon.expensivessheet.adapter.MovimentosAdapter;
import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.ws.model.Categoria;
import pt.gon.expensivessheet.ws.model.Movimento;

public class MovimentosActivity extends AppCompatActivity {

    private List<Movimento> movimentoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MovimentosAdapter mAdapter;
    MovimentosActivity activity;

    static View addViewMovimento;
  //  String id = "1w6E8-hPQJcAOrtiCVJPx1fVp-dOH2aW25twURTCpOzU";
    String id = "";
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimentos);
        activity = this;

        id = getIntent().getExtras().getString("id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMovimento();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_spreedSheet);

        mAdapter = new MovimentosAdapter(activity, movimentoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refreshMovimentos) {
            loadMovimentosList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadMovimentosList(){

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        try {

            new Thread(() -> {
                // do background stuff here
                Sheets service = new Sheets.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                        .setApplicationName("Sheets samples")
                        .build();

                List<List<Object>> movimentos = null;
                try {

                    movimentos = service.spreadsheets().values().get(id, "Movimentos!A:G").execute().getValues();
                    movimentoList.clear();
                    for (List<Object> movimento: movimentos) {
                        Movimento m = new Movimento();
                        m.setData(movimento.get(0).toString());
                        m.setDescricao(movimento.get(1).toString());
                        m.setValor(movimento.get(2).toString());
                        m.setTipo(movimento.get(3).toString());
                        m.setPessoa(movimento.get(4).toString());
                        m.setMes(movimento.get(5).toString());
                        m.setAno(movimento.get(6).toString());

                        movimentoList.add(m);
                    }
                    movimentoList.remove(0);
                    Collections.reverse(movimentoList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(()->{
                    mAdapter = new MovimentosAdapter(activity, movimentoList);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    progress.dismiss();
                    // OnPostExecute stuff here
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMovimentoList(int index){

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        index = (movimentoList.size()+1) - index;


        BatchClearValuesRequest content = new BatchClearValuesRequest();
        content.setRanges(Arrays.asList("Movimentos!"+index+":"+index));



        new Thread(() -> {
            try {
                Sheets service = new Sheets.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                        .setApplicationName("Sheets samples")
                        .build();

                service.spreadsheets().values().batchClear(id, content).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(()->{
                progress.dismiss();
                loadMovimentosList();
            });
        }).start();

    }

    public void addMovimento(){
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();



        new Thread(() -> {
            // do background stuff here
            Sheets service = new Sheets.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    GoogleCrendentialSingleton.getInstance().mGoogleAccountCredential)
                    .setApplicationName("Sheets samples")
                    .build();

            List<List<Object>> categorias = null;
            List<Categoria> result = new ArrayList<>();
            try {

                categorias = service.spreadsheets().values().get(id, "Categorias!A:C").execute().getValues();

                for (List<Object> c: categorias) {
                    Categoria m = new Categoria();
                    m.setTipo(c.get(0).toString());
                    if(c.size() > 1)
                    m.setAno(c.get(1).toString());
                    if(c.size() > 2)
                    m.setPessoa(c.get(2).toString());

                    result.add(m);
                }
                result.remove(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final List<String> pessoas = new ArrayList<>();
            final List<String> tipos = new ArrayList<>();

            for(Categoria c : result){
                if(c.getPessoa() != null && !c.getPessoa().isEmpty()) {
                    pessoas.add(c.getPessoa());
                }
                if(c.getTipo() != null && !c.getTipo().isEmpty()) {
                    tipos.add(c.getTipo());
                }
            }
            runOnUiThread(()->{
                progress.dismiss();
                ArrayAdapter adapterTipos = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, tipos);
                ArrayAdapter adapterPessoas = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, pessoas);

                final View addViewMovimento = activity.getLayoutInflater().inflate(R.layout.add_movimento, null);
                final AppCompatSpinner tipo = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_tipo);
                tipo.setAdapter(adapterTipos);

                final AppCompatSpinner pessoa = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_pessoa);
                pessoa.setAdapter(adapterPessoas);


                final EditText valor = addViewMovimento.findViewById(R.id.input_ss_value);
                final EditText descricao = addViewMovimento.findViewById(R.id.input_ss_descricao);

                final TextView date = addViewMovimento.findViewById(R.id.input_ss_date);
                date.setText(Preferences.convertFromSimpleDate(new Date()));

                final CalendarView simpleCalendarView = addViewMovimento.findViewById(R.id.calendarView); // get the reference of CalendarView
                simpleCalendarView.setDate((new Date()).getTime());
                simpleCalendarView.setOnDateChangeListener( new CalendarView.OnDateChangeListener() {
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        Calendar  calendar = new GregorianCalendar( year, month, dayOfMonth );
                        date.setText(Preferences.convertFromSimpleDate(calendar.getTime()));
                    }
                });

                final ViewGroup frm = addViewMovimento.findViewById(R.id.frm);
                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(frm.getVisibility() == View.GONE)
                            frm.setVisibility(View.VISIBLE);
                        else
                            frm.setVisibility(View.GONE);
                    }
                });

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog;
                builder.setView(addViewMovimento);
                builder.setTitle("Adicionar Movimento");
                builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progress.show();
                        Calendar now = Calendar.getInstance();
                        Calendar selected = Calendar.getInstance();
                        selected.setTime(Preferences.convertToSimpleDate(date.getText().toString()));

                        Date dateToSend = selected.getTime();
                        if(now.get(Calendar.DAY_OF_MONTH) == selected.get(Calendar.DAY_OF_MONTH) && now.get(Calendar.MONTH) == selected.get(Calendar.MONTH) && now.get(Calendar.YEAR) == selected.get(Calendar.YEAR)){
                            dateToSend = new Date();
                        }

                        List<Object> movimento = new ArrayList<>();
                        movimento.add(date.getText().toString());
                        movimento.add(descricao.getText().toString());
                        movimento.add(valor.getText().toString().trim().isEmpty() ? "0": valor.getText().toString().replace(".",","));
                        movimento.add(tipo.getItemAtPosition(tipo.getSelectedItemPosition()).toString());
                        movimento.add(pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString());
                        movimento.add(""+selected.get(Calendar.MONTH +1));
                        movimento.add(""+selected.get(Calendar.YEAR));


                        ValueRange insert = new ValueRange();
                        insert.setValues(Arrays.asList(movimento));
                        insert.setRange("Movimentos!A:G");

                        new Thread(() -> {
                        try {
                            service.spreadsheets().values().append(id, "Movimentos!A:G", insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                            runOnUiThread(()->{
                                progress.dismiss();
                                loadMovimentosList();
                            });
                        }).start();
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
                // OnPostExecute stuff here
            });
        }).start();

    }
}
