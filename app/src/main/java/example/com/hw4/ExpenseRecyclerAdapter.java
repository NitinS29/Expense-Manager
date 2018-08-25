package example.com.hw4;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Nitin on 6/24/2017.
 */

public class ExpenseRecyclerAdapter extends RecyclerView.Adapter<ExpenseRecyclerAdapter.ViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
        void clickForDisplay(int pos);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView cost;
        public TextView expensedate;
        public ImageView editExp;
        Context vContext;

        public ViewHolder(Context context,final View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tvMName);
            cost = (TextView) itemView.findViewById(R.id.tvMCost);
            expensedate = (TextView) itemView.findViewById(R.id.tvMDate);
            editExp = (ImageView) itemView.findViewById(R.id.imgEdit);
            editExp.setClickable(true);
            vContext = context;

            editExp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.clickForDisplay(getAdapterPosition());
                }
            });
        }

    }

    List<Expense> expensesList;
    Context mContext;

    public ExpenseRecyclerAdapter(List<Expense> expenses, Context mContext) {
        this.expensesList = expenses;
        this.mContext = mContext;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ExpenseRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.listitem, parent, false);
        ViewHolder viewHolder = new ViewHolder(getContext(),contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ExpenseRecyclerAdapter.ViewHolder holder, int position) {
        Expense expense = expensesList.get(position);
        holder.name.setText(expense.getEname());
        holder.cost.setText("Cost: " + "$" + expense.geteCost());
        holder.expensedate.setText("Date: " + expense.geteDate());
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }


}

