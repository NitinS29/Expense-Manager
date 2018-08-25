package example.com.hw4;

import android.*;
import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddExpenseActivity extends AppCompatActivity {

    final static int GALLERY_REQ = 1;
    final static int CAMERA_REQ = 2;
    final static int STORAGE_REQ = 2;
    private static final String IMAGE_DIRECTORY = "/expenses";

    String imageFileName;
    EditText editTextName;
    EditText editTextCost;
    Button btnAddExpense;
    Button btnDatePicker;
    TextView textViewDateValue;
    DatePickerDialog dialog;
    Button btnChooseImg;
    String mCurrentPhotoPath;
    Bitmap mImageBitmap;
    ImageView recipt;
    String imageURI;
    Bitmap receiptBitmap;
    Expense expense;
    boolean imgChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        imgChanged = false;
        editTextName = (EditText)findViewById(R.id.editTextName);
        editTextCost = (EditText)findViewById(R.id.editTextCost);
        btnAddExpense = (Button)findViewById(R.id.buttonEditExpense);
        btnDatePicker = (Button)findViewById(R.id.buttonPickADate);
        textViewDateValue = (TextView)findViewById(R.id.textViewDateValue);
        btnChooseImg = (Button)findViewById(R.id.buttonChooseImage);
        recipt = (ImageView)findViewById(R.id.imageViewReceipt);
        recipt.setImageResource(0);

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpDateListener expDateListener = new ExpDateListener(new ExpDateListener.IDateShow() {
                    @Override
                    public void getChosenDate(String d) {
                        String mDate = d;
                        try {
                            mDate = new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("MM/dd/yyyy").parse(d));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        textViewDateValue.setText(mDate);
                    }
                });
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                dialog = new DatePickerDialog(AddExpenseActivity.this,
                        expDateListener, mYear, mMonth, mDay);
                dialog.show();

            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expense = new Expense();
                if(validateInputs() == "") {
                    try {
                        expense.setEname(editTextName.getText().toString());
                        expense.seteCost(Double.parseDouble(editTextCost.getText().toString()));
                        expense.seteDate(textViewDateValue.getText().toString());
                        //uploadReceipt();
                        if (imgChanged) {
                            uploadReceipt();
                        } else {
                            expense.editExpense(expense);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }catch (Exception ex){
                        Toast.makeText(AddExpenseActivity.this,"Please enter proper inputs",Toast.LENGTH_LONG).show();
                        Log.d("demo",ex.toString());
                    }
                }else{
                    Toast.makeText(AddExpenseActivity.this,validateInputs(),Toast.LENGTH_LONG).show();
                }

            }
        });

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
                imgChanged = true;
            }
        });
    }

    private boolean uploadReceipt(){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp;
        StorageReference mountainsRef = storageRef.child(imageFileName + ".jpg");
        StorageReference mountainImagesRef = storageRef.child("expenses/" + imageFileName + ".jpg");
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false
        Bitmap bitmap = receiptBitmap;//recipt.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                imageURI = downloadUrl.toString();
                expense.setImageUrl(imageURI);
                expense.writeExpense(expense);
                setResult(RESULT_OK);
                finish();
            }
        });
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,CAMERA_REQ);
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        receiptBitmap = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY_REQ) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    String path = saveImage(bitmap);
                    Toast.makeText(AddExpenseActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    recipt.setImageBitmap(bitmap);
                    receiptBitmap = bitmap;

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AddExpenseActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA_REQ) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            recipt.setImageBitmap(thumbnail);
            receiptBitmap = thumbnail;
            saveImage(thumbnail);
            Toast.makeText(AddExpenseActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQ);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQ);
    }

    public String validateInputs(){
        if(editTextName.getText().toString().length() == 0){
            return "Plese enter expense name";
        }else if(editTextCost.getText().toString().length() == 0){
            return "Plese enter expense cost";
        }else if(textViewDateValue.getText().toString().length() == 0){
            return "Plese enter expense date";
        }
        return "";
    }
}
