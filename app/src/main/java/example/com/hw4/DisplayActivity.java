package example.com.hw4;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        TextView name  = (TextView)findViewById(R.id.textViewDNameVal);
        TextView cost  = (TextView)findViewById(R.id.textViewDCostVal);
        TextView date  = (TextView)findViewById(R.id.textViewDDateVal);
        ImageView img = (ImageView)findViewById(R.id.imageViewDReceipt);
        Button btnDone = (Button)findViewById(R.id.buttonDone);


        if(getIntent().getExtras() != null){
            Expense expenseDisplay = (Expense) getIntent().getExtras().getSerializable(MainActivity.EXP_KEY);
            name.setText(expenseDisplay.getEname());
            cost.setText(String.valueOf(expenseDisplay.geteCost()));
            date.setText(expenseDisplay.geteDate());
            Picasso.with(DisplayActivity.this).load(expenseDisplay.getImageUrl()).into(img);
        }

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
