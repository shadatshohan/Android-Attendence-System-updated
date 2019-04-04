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
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.androidattendencesystem.Model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText Inputphone, Inputpassword;
    private Button loginbutton;
    private ProgressDialog loadingbar;
    private String parentdbname = "Users";
    private TextView adminlink, notadminlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loadingbar = new ProgressDialog(this);

        adminlink = (TextView) findViewById(R.id.admin_panel_link);
        notadminlink = (TextView) findViewById(R.id.notadmin_panel_link);

        Inputphone = (EditText) findViewById(R.id.login_phonenumberinput);
        Inputpassword = (EditText) findViewById(R.id.login_passwordinput);
        loginbutton = (Button) findViewById(R.id.login_btn);


        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginuser();
            }
        });
        adminlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginbutton.setText("Login Admin");
                adminlink.setVisibility(View.INVISIBLE);
                notadminlink.setVisibility(View.VISIBLE);
                parentdbname = "Admin";
            }
        });
        notadminlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginbutton.setText("Login");
                adminlink.setVisibility(View.VISIBLE);
                notadminlink.setVisibility(View.INVISIBLE);
                parentdbname = "Users";

            }
        });
    }

    private void loginuser() {
        String phone = Inputphone.getText().toString();
        String password = Inputpassword.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "please enter your phone number", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "please enter your password", Toast.LENGTH_SHORT).show();
        } else {
            loadingbar.setTitle("Login Account");
            loadingbar.setMessage("please wait untill the process is running");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();
            Allowaccesstoaccount(phone, password);
        }
    }

    private void Allowaccesstoaccount(final String phone, final String password) {
        final DatabaseReference Rootref;
        Rootref = FirebaseDatabase.getInstance().getReference();
        Rootref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentdbname).child(phone).exists()) {
                    Users userdata = dataSnapshot.child(parentdbname).child(phone).getValue(Users.class);
                    if (userdata.getPhone().equals(phone)) {
                        if (userdata.getPassword().equals(password)) {
                            if (parentdbname.equals("Admin")) {
                                Toast.makeText(LoginActivity.this, "Admin Logged in successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, AdminCatagory.class);
                                startActivity(intent);
                            } else if (parentdbname.equals("Users")) {
                                Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "password is incorrect", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Account with this phone number don not exists", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                        Toast.makeText(LoginActivity.this, "please open a new account", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
