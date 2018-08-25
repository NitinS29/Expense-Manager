package example.com.hw4;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditExpenseActivity extends AppCompatActivity {

    final static int GALLERY_REQ = 1;
    final static int CAMERA_REQ = 2;
    private static final String IMAGE_DIRECTORY = "/expenses";
    EditText editTextName;
    EditText editTextCost;
    Button btnEditExpense;
    Button btnDatePicker;
    TextView textViewDateValue;
    Expense expenseEdit;
    ImageView imgReceipt;
    Button btnChooseImg;
    String imageFileName;
    DatePickerDialog dialog;
    String imageURI;
    Bitmap receiptBitmap;
    boolean imgChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        imgChanged = false;
        editTextName = (EditText)findViewById(R.id.editTextName);
        editTextCost = (EditText)findViewById(R.id.editTextCost);
        btnEditExpense = (Button)findViewById(R.id.buttonEditExpense);
        btnDatePicker = (Button)findViewById(R.id.buttonPickADate);
        textViewDateValue = (TextView)findViewById(R.id.textViewDateValue);
        imgReceipt = (ImageView)findViewById(R.id.imageViewReceipt);
        btnChooseImg =(Button)findViewById(R.id.buttonChooseImage);

        if(getIntent().getExtras() != null) {
            expenseEdit = (Expense) getIntent().getExtras().getSerializable(MainActivity.EXP_KEY);
            editTextName.setText(expenseEdit.getEname());
            editTextCost.setText(expenseEdit.geteCost() + "");
            textViewDateValue.setText(expenseEdit.geteDate());
            Picasso.with(EditExpenseActivity.this).load(expenseEdit.getImageUrl()).into(imgReceipt);

        }
        btnEditExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputs() == "") {
                    try {
                        expenseEdit.setEname(editTextName.getText().toString());
                        expenseEdit.seteCost(Double.parseDouble(editTextCost.getText().toString()));
                        expenseEdit.seteDate(textViewDateValue.getText().toString());
                        //uploadReceipt();
                        if (imgChanged) {
                            uploadReceipt();
                        } else {
                            expenseEdit.editExpense(expenseEdit);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }catch (Exception ex){
                        Toast.makeText(EditExpenseActivity.this,"Please enter proper inputs",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(EditExpenseActivity.this,validateInputs(),Toast.LENGTH_LONG).show();
                }
            }
        });

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();;
                imgChanged = true;
            }
        });

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
                dialog = new DatePickerDialog(EditExpenseActivity.this,
                        expDateListener, mYear, mMonth, mDay);
                dialog.show();

            }
        });


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

    private boolean uploadReceipt(){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp;
        StorageReference mountainsRef = storageRef.child(imageFileName + ".jpg");
        StorageReference mountainImagesRef = storageRef.child("expenses/" + imageFileName + ".jpg");
        mountainsRef.getName().equals(mountainImagesRef.getName());
        mountainsRef.getPath().equals(mountainImagesRef.getPath());
        Bitmap bitmap = receiptBitmap;//recipt.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                imageURI = downloadUrl.toString();
                expenseEdit.setImageUrl(imageURI);
                expenseEdit.editExpense(expenseEdit);
                setResult(RESULT_OK);
                finish();
            }
        });
        return true;
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
                    Toast.makeText(EditExpenseActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imgReceipt.setImageBitmap(bitmap);
                    receiptBitmap = bitmap;

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EditExpenseActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == CAMERA_REQ) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imgReceipt.setImageBitmap(thumbnail);
            receiptBitmap = thumbnail;
            saveImage(thumbnail);
            Toast.makeText(EditExpenseActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
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
