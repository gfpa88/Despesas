package pt.gon.expensivessheet.ui.movimentos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.databinding.FragmentMovimentosBinding;
import pt.gon.expensivessheet.utils.Utils;
import pt.gon.expensivessheet.ws.ApiService;
import pt.gon.expensivessheet.ws.model.Movimento;

public class MovimentosFragment extends Fragment {

    private FragmentMovimentosBinding binding;

    private List<String> tiposMovimentos = new ArrayList<>();
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

    public void postConfigure(String fileId) throws Exception {
        lang = ApiService.getInstance().getConfiguration(fileId,getContext()).getLanguage();
        tiposMovimentos = Arrays.asList(getLangString(R.string.label_transaction_type_normal),getLangString(R.string.label_transaction_type_settlement));
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
                try {

                    postConfigure(id);
                    movimentoList.clear();
                    movimentoList.addAll(ApiService.getInstance().getTransactions(id,getLangString(R.string.sheet_tab_expensive_load),getContext()));

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

    public void deleteMovimentoList(int index) {

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();


        new Thread(() -> {
            int indexFinal = movimentoList.size() - index;

            boolean success = ApiService.getInstance().deleteRow(id, getLangString(R.string.sheet_tab_entry_name),getContext(),indexFinal, indexFinal + 1);

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

            try {

                final List<String> pessoas = ApiService.getInstance().getPersonOrCategory(id, getLangString(R.string.sheet_tab_persons), getContext());
                final List<String> categorias = ApiService.getInstance().getPersonOrCategory(id, getLangString(R.string.sheet_tab_category), getContext());

                AtomicBoolean finalError = new AtomicBoolean(error);
                getActivity().runOnUiThread(() -> {
                    insertTransaction(progress, pessoas, categorias, finalError);
                });
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }
        }).start();

    }

    private void insertTransaction(ProgressDialog progress, List<String> pessoas, List<String> categorias, AtomicBoolean finalError) {
        progress.dismiss();
        if(finalError.get()){
            Toast.makeText(getContext(), getString(R.string.global_error),
                    Toast.LENGTH_LONG).show();
        }else {

            final View addViewMovimento = activity.getLayoutInflater().inflate(R.layout.add_movimento, null);
            final View view_type = addViewMovimento.findViewById(R.id.view_type);
            final AppCompatSpinner tipo = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_type);
            View section_normal = addViewMovimento.findViewById(R.id.section_normal);
            View section_settlement = addViewMovimento.findViewById(R.id.section_settlement);
            section_normal.setVisibility(View.VISIBLE);
            section_settlement.setVisibility(View.GONE);

            /*
            Normal Section
             */
            ArrayAdapter adapterCategorias = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, categorias);
            ArrayAdapter adapterPessoas = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, pessoas);

            final AppCompatSpinner categoria = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_category);
            categoria.setAdapter(adapterCategorias);
            final AppCompatSpinner pessoa = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_pessoa);
            pessoa.setAdapter(adapterPessoas);

            /*
            Settlement Section
             */
            ArrayAdapter adapterTipos = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, tiposMovimentos);
            tipo.setAdapter(adapterTipos);
            tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    switch (position) {
                        case 0:
                            section_normal.setVisibility(View.VISIBLE);
                            section_settlement.setVisibility(View.GONE);
                            break;
                        case 1:
                            section_normal.setVisibility(View.GONE);
                            section_settlement.setVisibility(View.VISIBLE);
                            break;
                        default:
                            section_normal.setVisibility(View.VISIBLE);
                            section_settlement.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            ArrayAdapter adapterCategoriasSender = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, categorias);
            ArrayAdapter adapterPessoasSender = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, pessoas);
            ArrayAdapter adapterCategoriasReceiver = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, categorias);
            ArrayAdapter adapterPessoasReceiver = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, pessoas);

            final AppCompatSpinner categoriaSender = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_settlement_sender_category);
            categoriaSender.setAdapter(adapterCategoriasSender);
            final AppCompatSpinner pessoaSender = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_settlement_sender_person);
            pessoaSender.setAdapter(adapterPessoasSender);
            final AppCompatSpinner categoriaReceiver = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_settlement_receiver_category);
            categoriaReceiver.setAdapter(adapterCategoriasReceiver);
            final AppCompatSpinner pessoaReceiver = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_settlement_receiver_person);
            pessoaReceiver.setAdapter(adapterPessoasReceiver);
            if(pessoas.size() <= 1){
                view_type.setVisibility(View.GONE);
            }else{
                pessoaReceiver.setSelection(1);
            }


            final EditText valor = addViewMovimento.findViewById(R.id.input_ss_value);
            final EditText descricao = addViewMovimento.findViewById(R.id.input_ss_descricao);

            final TextView date = addViewMovimento.findViewById(R.id.input_ss_date);
            date.setText(Preferences.convertFromSimpleDateTime(new Date()));

            date.setOnClickListener(v -> {

                LayoutInflater inflater = (LayoutInflater)activity.getApplicationContext().getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout calendar_layout= (LinearLayout)inflater.inflate(R.layout.calendar_layout, null, false);

                final CalendarView simpleCalendarView = calendar_layout.findViewById(R.id.calendarView); // get the reference of CalendarView
                simpleCalendarView.setDate((new Date()).getTime());
                simpleCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                    Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
                    date.setText(Preferences.convertFromSimpleDateTime(calendar.getTime()));
                });

                new AlertDialog.Builder(activity)
                        .setView(calendar_layout)
                        .setPositiveButton("Ok", (dialog, whichButton) -> {})
                        .setNegativeButton("Cancel", (dialog, whichButton) -> {})
                        .show();
            });

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            AlertDialog dialog;
            builder.setView(addViewMovimento);
            builder.setTitle(R.string.dialog_add_entry_title);
            builder.setPositiveButton(R.string.add_button, (dialog1, which) -> {
                progress.show();
                Calendar selected = Calendar.getInstance();
                selected.setTime(Preferences.convertToSimpleDate(date.getText().toString()));
                Double valorDouble = Double.parseDouble(valor.getText().toString());

                List<List<Object>> values = null;
                switch (tipo.getSelectedItemPosition()) {
                    case 0:
                        List<Object> movimento = new ArrayList<>();
                        movimento.add(date.getText().toString());
                        movimento.add(descricao.getText().toString().trim());
                        movimento.add(valorDouble);
                        movimento.add(categoria.getItemAtPosition(categoria.getSelectedItemPosition()).toString());
                        movimento.add(pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString());
                        movimento.add(selected.get(Calendar.MONTH)+1);
                        movimento.add(selected.get(Calendar.YEAR));

                        values = Arrays.asList(movimento);
                        break;
                    case 1:
                        List<Object> movimentoSender = new ArrayList<>();
                        movimentoSender.add(date.getText().toString());
                        movimentoSender.add(descricao.getText().toString().trim());
                        movimentoSender.add(valorDouble);
                        movimentoSender.add(categoriaSender.getItemAtPosition(categoriaSender.getSelectedItemPosition()).toString());
                        movimentoSender.add(pessoaSender.getItemAtPosition(pessoaSender.getSelectedItemPosition()).toString());
                        movimentoSender.add(selected.get(Calendar.MONTH)+1);
                        movimentoSender.add(selected.get(Calendar.YEAR));

                        List<Object> movimentoReceiver = new ArrayList<>();
                        movimentoReceiver.add(date.getText().toString());
                        movimentoReceiver.add(descricao.getText().toString().trim());
                        movimentoReceiver.add(0-valorDouble);
                        movimentoReceiver.add(categoriaReceiver.getItemAtPosition(categoriaReceiver.getSelectedItemPosition()).toString());
                        movimentoReceiver.add(pessoaReceiver.getItemAtPosition(pessoaReceiver.getSelectedItemPosition()).toString());
                        movimentoReceiver.add(selected.get(Calendar.MONTH)+1);
                        movimentoReceiver.add(selected.get(Calendar.YEAR));

                        values = Arrays.asList(movimentoSender, movimentoReceiver);
                        break;
                }

                List<List<Object>> finalValues = values;
                new Thread(() -> {
                    try {
                        ApiService.getInstance().addRow(id,getLangString(R.string.sheet_tab_expensive_insert), finalValues,getContext());
                    } catch (Exception e) {
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

             List<String> pessoas = new ArrayList<>();
             List<String> categorias = new ArrayList<>();
            try {

                pessoas = ApiService.getInstance().getPersonOrCategory(id, getLangString(R.string.sheet_tab_persons), getContext());
                categorias = ApiService.getInstance().getPersonOrCategory(id, getLangString(R.string.sheet_tab_category), getContext());

            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }

            AtomicBoolean finalError = new AtomicBoolean(error);
            List<String> finalCategorias = categorias;
            List<String> finalPessoas = pessoas;
            getActivity().runOnUiThread(() -> {
                progress.dismiss();
                if(finalError.get()){
                    Toast.makeText(getContext(), getString(R.string.global_error),
                            Toast.LENGTH_LONG).show();
                }else {
                    ArrayAdapter adapterCategorias = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, finalCategorias);
                    ArrayAdapter adapterPessoas = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, finalPessoas);

                    final View addViewMovimento = activity.getLayoutInflater().inflate(R.layout.add_movimento, null);
                    final View view_type = addViewMovimento.findViewById(R.id.view_type);

                    View section_normal = addViewMovimento.findViewById(R.id.section_normal);
                    View section_settlement = addViewMovimento.findViewById(R.id.section_settlement);

                    view_type.setVisibility(View.GONE);
                    section_normal.setVisibility(View.VISIBLE);
                    section_settlement.setVisibility(View.GONE);

                    final AppCompatSpinner categoria = (AppCompatSpinner) addViewMovimento.findViewById(R.id.input_ss_category);
                    categoria.setAdapter(adapterCategorias);
                    categoria.setSelection(adapterCategorias.getPosition(movimento.getTipo()));

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
                            date.setText(Preferences.convertFromSimpleDateTime(calendar.getTime()));
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
                        movimentos.add(categoria.getItemAtPosition(categoria.getSelectedItemPosition()).toString());
                        movimentos.add(pessoa.getItemAtPosition(pessoa.getSelectedItemPosition()).toString());
                        movimentos.add(selected.get(Calendar.MONTH)+1);
                        movimentos.add(selected.get(Calendar.YEAR));

                        int indexFinal = movimentoList.size() - index;
                        String range = getLangString(R.string.sheet_tab_entry_name) +"!A"+(indexFinal+1)+":G"+(indexFinal+1);

                        new Thread(() -> {
                            try {

                                ApiService.getInstance().updateRow(id,range,Arrays.asList(movimentos),getContext());
                            } catch (Exception e) {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}