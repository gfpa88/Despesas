package pt.gon.despesas;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
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
  //  String id = "1w6E8-hPQJcAOrtiCVJPx1fVp-dOH2aW25twURTCpOzU";
    String id = "";
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

        loadMovimentosList();

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

                View view = activity.getLayoutInflater().inflate(R.layout.add_movimento, null);
                final AppCompatSpinner tipo = (AppCompatSpinner) view.findViewById(R.id.input_ss_tipo);
                tipo.setAdapter(adapterTipos);

                final AppCompatSpinner pessoa = (AppCompatSpinner) view.findViewById(R.id.input_ss_pessoa);
                pessoa.setAdapter(adapterPessoas);


                final EditText valor = view.findViewById(R.id.input_ss_value);
                final EditText descricao = view.findViewById(R.id.input_ss_descricao);

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog;
                builder.setView(view);
                builder.setTitle("Adicionar Movimento");
                builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progress.show();
                        RetrofitClient.getInstance().inserirMovimento(id, descricao.getText().toString(),valor.getText().toString(), tipo.getItemAtPosition(tipo.getSelectedItemPosition()).toString(),pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString(), new ApiCallBack() {
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
