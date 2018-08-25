package example.com.hw4;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

/**
 * Created by Nitin on 6/24/2017.
 */

class ExpDateListener implements DatePickerDialog.OnDateSetListener {

    String mDate;
    IDateShow iDateShow;

    public ExpDateListener(IDateShow iDateShow){
      this.iDateShow = iDateShow;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        // TODO Auto-generated method stub
        // getCalender();
        int mYear = year;
        int mMonth = monthOfYear;
        int mDay = dayOfMonth;

        mDate = (new StringBuilder()
                .append(mMonth + 1).append("/").append(mDay).append("/")
                .append(mYear).append(" ")).toString();
        iDateShow.getChosenDate(mDate);
    }

    public interface IDateShow{
        void  getChosenDate(String d);
    }
}