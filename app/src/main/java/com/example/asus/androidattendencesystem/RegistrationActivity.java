package com.example.asus.androidattendencesystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.esotericsoftware.kryo.Registration;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {
    private String catagoryname;
    private Button Addproductbtn;
    private EditText Inputname,Inputdescription,Inputprice;
    private ImageView ADDNEWIMAGE;
    private static final int gallerypick=1;
    private String description,pname,price;
    private ProgressDialog loadingbar;
    private String savecurrentdate,savecurrenttime,productrandomkey,downloadImageurl;
    private Uri Imageuri;
    private StorageReference PimageRefrence;
    private DatabaseReference Productrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        PimageRefrence= FirebaseStorage.getInstance().getReference().child("product Images");
        Productrefrence= FirebaseDatabase.getInstance().getReference().child("Products");

        loadingbar=new ProgressDialog(this);
        catagoryname=getIntent().getExtras().get("catagory").toString();
        Toast.makeText(this, catagoryname, Toast.LENGTH_SHORT).show();
        ADDNEWIMAGE=(ImageView)findViewById(R.id.select_product_image);

        Addproductbtn=(Button) findViewById(R.id.add_new_product);
        Inputname=(EditText) findViewById(R.id.product_name);
        Inputdescription=(EditText) findViewById(R.id.product_description);
        Inputprice=(EditText) findViewById(R.id.product_price);

        ADDNEWIMAGE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Opengallery();
            }
        });
        Addproductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateinformation();
            }
        });


    }

    private void validateinformation() {
        description=Inputdescription.getText().toString();
        price=Inputprice.getText().toString();
        pname=Inputname.getText().toString();
        if(Imageuri==null)
        {
            Toast.makeText(this, "please insert an image", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "please insert your description", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(price))
        {
            Toast.makeText(this, "please insert your price", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(pname))
        {
            Toast.makeText(this, "please insert your product name", Toast.LENGTH_SHORT).show();
        }
        else
        {
            savedatainformation();
        }



    }

    private void savedatainformation() {

        loadingbar.setTitle("Adding new product");
        loadingbar.setMessage("please wait untill the process is finished");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentdate=new SimpleDateFormat("MMM dd,yyyy");
        savecurrentdate=currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime=new SimpleDateFormat("HH:mm:ss a");
        savecurrenttime=currenttime.format(calendar.getTime());

        productrandomkey=savecurrentdate+savecurrenttime;
        final StorageReference filepath=PimageRefrence.child(Imageuri.getLastPathSegment()+productrandomkey+".jpg");

        final UploadTask uploadTask=filepath.putFile(Imageuri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingbar.dismiss();
                String message=e.toString();
                Toast.makeText(RegistrationActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegistrationActivity.this, "Image added successfully", Toast.LENGTH_SHORT).show();
                Task<Uri> imageuri=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        downloadImageurl=filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            downloadImageurl=task.getResult().toString();
                            Toast.makeText(RegistrationActivity.this, "Image added to storage successfully", Toast.LENGTH_SHORT).show();
                            savedataintodatabase();
                        }
                    }
                });
            }
        });


    }

    private void savedataintodatabase() {
        HashMap<String,Object> productmap=new HashMap<>();

        productmap.put("pid",productrandomkey);
        productmap.put("pname",pname);
        productmap.put("price",price);
        productmap.put("description",description);
        productmap.put("date",savecurrentdate);
        productmap.put("time",savecurrenttime);
        productmap.put("catagory",catagoryname);
        productmap.put("image",downloadImageurl);

        Productrefrence.child(productrandomkey).updateChildren(productmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    loadingbar.dismiss();
                    Toast.makeText(RegistrationActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(RegistrationActivity.this,AdminCatagory.class);
                    startActivity(intent);
                }
                else
                {
                    loadingbar.dismiss();
                    String message=task.getException().toString();
                    Toast.makeText(RegistrationActivity
                            .this, "Error"+message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void Opengallery() {

        Intent galleryintent=new Intent();
        galleryintent.setAction(Intent.ACTION_GET_CONTENT);
        galleryintent.setType("Images/*");
        startActivityForResult(galleryintent,gallerypick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==gallerypick && resultCode==RESULT_OK && data!=null)
        {
            Imageuri=data.getData();
            ADDNEWIMAGE.setImageURI(Imageuri);
        }
    }
}


