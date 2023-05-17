package pt.gon.expensivessheet.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.adapter.SimpleStringAdapter;
import pt.gon.expensivessheet.ws.ApiService;
import pt.gon.expensivessheet.ws.model.Configuration;

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


    public void getLang(String fileId) throws Exception {
        this.lang = ApiService.getInstance().getConfiguration(fileId, getContext()).getLanguage();
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

                try {
                    getLang(id);
                    mlist.clear();
                    mlist.addAll(ApiService.getInstance().getPersonOrCategory(id,getSheetTab(lang),getContext()));
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


            new Thread(() -> {
                boolean error = false;
                try {
                    ApiService.getInstance().addRow(id,getSheetTab(lang),Arrays.asList(cat),getContext());
                } catch (Exception e) {
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

            new Thread(() -> {
                boolean error = false;
                try {
                    ApiService.getInstance().updateRow(id,range,Arrays.asList(cat),getContext());
                      } catch (Exception e) {
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
            boolean success = ApiService.getInstance().deleteRow(id, getTabName(lang),getContext(),index, index + 1);
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



    public void importOptions() {

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(getString(R.string.dialog_loading));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        try {

            new Thread(() -> {
                boolean error = false;
                // do background stuff here
                List<File> files = new ArrayList<>();
                try {
                    files = ApiService.getInstance().getFiles(getContext());
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
                try {
                    Configuration config = ApiService.getInstance().getConfiguration(file.getId(), getContext());
                    String name = config.getName();
                    String langFile = config.getLanguage();
                    if (!name.equals("expensivessheet")) {
                        error = true;
                    }else{
                        List<String> toImport = new ArrayList<>();
                        List<String> drive = ApiService.getInstance().getPersonOrCategory(file.getId(),getSheetTab(langFile), getContext());

                        for (Object t : drive) {
                            if(!mlist.contains(t.toString())) {
                                if(!toImport.contains(t.toString())) {
                                    toImport.add(t.toString());
                                }
                            }
                        }

                        if(!toImport.isEmpty()) {
                            //build ValueRange
                            List<Object> finalImport = new ArrayList<>();
                            for (String l : toImport) {
                                finalImport.add(Arrays.asList(l));
                            }
                            ApiService.getInstance().addRow(id,getSheetTab(lang),Arrays.asList(finalImport),getContext());

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
