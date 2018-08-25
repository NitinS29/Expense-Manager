package example.com.hw4;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements ExpenseRecyclerAdapter.OnItemClickListener {

    final static int REQ_CODE = 100;
    final static int REQ_CODE_EDIT = 101;
    final static int REQ_CODE_DISP = 102;
    final static String EXP_KEY = "expense";

    TextView textViewtotal;
    FloatingActionButton btnAddMain;
    RecyclerView recyclerView;
    ExpenseRecyclerAdapter adapter;
    ArrayList<Expense> expList;
    Expense selectedExpense;
    TextView total;
    double totalVal;
    AlertDialog alert;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewtotal = (TextView)findViewById(R.id.textViewTotalValue);
        btnAddMain = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        total = (TextView)findViewById(R.id.textViewTotalValue);
        totalVal = 0;
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Are you sure you want to reset the exnpense?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference childRef = rootRef.child("expenses");
                        childRef.setValue(null);
                        expList.removeAll(expList);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        btnAddMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddExpenseActivity.class);
                startActivityForResult(intent,REQ_CODE);
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recyclerViewMain);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.custom_divider));
        recyclerView.addItemDecoration(mDividerItemDecoration);
        expList = new ArrayList<Expense>();



        adapter = new ExpenseRecyclerAdapter(expList,MainActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        adapter.setOnItemClickListener(MainActivity.this);
        adapter.notifyDataSetChanged();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childRef = rootRef.child("expenses");

        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                expList.removeAll(expList);
                totalVal = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Expense exp = ds.getValue(Expense.class);
                    expList.add(exp);
                    totalVal += exp.geteCost();
                }
                if(expList.size() > 0) {
                    sortList(true);
                    adapter.notifyDataSetChanged();
                    total.setText("$" + totalVal);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE){
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this,"Expense added successfully",Toast.LENGTH_SHORT).show();
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this,"Expense could not be added",Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQ_CODE_EDIT){
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this,"Expense edited successfully",Toast.LENGTH_SHORT).show();
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this,"Expense could not be edited",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(View itemView, int position) {
        Log.d("clicked Main",position + "");
        selectedExpense = expList.get(position);
        Intent intent = new Intent(MainActivity.this,EditExpenseActivity.class);
        intent.putExtra(EXP_KEY,selectedExpense);
        startActivityForResult(intent,REQ_CODE_EDIT);
    }

    @Override
    public void clickForDisplay(int pos) {
        selectedExpense = expList.get(pos);
        Intent intent = new Intent(MainActivity.this,DisplayActivity.class);
        intent.putExtra(EXP_KEY,selectedExpense);
        startActivityForResult(intent,REQ_CODE_DISP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sorting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sortByCost){
            sortList(false);
            adapter.notifyDataSetChanged();
            return true;
        }else if(item.getItemId() == R.id.sortByDate){
            sortList(true);
            adapter.notifyDataSetChanged();
            return true;
        }else if(item.getItemId() == R.id.resetAll){
            alert = builder.create();
            alert.show();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void sortList(Boolean byDate){
        if(byDate){
            Collections.sort(expList, new Comparator<Expense>() {
                @Override
                public int compare(Expense o1, Expense o2) {
                    int returnValue = 0;
                    try {
                        if(new SimpleDateFormat("MMM dd,yyyy").parse(o1.geteDate()).before(new SimpleDateFormat("MMM dd,yyyy").parse(o2.geteDate())))
                        {returnValue = 1;}
                        else if(new SimpleDateFormat("MMM dd,yyyy").parse(o1.geteDate()).after(new SimpleDateFormat("MMM dd,yyyy").parse(o2.geteDate())))
                        {returnValue = -1; }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    return returnValue;
                }
            });
        }        else{
            Collections.sort(expList, new Comparator<Expense>() {
                @Override
                public int compare(Expense o1, Expense o2) {
                    if(o1.geteCost()> o2.geteCost())
                        return 1;
                    else
                        return -1;
                }
            });
        }

    }
}
