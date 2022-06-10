package com.example.garbagegoapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garbagegoapp.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    ImageButton location, image, send;
    TextView address;
    FusedLocationProviderClient fusedLocationProviderClient;
    protected LocationManager locationManager;
    ActivityMainBinding binding;
    StorageReference storageReference;
    Uri getImageUri;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        image = findViewById(R.id.btnimage);
        send = findViewById(R.id.btnsend);
        imageView = findViewById(R.id.image1);
        location = findViewById(R.id.btnloc);
        address = findViewById(R.id.locationtext1);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                showToast("Submit Button Clicked ");
                upload();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                showDialog();
                String FILENAME = "image/";
                File f = new File(FILENAME);
                getImageUri = Uri.fromFile(f);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Location Button Clicked ");
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });
    }

    private void upload() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String filename = dateFormat.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+filename);
        storageReference.putFile(getImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageView.setImageBitmap(null);
                showToast("Image Uploaded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Image Upload Failed");
            }
        });
    }

    private void showDialog() {
        Dialog myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.popup);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CardView camC, galC;
        camC = myDialog.findViewById(R.id.cameraCard);
        galC = myDialog.findViewById(R.id.galCard);

        camC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activityResultLauncher.launch(open_camera);
                myDialog.dismiss();
            }
        });

        galC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent open_gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLaunchergal.launch(open_gallery);
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());

                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        address.setText(HtmlCompat.fromHtml(addresses.get(0).getAddressLine(0), HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int result = activityResult.getResultCode();
                    Intent data = activityResult.getData();
                    if (result == RESULT_OK ){
                        Uri imageuri;
                        Bundle extras = data.getExtras();
                        bitmap = (Bitmap)extras.get("data");
                        WeakReference<Bitmap> result1 = new WeakReference<>(Bitmap.createScaledBitmap(bitmap,
                                bitmap.getHeight(),bitmap.getWidth(),false).copy(
                                        Bitmap.Config.RGB_565,true));
                        Bitmap bm = result1.get();
                        imageuri=saveImage(bm,MainActivity.this);
                        imageView.setImageURI(imageuri);
                        showToast("image recieved ");
                    } else {
                        showToast("canceled ");
                    }
                }
            }
    );

    private Uri saveImage(Bitmap image, Context context) {
        File imagesFolder=new File(context.getCacheDir(),"images");
        Uri uri=null;
        try{
            imagesFolder.mkdirs();
            File file=new File(imagesFolder,"captured_image.jpg");
            FileOutputStream stream=new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context.getApplicationContext(),"com.example.garbagegoapp"+".provider",file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    ActivityResultLauncher<Intent> activityResultLaunchergal = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int result = activityResult.getResultCode();
                    Intent data = activityResult.getData();
                    if (result == RESULT_OK ){
                        try {
                            Uri imageuri;
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                            WeakReference<Bitmap> result1 = new WeakReference<>(Bitmap.createScaledBitmap(bitmap,
                                    bitmap.getHeight(),bitmap.getWidth(),false).copy(
                                    Bitmap.Config.RGB_565,true));
                            Bitmap bm = result1.get();
                            imageuri=saveImage(bm,MainActivity.this);
                            imageView.setImageURI(imageuri);
                            showToast("image recieved ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showToast("canceled ");
                    }
                }
            }
    );

}