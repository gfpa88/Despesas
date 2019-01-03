package pt.gon.despesas;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import pt.gon.despesas.adapter.MovimentosAdapter;
import pt.gon.despesas.ws.ApiCallBack;
import pt.gon.despesas.ws.RetrofitClient;
import pt.gon.despesas.ws.model.Movimento;
import pt.gon.despesas.ws.model.Movimentos;

public class MovimentosActivity extends AppCompatActivity {

    private List<Movimento> movimentoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MovimentosAdapter mAdapter;
    MovimentosActivity activity;
    String id = "1w6E8-hPQJcAOrtiCVJPx1fVp-dOH2aW25twURTCpOzU";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimentos);
        activity = this;

       // id = getIntent().getExtras().getString("id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

}
