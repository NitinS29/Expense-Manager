package example.com.hw4;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nitin on 6/24/2017.
 */

public class Expense implements Serializable{
    String ename, eDate, eKey, imageUrl;
    double eCost;


    public Expense(){

    }

    public Expense(String ename, String eDate, double eCost) {
        this.ename = ename;
        this.eDate = eDate;
        this.eCost = eCost;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String geteKey() {
        return eKey;
    }

    public void seteKey(String eKey) {
        this.eKey = eKey;
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String geteDate() {
        return eDate;
    }

    public void seteDate(String eDate) {
        this.eDate = eDate;
    }

    public double geteCost() {
        return eCost;
    }

    public void seteCost(double eCost) {
        this.eCost = eCost;
    }

    public void writeExpense(Expense expense){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childRef = rootRef.child("expenses");
        String key = childRef.push().getKey();
        expense.seteKey(key);
        childRef.child(expense.geteKey()).setValue(expense);
    }

    public void editExpense(Expense expense){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childRef = rootRef.child("expenses");
        childRef.child(expense.geteKey()).setValue(expense);
    }
}
