package pt.gon.despesas;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import pt.gon.despesas.adapter.MovimentosAdapter;
import pt.gon.despesas.adapter.Preferences;
import pt.gon.despesas.ws.ApiCallBack;
import pt.gon.despesas.ws.RetrofitClient;
import pt.gon.despesas.ws.model.Categoria;
import pt.gon.despesas.ws.model.Categorias;
import pt.gon.despesas.ws.model.Movimento;
import pt.gon.despesas.ws.model.Movimentos;

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

        RetrofitClient.getInstance().getMovimentos(id, new ApiCallBack() {
            @Override
            public void onSuccess(@NonNull Object value) {
                movimentoList = ((Movimentos) value).getMovimentos();
                Collections.reverse(movimentoList);
                mAdapter = new MovimentosAdapter(activity, movimentoList);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                progress.dismiss();
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                progress.dismiss();
            }
        });
    }

    public void deleteMovimentoList(int index){

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        index = (movimentoList.size()-1) - index;
        RetrofitClient.getInstance().deleteMovimento(id, index, new ApiCallBack() {
            @Override
            public void onSuccess(@NonNull Object value) {
                progress.dismiss();
                loadMovimentosList();
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                progress.dismiss();
                loadMovimentosList();
            }
        });
    }

    public void addMovimento(){
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        RetrofitClient.getInstance().getCategorias(id, new ApiCallBack() {
            @Override
            public void onSuccess(@NonNull Object value) {
                progress.dismiss();

                Categorias result = (Categorias)value;

                final List<String> pessoas = new ArrayList<>();
                final List<String> tipos = new ArrayList<>();

                for(Categoria c : result.getCategorias()){
                    if(c.getPessoa() != null && !c.getPessoa().isEmpty()) {
                        pessoas.add(c.getPessoa());
                    }
                    if(c.getTipo() != null && !c.getTipo().isEmpty()) {
                        tipos.add(c.getTipo());
                    }
                }

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
                        RetrofitClient.getInstance().inserirMovimento(id, descricao.getText().toString(),valor.getText().toString().trim().isEmpty() ? "0": valor.getText().toString(), tipo.getItemAtPosition(tipo.getSelectedItemPosition()).toString(),pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString(), dateToSend, new ApiCallBack() {
                            @Override
                            public void onSuccess(@NonNull Object value) {
                                progress.dismiss();
                                loadMovimentosList();
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                progress.dismiss();
                                loadMovimentosList();
                            }
                        });
                        dialog.dismiss();
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

            @Override
            public void onError(@NonNull Throwable throwable) {
                progress.dismiss();
                loadMovimentosList();
            }
        });

    }
}
