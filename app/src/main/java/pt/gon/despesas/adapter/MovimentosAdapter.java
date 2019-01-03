package pt.gon.despesas.adapter;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import pt.gon.despesas.MovimentosActivity;
import pt.gon.despesas.R;
import pt.gon.despesas.ws.model.Movimento;

public class MovimentosAdapter extends RecyclerView.Adapter<MovimentosAdapter.MyViewHolder> {

    private List<Movimento> movimentosList;
    MovimentosActivity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView value, tipo, date, pessoa,descricao;

        public MyViewHolder(View view) {
            super(view);
            value = view.findViewById(R.id.value);
            tipo = view.findViewById(R.id.tipo);
            pessoa = view.findViewById(R.id.pessoa);
            date = view.findViewById(R.id.date);
            descricao = view.findViewById(R.id.descricao);
        }
    }


    public MovimentosAdapter(MovimentosActivity activity, List<Movimento> movimentosList) {
        this.movimentosList = movimentosList;
        this.activity = activity;
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
        Date d = Preferences.convertToDate(movie.getData());
        if (d!=null){
            holder.date.setText(""+DateFormat.format("dd-MM-yyyy HH:mm:ss", d).toString());
        }
        holder.descricao.setText(movie.getDescricao());
        holder.pessoa.setText(movie.getPessoa());
    }

    @Override
    public int getItemCount() {
        return movimentosList.size();
    }

}
