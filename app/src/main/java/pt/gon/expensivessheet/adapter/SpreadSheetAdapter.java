package pt.gon.expensivessheet.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.gon.expensivessheet.MainActivity;
import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.SheetActivity;
import pt.gon.expensivessheet.SpreadSheet;

public class SpreadSheetAdapter extends RecyclerView.Adapter<SpreadSheetAdapter.MyViewHolder> {

    private List<SpreadSheet> spreadSheetList;
    MainActivity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, id;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            id = view.findViewById(R.id.id);
        }
    }


    public SpreadSheetAdapter(MainActivity activity, List<SpreadSheet> spreadSheetList) {
        this.spreadSheetList = spreadSheetList;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.spreadsheet_item, parent, false);

        final TextView name = itemView.findViewById(R.id.name);
        final TextView id = itemView.findViewById(R.id.id);
        final View mainView = itemView.findViewById(R.id.mainView);
        final ImageButton deleteButton = itemView.findViewById(R.id.delete_button);
        final ImageButton editButton = itemView.findViewById(R.id.edit_button);
        editButton.setVisibility(View.GONE);
        deleteButton.setOnClickListener(v -> {
            removeFromSpreadSheet(name.getText().toString(), id.getText().toString());
        });

        mainView.setOnClickListener(v -> {
            Intent i = new Intent(activity, SheetActivity.class);
            i.putExtra("id", id.getText());
            i.putExtra("name", name.getText());
            activity.startActivity(i);
        });
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SpreadSheet movie = spreadSheetList.get(position);
        holder.name.setText(movie.getName());
        holder.id.setText(movie.getId());
    }

    @Override
    public int getItemCount() {
        return spreadSheetList.size();
    }

    public void removeFromSpreadSheet(final String name, final String id) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog dialog;
        builder.setTitle(R.string.dialog_delete_sheet_title);
        builder.setMessage(R.string.dialog_delete_sheet_message);
        builder.setPositiveButton(R.string.delete_button, (dialog1, which) -> {

            Preferences.removeSpreadSheet(activity, id, name);
            activity.loadSpreadSheatsList();
        });
        builder.setNegativeButton(R.string.close_button, (dialog12, which) -> dialog12.dismiss());

        dialog = builder.create();
        dialog.show();
    }

}
