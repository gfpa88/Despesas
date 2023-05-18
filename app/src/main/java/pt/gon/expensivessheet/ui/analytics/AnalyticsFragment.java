package pt.gon.expensivessheet.ui.analytics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.databinding.FragmentAnalyticsBinding;
import pt.gon.expensivessheet.ui.analytics.model.Report;
import pt.gon.expensivessheet.ui.analytics.model.ReportChild;
import pt.gon.expensivessheet.ui.analytics.model.ReportElement;
import pt.gon.expensivessheet.utils.Utils;
import pt.gon.expensivessheet.ws.ApiService;
import pt.gon.expensivessheet.ws.model.Movimento;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    String id;
    Activity activity;
    String lang;
    private List<String> categorias, pessoas;
    private List<Movimento> movimentos;
    private List<String> anos;
    private List<String> periodos;
    private List<String> meses;


    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = getActivity();

        id = getActivity().getIntent().getExtras().getString("id");
        init();

        binding.generateReportButton.setOnClickListener(view -> {
            generateReport();
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void init() {
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new Thread(() -> {
            try {

                lang = ApiService.getInstance().getConfiguration(id,getContext()).getLanguage();
                pessoas = ApiService.getInstance().getPersonOrCategory(id, getLangString(R.string.sheet_tab_persons), getContext());
                categorias = ApiService.getInstance().getPersonOrCategory(id, getLangString(R.string.sheet_tab_category), getContext());
                movimentos = ApiService.getInstance().getTransactions(id, getLangString(R.string.sheet_tab_expensive_load), getContext());
                periodos = Arrays.asList(getString(R.string.period_complete), getString(R.string.period_year),getString(R.string.period_month), getString(R.string.period_interval));
                anos = movimentos.stream().map(Movimento::getAno).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                meses = anos.isEmpty() ? new ArrayList<>() : movimentos.stream().filter(f -> f.getAno().equals(anos.get(0))).map(Movimento::getMes).distinct().sorted().collect(Collectors.toList());

                getActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    ArrayAdapter adapterPeriod = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, periodos);
                    ArrayAdapter adapterAnos = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, anos);
                    ArrayAdapter adapterMeses = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, meses);

                    final AppCompatSpinner inputPeriodOption = binding.inputPeriodOption;
                    inputPeriodOption.setAdapter(adapterPeriod);
                    inputPeriodOption.setSelection(0);

                    inputPeriodOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            switch (position) {
                                //Complete
                                case 0:
                                    binding.periodValueLayout.setVisibility(View.GONE);
                                    binding.dateCustomLayout.setVisibility(View.GONE);
                                    break;
                                //Ano
                                case 1:
                                    binding.periodValueLayout.setVisibility(View.VISIBLE);
                                    binding.periodYearLayout.setVisibility(View.VISIBLE);
                                    binding.periodMonthLayout.setVisibility(View.GONE);
                                    binding.dateCustomLayout.setVisibility(View.GONE);
                                    break;
                                //Mês
                                case 2:
                                    binding.periodValueLayout.setVisibility(View.VISIBLE);
                                    binding.dateCustomLayout.setVisibility(View.GONE);
                                    binding.periodYearLayout.setVisibility(View.VISIBLE);
                                    binding.periodMonthLayout.setVisibility(View.VISIBLE);
                                    break;
                                //Intervalo
                                case 3:
                                    binding.periodValueLayout.setVisibility(View.GONE);
                                    binding.dateCustomLayout.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                        }
                    });

                    final AppCompatSpinner inputPeriodYear = binding.inputPeriodYear;
                    inputPeriodYear.setAdapter(adapterAnos);
                    inputPeriodYear.setSelection(0);

                    inputPeriodYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            meses = anos.isEmpty() ? new ArrayList<>() : movimentos.stream().filter(f -> f.getAno().equals(inputPeriodYear.getSelectedItem())).map(Movimento::getMes).distinct().collect(Collectors.toList());
                            adapterMeses.clear();
                            adapterMeses.addAll(meses);
                            adapterMeses.notifyDataSetChanged();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                        }
                    });

                    final AppCompatSpinner inputPeriodMonth = binding.inputPeriodMonth;
                    inputPeriodMonth.setAdapter(adapterMeses);
                    inputPeriodMonth.setSelection(0);

                    final TextView inputInitialDate = binding.inputInitialDate;
                    inputInitialDate.setOnClickListener(v -> {
                        getDateFromModal(inputInitialDate);
                    });

                    final TextView inputFinalDate = binding.inputFinalDate;
                    inputFinalDate.setOnClickListener(v -> {
                        getDateFromModal(inputFinalDate);
                    });

                    pessoas.forEach( p -> addChip(p,binding.filterAnalyticsPerson));
                    categorias.forEach( p -> addChip(p,binding.filterAnalyticsCategory));


                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void generateReport(){
        List<String> categories = getSelectedChips(binding.filterAnalyticsCategory);
        List<String> persons = getSelectedChips(binding.filterAnalyticsPerson);

        String year = binding.inputPeriodYear.getItemAtPosition(binding.inputPeriodYear.getSelectedItemPosition()).toString();
        String month = binding.inputPeriodMonth.getItemAtPosition(binding.inputPeriodMonth.getSelectedItemPosition()).toString();

        List<Movimento> filterTransactions = movimentos.stream().filter(f->persons.contains(f.getPessoa()) && categories.contains(f.getTipo())).collect(Collectors.toList());

        switch (binding.inputPeriodOption.getSelectedItemPosition()) {
            //Complete
            case 0:
                break;
            //Ano
            case 1:
                 filterTransactions = filterTransactions.stream().filter(f->f.getAno().equals(year)).collect(Collectors.toList());
                break;
            //Mês
            case 2:
                filterTransactions = filterTransactions.stream().filter(f->f.getAno().equals(year) && f.getMes().equals(month)).collect(Collectors.toList());
                break;
            //Intervalo
            case 3:
                filterTransactions = filterTransactions.stream().filter(f->Preferences.convertToSimpleDate(f.getData()).after(Preferences.convertToSimpleDate(binding.inputInitialDate.getText().toString())) && Preferences.convertToSimpleDate(f.getData()).before(Preferences.convertToSimpleDate(binding.inputFinalDate.getText().toString())) ).collect(Collectors.toList());
                break;
        }

        Report report = new Report();
        report.setMovimentos(filterTransactions);

        Map<String, ReportElement> pessoasMap = generateReportByPerson(filterTransactions);


    }

    private Map<String, ReportElement> generateReportByPerson(List<Movimento> filterTransactions) {
        Map<String, ReportElement> pessoasMap = new HashMap<>();
        for (Movimento myObject : filterTransactions) {
            String key = myObject.getPessoa();
            Double value = Double.parseDouble(myObject.getValor());
            if (pessoasMap.containsKey(key)) {
                ReportElement element = pessoasMap.get(key);
                Double sum = element.getValue() + value;
                Map<String, Double> categoriasMap = element.getChilds();
                if (categoriasMap.containsKey(key)) {
                    Double sumCat = categoriasMap.get(key) + value;
                    categoriasMap.put(key, sumCat);
                }else{
                    categoriasMap = new HashMap<>();
                    categoriasMap.put(myObject.getTipo(), value);
                }

                element.setValue(sum);
            } else {
                ReportElement element = new ReportElement();
                element.setValue(value);
                Map<String, Double> categoriasMap = new HashMap<>();
                categoriasMap.put(myObject.getTipo(), value);
                element.setChilds(categoriasMap);
                pessoasMap.put(key, element);
            }
        }
        return pessoasMap;
    }

    private List<String> getSelectedChips(ChipGroup pChipGroup){
        List<String> result = new ArrayList<>();
        List<Integer> ids = pChipGroup.getCheckedChipIds();
        for (Integer id:ids){
            Chip chip = pChipGroup.findViewById(id);
            result.add(chip.getText().toString());
        }
        return result;
    }

    private void addChip(String pItem, ChipGroup pChipGroup) {
        Chip lChip = new Chip(getContext());
        lChip.setText(pItem);
        lChip.setCheckable(true);
        lChip.setChecked(true);
        ChipDrawable drawable = ChipDrawable.createFromAttributes(getContext(), null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
        lChip.setChipDrawable(drawable);

        pChipGroup.addView(lChip, pChipGroup.getChildCount() - 1);
    }

    private void getDateFromModal(TextView date) {
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
                .setPositiveButton("Ok", (dialog, whichButton) -> {})
                .setNegativeButton("Cancel", (dialog, whichButton) -> {})
                .show();
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