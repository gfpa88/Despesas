package pt.gon.expensivessheet.ui.pessoas;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gon.expensivessheet.GoogleCrendentialSingleton;
import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.SimpleStringAdapter;
import pt.gon.expensivessheet.databinding.FragmentCategoriasBinding;
import pt.gon.expensivessheet.databinding.FragmentPessoasBinding;
import pt.gon.expensivessheet.ui.SheetFragment;

public class PessoasFragment extends SheetFragment {

    private FragmentPessoasBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentPessoasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = getActivity();

        id = getActivity().getIntent().getExtras().getString("id");


        recyclerView = binding.recyclerSpreedSheet;

        mAdapter = new SimpleStringAdapter(this, mlist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        FloatingActionButton fabExpand = binding.fabExpand;
        FloatingActionButton fabSearch = binding.fabSearch;
        FloatingActionButton fab = binding.fab;
        fabExpand.setOnClickListener(view -> {
            if (!floatExpanded) {
                floatExpanded = true;
                fabSearch.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimaryDark)));
            } else {
                floatExpanded = false;
                fabSearch.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimary)));
            }
        });
        fab.setOnClickListener(view -> {
            floatExpanded = false;
            fabSearch.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimary)));
            add();
        });
        fabSearch.setOnClickListener(view -> {
            floatExpanded = false;
            fabSearch.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            fabExpand.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimary)));
            importOptions();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    @Override
    public String getSheetTab() {
        return getString(R.string.sheet_tab_persons);
    }

    @Override
    public String getTabName() {
        return getString(R.string.sheet_tab_persons_name);
    }

    @Override
    public String getAddTitleName() {
        return getString(R.string.dialog_add_person_title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}