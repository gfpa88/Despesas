package pt.gon.expensivessheet.ui.movimentos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
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
import java.util.concurrent.atomic.AtomicBoolean;

import pt.gon.expensivessheet.GoogleCrendentialSingleton;
import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.databinding.FragmentMovimentosBinding;
import pt.gon.expensivessheet.utils.Utils;
import pt.gon.expensivessheet.ws.model.Movimento;

public class MovimentosFragment extends Fragment {

    private FragmentMovimentosBinding binding;

    private List<Movimento> movimentoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MovimentosAdapter mAdapter;

    String id;
    Activity activity;
    String lang;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMovimentosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = getActivity();

        id = getActivity().getIntent().getExtras().getString("id");

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> addMovimento());

        recyclerView = binding.recyclerSpreedSheet;

        mAdapter = new MovimentosAdapter(this, movimentoList);
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

    public void getLang(String fileId, Sheets service) throws IOException {
        List<List<Object>> versionTab = service.spreadsheets().values().get(fileId, "Version!A1:A3").execute().getValues();
        lang = versionTab.get(2).get(0).toString();
    }

    public void loadMovimentosList() {

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        try {

            new Thread(() -> {
                boolean error = false;
                // do background stuff here
                Sheets service = new Sheets.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                        .setApplicationName(getString(R.string.app_name))
                        .build();
                List<List<Object>> movimentos = null;
                try {

                    getLang(id,service);
                    movimentos = service.spreadsheets().values().get(id, getLangString(R.string.sheet_tab_expensive_load)).execute().getValues();
                    movimentoList.clear();
                    for (List<Object> movimento : movimentos) {
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
                    error = true;
                }

                boolean finalError = error;
                getActivity().runOnUiThread(() -> {
                    if(finalError){
                        Toast.makeText(getContext(), getString(R.string.global_error),
                                Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }else {
                        mAdapter = new MovimentosAdapter(this, movimentoList);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                    progress.dismiss();
                    // OnPostExecute stuff here
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            progress.dismiss();
            Toast.makeText(getContext(), getString(R.string.global_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean deleteRow(Integer StartIndex, Integer EndIndex) {
        Spreadsheet spreadsheet = null;

        Sheets service = new Sheets.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                .setApplicationName(getString(R.string.app_name))
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
        for (Sheet s : spreadsheet.getSheets()) {
            if (s.getProperties().getTitle().equals(getLangString(R.string.sheet_tab_entry_name))) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dimensionRange = null;
            deleteDimensionRequest = null;
            request = null;
            requests = null;
            content = null;
        }
        return true;
    }

    public void deleteMovimentoList(int index) {

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();


        new Thread(() -> {
            int indexFinal = movimentoList.size() - index;

           boolean success = deleteRow(indexFinal, indexFinal + 1);
            getActivity().runOnUiThread(() -> {
                progress.dismiss();
                if(success) {
                    loadMovimentosList();
                }else{

                }
            });
        }).start();

    }

    public void addMovimento() {
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new Thread(() -> {
            boolean error = false;
            // do background stuff here
            Sheets service = new Sheets.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                    .setApplicationName(getString(R.string.app_name))
                    .build();


            final List<String> pessoas = new ArrayList<>();
            final List<String> tipos = new ArrayList<>();
            try {

                List<List<Object>> categoriasDrive = service.spreadsheets().values().get(id, getLangString(R.string.sheet_tab_category)).execute().getValues();

                for (List<Object> c : categoriasDrive) {
                    for (Object t : c) {
                        tipos.add(t.toString());
                    }
                }

                List<List<Object>> pessoasDrive = service.spreadsheets().values().get(id, getLangString(R.string.sheet_tab_persons)).execute().getValues();

                for (List<Object> c : pessoasDrive) {
                    for (Object t : c) {
                        pessoas.add(t.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }

            AtomicBoolean finalError = new AtomicBoolean(error);
            getActivity().runOnUiThread(() -> {
                progress.dismiss();
                if(finalError.get()){
                    Toast.makeText(getContext(), getString(R.string.global_error),
                            Toast.LENGTH_LONG).show();
                }else {
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

                    date.setOnClickListener(v -> {

                        LayoutInflater inflater = (LayoutInflater)activity.getApplicationContext().getSystemService
                                (Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout calendar_layout= (LinearLayout)inflater.inflate(R.layout.calendar_layout, null, false);

                        final CalendarView simpleCalendarView = calendar_layout.findViewById(R.id.calendarView); // get the reference of CalendarView
                        simpleCalendarView.setDate((new Date()).getTime());
                        simpleCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                            Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
                            date.setText(Preferences.convertFromSimpleDate(calendar.getTime()));
                        });

                        new AlertDialog.Builder(activity)
                                .setView(calendar_layout)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //do nothing...yet
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // Do nothing.
                                            }
                                        }
                                ).show();


                    });

                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    AlertDialog dialog;
                    builder.setView(addViewMovimento);
                    builder.setTitle(R.string.dialog_add_entry_title);
                    builder.setPositiveButton(R.string.add_button, (dialog1, which) -> {
                        progress.show();
                        Calendar selected = Calendar.getInstance();
                        selected.setTime(Preferences.convertToSimpleDate(date.getText().toString()));

                        List<Object> movimento = new ArrayList<>();
                        movimento.add(date.getText().toString());
                        movimento.add(descricao.getText().toString());
                        Double valorDouble = Double.parseDouble(valor.getText().toString());
                        movimento.add(valorDouble);
                        movimento.add(tipo.getItemAtPosition(tipo.getSelectedItemPosition()).toString());
                        movimento.add(pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString());
                        movimento.add(selected.get(Calendar.MONTH)+1);
                        movimento.add(selected.get(Calendar.YEAR));

                        ValueRange insert = new ValueRange();
                        insert.setValues(Arrays.asList(movimento));
                        insert.setRange(getLangString(R.string.sheet_tab_expensive_insert));

                        new Thread(() -> {
                            try {
                                service.spreadsheets().values().append(id, getLangString(R.string.sheet_tab_expensive_insert), insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                                finalError.set(true);
                            }
                            getActivity().runOnUiThread(() -> {
                                progress.dismiss();
                                if(finalError.get()){
                                    Toast.makeText(getContext(), getString(R.string.global_error),
                                            Toast.LENGTH_LONG).show();
                                }else {
                                    loadMovimentosList();
                                }
                            });
                        }).start();
                    });
                    builder.setNegativeButton(R.string.close_button, (dialog12, which) -> dialog12.dismiss());

                    dialog = builder.create();
                    dialog.show();
                }
            });
        }).start();

    }

    @NonNull
    private String getLangString(int string_id) {
        return Utils.getLocaleStringResource(Utils.getLocale(lang),string_id,getContext());
    }


    public void editMovimento(int index, Movimento movimento) {
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new Thread(() -> {
            boolean error = false;
            // do background stuff here
            Sheets service = new Sheets.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                    .setApplicationName(getString(R.string.app_name))
                    .build();


            final List<String> pessoas = new ArrayList<>();
            final List<String> tipos = new ArrayList<>();
            try {

                List<List<Object>> categoriasDrive = service.spreadsheets().values().get(id, getLangString(R.string.sheet_tab_category)).execute().getValues();

                for (List<Object> c : categoriasDrive) {
                    for (Object t : c) {
                        tipos.add(t.toString());
                    }
                }

                List<List<Object>> pessoasDrive = service.spreadsheets().values().get(id, getLangString(R.string.sheet_tab_persons)).execute().getValues();

                for (List<Object> c : pessoasDrive) {
                    for (Object t : c) {
                        pessoas.add(t.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }

            AtomicBoolean finalError = new AtomicBoolean(error);
            getActivity().runOnUiThread(() -> {
                progress.dismiss();
                if(finalError.get()){
                    Toast.makeText(getContext(), getString(R.string.global_error),
                            Toast.LENGTH_LONG).show();
                }else {
                    ArrayAdapter adapterTipos = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, tipos);
                    ArrayAdapter adapterPessoas = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, pessoas);

                    final View addViewMovimento = activity.getLayoutInflater().inflate(R.layout.add_movimento, null);
                    final AppCompatSpinner tipo = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_tipo);
                    tipo.setAdapter(adapterTipos);
                    tipo.setSelection(adapterTipos.getPosition(movimento.getTipo()));

                    final AppCompatSpinner pessoa = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_pessoa);
                    pessoa.setAdapter(adapterPessoas);
                    pessoa.setSelection(adapterPessoas.getPosition(movimento.getPessoa()));

                    final EditText valor = addViewMovimento.findViewById(R.id.input_ss_value);
                    try{
                       Double val =  Double.parseDouble(movimento.getValor());
                       valor.setText(val.toString());
                    }catch (Exception e){
                        try{
                            Double val =  Double.parseDouble(movimento.getValor().replace(",","."));
                            valor.setText(val.toString());
                        }catch (Exception e1){
                            try{
                                Double val =  Double.parseDouble(movimento.getValor().replace(".",","));
                                valor.setText(val.toString());
                            }catch (Exception e2){
                            }
                        }
                    }



                    final EditText descricao = addViewMovimento.findViewById(R.id.input_ss_descricao);
                    descricao.setText(movimento.getDescricao());
                    final TextView date = addViewMovimento.findViewById(R.id.input_ss_date);
                    date.setText(movimento.getData());

                    date.setOnClickListener(v -> {

                        LayoutInflater inflater = (LayoutInflater)activity.getApplicationContext().getSystemService
                                (Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout calendar_layout= (LinearLayout)inflater.inflate(R.layout.calendar_layout, null, false);

                        final CalendarView simpleCalendarView = calendar_layout.findViewById(R.id.calendarView); // get the reference of CalendarView
                        simpleCalendarView.setDate((Preferences.convertToSimpleDate(movimento.getData())).getTime());
                        simpleCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                            Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
                            date.setText(Preferences.convertFromSimpleDate(calendar.getTime()));
                        });

                        new AlertDialog.Builder(activity)
                                .setView(calendar_layout)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //do nothing...yet
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // Do nothing.
                                            }
                                        }
                                ).show();


                    });

                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    AlertDialog dialog;
                    builder.setView(addViewMovimento);
                    builder.setPositiveButton(R.string.confirm_button, (dialog1, which) -> {
                        progress.show();
                        Calendar selected = Calendar.getInstance();
                        selected.setTime(Preferences.convertToSimpleDate(date.getText().toString()));

                        List<Object> movimentos = new ArrayList<>();
                        movimentos.add(date.getText().toString());
                        movimentos.add(descricao.getText().toString());
                        Double valorDouble = Double.parseDouble(valor.getText().toString());
                        movimentos.add(valorDouble);
                        movimentos.add(tipo.getItemAtPosition(tipo.getSelectedItemPosition()).toString());
                        movimentos.add(pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString());
                        movimentos.add(selected.get(Calendar.MONTH)+1);
                        movimentos.add(selected.get(Calendar.YEAR));

                        int indexFinal = movimentoList.size() - index;
                        String range = getLangString(R.string.sheet_tab_entry_name) +"!A"+(indexFinal+1)+":G"+(indexFinal+1);


                        ValueRange insert = new ValueRange();
                        insert.setValues(Arrays.asList(movimentos));
                        insert.setRange(range);

                        new Thread(() -> {
                            try {

                                service.spreadsheets().values().update(id, range, insert).setValueInputOption("RAW").execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                                finalError.set(true);
                            }
                            getActivity().runOnUiThread(() -> {
                                progress.dismiss();
                                if(finalError.get()){
                                    Toast.makeText(getContext(), getString(R.string.global_error),
                                            Toast.LENGTH_LONG).show();
                                }else {
                                    loadMovimentosList();
                                }
                            });
                        }).start();
                    });
                    builder.setNegativeButton(R.string.close_button, (dialog12, which) -> dialog12.dismiss());

                    dialog = builder.create();
                    dialog.show();
                }
            });
        }).start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}