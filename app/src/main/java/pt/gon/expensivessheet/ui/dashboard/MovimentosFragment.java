package pt.gon.expensivessheet.ui.dashboard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import pt.gon.expensivessheet.GoogleCrendentialSingleton;
import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.databinding.FragmentMovimentosBinding;
import pt.gon.expensivessheet.ws.model.Movimento;

public class MovimentosFragment extends Fragment {

    private FragmentMovimentosBinding binding;

    private List<Movimento> movimentoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MovimentosAdapter mAdapter;

    String id;
    Activity activity;
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMovimentosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = getActivity();

        id = getActivity().getIntent().getExtras().getString("id");

        FloatingActionButton fab = (FloatingActionButton) binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMovimento();
            }
        });

        recyclerView = binding.recyclerSpreedSheet;

        mAdapter = new MovimentosAdapter( this, movimentoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMovimentosList();
    }

    public void loadMovimentosList(){

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        try {

            new Thread(() -> {
                // do background stuff here
                Sheets service = new Sheets.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                        .setApplicationName("Sheets samples")
                        .build();

                List<List<Object>> movimentos = null;
                try {

                    movimentos = service.spreadsheets().values().get(id, "Movimentos!A:E").execute().getValues();
                    movimentoList.clear();
                    for (List<Object> movimento: movimentos) {
                        Movimento m = new Movimento();
                        m.setData(movimento.get(0).toString());
                        m.setDescricao(movimento.get(1).toString());
                        m.setValor(movimento.get(2).toString());
                        m.setTipo(movimento.get(3).toString());
                        m.setPessoa(movimento.get(4).toString());

                        movimentoList.add(m);
                    }
                    movimentoList.remove(0);
                    Collections.reverse(movimentoList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(()->{
                    mAdapter = new MovimentosAdapter(this, movimentoList);
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

    public void deleteRow(Integer StartIndex, Integer EndIndex) {
        Spreadsheet spreadsheet = null;

        Sheets service = new Sheets.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                .setApplicationName("Sheets samples")
                .build();
        try {
            spreadsheet = service.spreadsheets().get(id).execute();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        Request request = new Request();
        DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
        DimensionRange dimensionRange = new DimensionRange();
        dimensionRange.setDimension("ROWS");
        dimensionRange.setStartIndex(StartIndex);
        dimensionRange.setEndIndex(EndIndex);

        Sheet sh = null;
        for(Sheet s : spreadsheet.getSheets()){
            if(s.getProperties().getTitle().equals("Movimentos")){
                sh = s;
                break;
            }
        }
        dimensionRange.setSheetId(sh.getProperties().getSheetId());
        deleteDimensionRequest.setRange(dimensionRange);

        request.setDeleteDimension(deleteDimensionRequest);

        List<Request> requests = new ArrayList<Request>();
        requests.add(request);
        content.setRequests(requests);

        try {
            service.spreadsheets().batchUpdate(id, content).execute();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dimensionRange = null;
            deleteDimensionRequest = null;
            request = null;
            requests = null;
            content = null;
        }
    }

    public void deleteMovimentoList(int index){

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();


        new Thread(() -> {
           int indexFinal = movimentoList.size() - index;

            deleteRow(indexFinal, indexFinal+1);
            getActivity().runOnUiThread(()->{
                progress.dismiss();
                loadMovimentosList();
            });
        }).start();

    }

    public void addMovimento(){
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("A Carregar");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();



        new Thread(() -> {
            // do background stuff here
            Sheets service = new Sheets.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                    .setApplicationName("Sheets samples")
                    .build();



            final List<String> pessoas = new ArrayList<>();
            final List<String> tipos = new ArrayList<>();
            try {

                List<List<Object>> categoriasDrive = service.spreadsheets().values().get(id, "Categorias!A:A").execute().getValues();

                for (List<Object> c: categoriasDrive) {
                    for( Object t: c) {
                        tipos.add(t.toString());
                    }
                }

                List<List<Object>> pessoasDrive = service.spreadsheets().values().get(id, "Pessoas!A:A").execute().getValues();

                for (List<Object> c: pessoasDrive) {
                    for( Object t: c) {
                        pessoas.add(t.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            getActivity().runOnUiThread(()->{
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
                        Calendar calendar = new GregorianCalendar( year, month, dayOfMonth );
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
                        Double valorDouble = Double.parseDouble(valor.getText().toString().trim().isEmpty() ? "0": valor.getText().toString().replace(".",","));
                        movimento.add(valorDouble);
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
                            getActivity().runOnUiThread(()->{
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

    @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
    }
}