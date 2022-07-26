package pt.gon.expensivessheet.ui.pessoas;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;

import pt.gon.expensivessheet.GoogleCrendentialSingleton;
import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.SimpleStringAdapter;
import pt.gon.expensivessheet.databinding.FragmentPessoasBinding;
import pt.gon.expensivessheet.ui.SheetFragment;

public class PessoasFragment extends SheetFragment {

    private FragmentPessoasBinding binding;
    private List<String> mlist = new ArrayList<>();
    private RecyclerView recyclerView;
    private SimpleStringAdapter mAdapter;

    String id;
    Activity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPessoasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = getActivity();

        id = getActivity().getIntent().getExtras().getString("id");

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> add());

        recyclerView = binding.recyclerSpreedSheet;

        mAdapter = new SimpleStringAdapter(this, mlist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    public void load() {

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

                try {

                    List<List<Object>> drive = service.spreadsheets().values().get(id, getString(R.string.sheet_tab_persons)).execute().getValues();
                    mlist.clear();
                    for (List<Object> c : drive) {
                        for (Object t : c) {
                            mlist.add(t.toString());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }

                boolean finalError = error;
                getActivity().runOnUiThread(() -> {
                    if(finalError){
                        Toast.makeText(getContext(), getString(R.string.global_error),
                                Toast.LENGTH_LONG).show();
                    }else {
                        mAdapter = new SimpleStringAdapter(this, mlist);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                    progress.dismiss();
                    // OnPostExecute stuff here
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
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
            if (s.getProperties().getTitle().equals(getString(R.string.sheet_tab_persons_name))) {
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

    public void delete(int index) {

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        Request request = new Request()
                .setDeleteDimension(new DeleteDimensionRequest()
                        .setRange(new DimensionRange()
                                .setDimension("ROWS")
                                .setStartIndex(index)
                                .setEndIndex(index)
                        )
                );

        List<Request> requests = new ArrayList<>();
        requests.add(request);

        new Thread(() -> {
            boolean success = deleteRow(index, index + 1);
            getActivity().runOnUiThread(() -> {
                progress.dismiss();
                if(!success){
                    Toast.makeText(getContext(), getString(R.string.global_error),
                            Toast.LENGTH_LONG).show();
                }else {
                    load();
                }
            });
        }).start();

    }

    public void add() {
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog;
        builder.setView(R.layout.add_spreadsheet);
        builder.setTitle(R.string.dialog_add_person_title);
        builder.setPositiveButton(R.string.add_button, (dialog1, which) -> {
            Dialog dialog2 = Dialog.class.cast(dialog1);
            EditText name = dialog2.findViewById(R.id.input_ss_name);
            List<Object> cat = new ArrayList<>();
            cat.add(name.getText().toString());
            ValueRange insert = new ValueRange();
            insert.setValues(Arrays.asList(cat));
            insert.setRange(getString(R.string.sheet_tab_persons));

            new Thread(() -> {
                boolean error = false;
                try {
                    Sheets service = new Sheets.Builder(new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                            .setApplicationName(getString(R.string.app_name))
                            .build();
                    service.spreadsheets().values().append(id, getString(R.string.sheet_tab_persons), insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    error = true;
                }
                boolean finalError = error;
                getActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    if(finalError){
                        Toast.makeText(getContext(), getString(R.string.global_error),
                                Toast.LENGTH_LONG).show();
                    }else{
                        load();

                    }
                });
            }).start();
        });
        builder.setNegativeButton(R.string.close_button, (dialog12, which) -> dialog12.dismiss());

        dialog = builder.create();
        dialog.show();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}