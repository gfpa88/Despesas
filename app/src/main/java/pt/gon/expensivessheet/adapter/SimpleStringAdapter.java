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

import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.SheetActivity;
import pt.gon.expensivessheet.ui.SheetFragment;

public class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.MyViewHolder> {

    private List<String> mList;
    SheetFragment fragment;
    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView name;
        public MyViewHolder(View view) {
            super(view);


            name = itemView.findViewById(R.id.name);
            final TextView id = itemView.findViewById(R.id.id);
            final View mainView = itemView.findViewById(R.id.mainView);
            final ImageButton deleteButton = itemView.findViewById(R.id.delete_button);
            final ImageButton editButton = itemView.findViewById(R.id.edit_button);


            deleteButton.setOnClickListener(v -> {
                final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                AlertDialog dialog;
                builder.setTitle(R.string.dialog_delete_generic);
                builder.setMessage(R.string.dialog_delete_generic_message);
                builder.setPositiveButton(R.string.delete_button, (dialog1, which) -> fragment.delete(getAdapterPosition()));
                builder.setNegativeButton(R.string.close_button, (dialog12, which) -> dialog12.dismiss());

                dialog = builder.create();
                dialog.show();

            });

            editButton.setOnClickListener(v -> {
                fragment.edit(getAdapterPosition(),  mList.get(getAdapterPosition()));
            });
        }
    }


    public SimpleStringAdapter(SheetFragment fragment, List<String> list) {
        this.mList = list;
        this.fragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.spreadsheet_item, parent, false);

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String name = mList.get(position);
        holder.name.setText(name);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

}
