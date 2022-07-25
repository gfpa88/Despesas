package pt.gon.expensivessheet.ui.movimentos;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.ws.model.Movimento;

public class MovimentosAdapter extends RecyclerView.Adapter<MovimentosAdapter.MyViewHolder> {

    private List<Movimento> movimentosList;
    MovimentosFragment fragment;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView value, tipo, date, pessoa,descricao;

        public MyViewHolder(View view) {
            super(view);
            value = view.findViewById(R.id.value);
            tipo = view.findViewById(R.id.tipo);
            pessoa = view.findViewById(R.id.pessoa);
            date = view.findViewById(R.id.date);
            descricao = view.findViewById(R.id.descricao);

            view.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                    AlertDialog dialog;
                    builder.setTitle("Remover Movimento");
                    builder.setMessage("Queres eliminar este movimento?");
                    builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragment.deleteMovimentoList(getAdapterPosition());
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

                    return false;
                }
            });
        }
    }


    public MovimentosAdapter(MovimentosFragment fragment, List<Movimento> movimentosList) {
        this.movimentosList = movimentosList;
        this.fragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movimento_item, parent, false);

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Movimento movie = movimentosList.get(position);
        holder.value.setText(""+movie.getValor());
        holder.tipo.setText(movie.getTipo());
        holder.date.setText(movie.getData());
        holder.descricao.setText(movie.getDescricao());
        holder.pessoa.setText(movie.getPessoa());
    }

    @Override
    public int getItemCount() {
        return movimentosList != null ? movimentosList.size(): 0;
    }

}
