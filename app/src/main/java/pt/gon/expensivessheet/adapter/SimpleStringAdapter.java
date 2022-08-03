package pt.gon.expensivessheet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.ui.SheetFragment;

public class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.MyViewHolder> {

    private List<String> mList;
    SheetFragment fragment;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);

            view.setOnLongClickListener(v -> {
                final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                AlertDialog dialog;
                builder.setTitle(R.string.dialog_delete_entry);
                builder.setMessage(R.string.dialog_delete_entry_message);
                builder.setPositiveButton(R.string.delete_button, (dialog1, which) -> fragment.delete(getAdapterPosition()));
                builder.setNegativeButton(R.string.close_button, (dialog12, which) -> dialog12.dismiss());

                dialog = builder.create();
                dialog.show();

                return false;
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
