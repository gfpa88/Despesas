package pt.gon.despesas.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import pt.gon.despesas.MainActivity;
import pt.gon.despesas.MovimentosActivity;
import pt.gon.despesas.R;
import pt.gon.despesas.SpreadSheet;

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
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removeFromSpreadSheet(name.getText().toString(),id.getText().toString());
                return false;
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, MovimentosActivity.class);
                i.putExtra("id", id.getText());
                activity.startActivity(i);
            }
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

    public void removeFromSpreadSheet(final String name, final String id){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog dialog;
        builder.setTitle("Remover Folha");
        builder.setMessage("Queres eliminar a folha?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Preferences.removeSpreadSheet(activity,id,name);
                activity.loadSpreadSheatsList();
            }
        });
        builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

}
