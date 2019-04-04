package com.example.asus.androidattendencesystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText Inputname,Inputphone,Inputpassword;
    private Button createaccountbutton;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loadingbar=new ProgressDialog(this);

        Inputname=(EditText) findViewById(R.id.register_nameinput);
        Inputphone=(EditText) findViewById(R.id.register_phonenumberinput);
        Inputpassword=(EditText) findViewById(R.id.register_passwordinput);

        createaccountbutton=(Button) findViewById(R.id.register_btn);

        createaccountbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void createAccount() {

        String name=Inputname.getText().toString();
        String phone=Inputphone.getText().toString();
        String password=Inputpassword.getText().toString();

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "please enter your name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(this, "please enter your phone number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "please enter your password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Create Account");
            loadingbar.setMessage("Please wait for sometime");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();
            validatephone(name,phone,password);
        }
    }

    private void validatephone(final String name, final String phone, final String password) {
        final DatabaseReference Rootref;
        Rootref= FirebaseDatabase.getInstance().getReference();
        Rootref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("Users").child(phone).exists()))
                {
                    HashMap<String,Object> userdataMap=new HashMap<>();
                    userdataMap.put("phone",phone);
                    userdataMap.put("name",name);
                    userdataMap.put("password",password);
                    Rootref.child("Users").child(phone).updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(RegisterActivity.this, "Data add is successful", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                                startActivity(intent);
                            }
                            else
                            {
                                loadingbar.dismiss();
                                Toast.makeText(RegisterActivity.this, "Network error!please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "this"+phone+"number already exists", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                    Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
