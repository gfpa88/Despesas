package pt.gon.expensivessheet.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
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
import pt.gon.expensivessheet.adapter.Preferences;
import pt.gon.expensivessheet.adapter.SimpleStringAdapter;

public abstract class SheetFragment extends Fragment {

//    public abstract void delete(int index);
//    public abstract void edit(int index, String value);

    public abstract String getSheetTab(String lang);
    public abstract String getTabName(String lang);
    public abstract String getAddTitleName();

    public List<String> mlist = new ArrayList<>();
    public RecyclerView recyclerView;
    public  SimpleStringAdapter mAdapter;

    public String id;
    public Activity activity;
    public String lang;

    public boolean floatExpanded = false;


    public void getLang(String fileId, Sheets service) throws IOException {
        List<List<Object>> versionTab = service.spreadsheets().values().get(fileId, "Version!A1:A3").execute().getValues();
        lang = versionTab.get(2).get(0).toString();
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

                    getLang(id, service);
                    List<List<Object>> drive = service.spreadsheets().values().get(id, getSheetTab(lang)).execute().getValues();
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

    public void add() {
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        AlertDialog dialog;
        builder.setView(R.layout.add_spreadsheet);
        builder.setTitle(getAddTitleName());
        builder.setPositiveButton(R.string.add_button, (dialog1, which) -> {
            Dialog dialog2 = Dialog.class.cast(dialog1);
            List<Object> cat = new ArrayList<>();
            EditText name = dialog2.findViewById(R.id.input_ss_name);
            cat.add(name.getText().toString());
            ValueRange insert = new ValueRange();
            insert.setValues(Arrays.asList(cat));
            insert.setRange(getSheetTab(lang));

            new Thread(() -> {
                boolean error = false;
                try {
                    Sheets service = new Sheets.Builder(new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                            .setApplicationName(getString(R.string.app_name))
                            .build();
                    service.spreadsheets().values().append(id, getSheetTab(lang), insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();
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

    public void edit(int index, String value) {
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(R.string.dialog_loading);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        // 1. Instantiate an AlertDialog.Builder with its constructor

        final View editView = activity.getLayoutInflater().inflate(R.layout.add_spreadsheet, null);
        EditText name = editView.findViewById(R.id.input_ss_name);
        name.setText(value);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog;
        builder.setView(editView);
        builder.setPositiveButton(R.string.confirm_button, (dialog1, which) -> {

            String range = getTabName(lang)+"!"+(index+1)+":"+(index+1);

            List<Object> cat = new ArrayList<>();
            cat.add(name.getText().toString());
            ValueRange insert = new ValueRange();
            insert.setValues(Arrays.asList(cat));
            insert.setRange(range);

            new Thread(() -> {
                boolean error = false;
                try {
                    Sheets service = new Sheets.Builder(new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                            .setApplicationName(getString(R.string.app_name))
                            .build();

                    service.spreadsheets().values().update(id, range, insert).setValueInputOption("RAW").execute();
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
            if (s.getProperties().getTitle().equals(getTabName(lang))) {
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

    public void importOptions() {

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(getString(R.string.dialog_loading));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        try {

            new Thread(() -> {
                boolean error = false;
                // do background stuff here
                Drive driveService = new Drive.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                        .setApplicationName(getString(R.string.app_name))
                        .build();

                List<File> files = new ArrayList<>();
                try {
                    Drive.Files.List request = driveService.files().list()
                            //.setPageSize(100)
                            // Available Query parameters here:
                            //https://developers.google.com/drive/v3/web/search-parameters
                            .setQ("mimeType = 'application/vnd.google-apps.spreadsheet' and (name contains 'Despesa' or name contains 'Expense') and trashed = false")
                            .setFields("nextPageToken, files(id, name)");

                    FileList result = request.execute();

                    files = result.getFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }

                List<File> finalFiles = files;
                boolean finalError = error;
                getActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    if (finalError) {
                        Toast.makeText(getContext(), getString(R.string.global_error),
                                Toast.LENGTH_LONG).show();
                    } else {
                        chooseSheetFromDrive(finalFiles);
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseSheetFromDrive(final List<File> files) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle(R.string.dialog_import_options_title);
        builderSingle.setNegativeButton(R.string.cancel_button,(dialogInterface, i) -> {});
        List<String> accountName = new ArrayList<>();
        int ownIndex = -1;
        for (int i = 0; i<files.size(); i++ ) {
            if(!files.get(i).getId().equals(id)) {
                accountName.add(files.get(i).getName());
            }else{
                ownIndex = i;
            }
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice, accountName);

        int finalOwnIndex = ownIndex;
        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            if(which >= finalOwnIndex){
                which++;
            }
            dialog.dismiss();
            validateFile(files.get(which));

        });

        builderSingle.show();
    }

    public void validateFile(File file) {

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

                    List<List<Object>> versionTab = service.spreadsheets().values().get(file.getId(), "Version!A1:A3").execute().getValues();
                    String name = versionTab.get(0).get(0).toString();
                    String langFile = versionTab.get(2).get(0).toString();
                    if (!name.equals("expensivessheet")) {
                        error = true;
                    }else{
                        List<String> toImport = new ArrayList<>();
                        List<List<Object>> drive = service.spreadsheets().values().get(file.getId(), getSheetTab(langFile)).execute().getValues();
                        for (List<Object> c : drive) {
                            for (Object t : c) {
                                if(!mlist.contains(t.toString())) {
                                    if(!toImport.contains(t.toString())) {
                                        toImport.add(t.toString());
                                    }
                                }
                            }
                        }

                        if(!toImport.isEmpty()) {
                            //build ValueRange
                            List<List<Object>> finalImport = new ArrayList<>();
                            for (String l : toImport) {
                                finalImport.add(Arrays.asList(l));
                            }
                            ValueRange insert = new ValueRange();
                            insert.setValues(finalImport);
                            insert.setRange(getSheetTab(lang));
                            service.spreadsheets().values().append(id, getSheetTab(lang), insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }

                boolean finalError = error;
                getActivity().runOnUiThread(() -> {

                    progress.dismiss();
                    if (finalError) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                        builderSingle.setTitle(R.string.error_invalid_sheet);

                        AlertDialog dialog;
                        builderSingle.setNeutralButton(R.string.close_button, (dialogInterface, i) -> dialogInterface.dismiss());
                        dialog = builderSingle.create();
                        dialog.show();
                    } else {
                        load();
                    }
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
}
