package ca.qc.bdeb.internshipmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.systems.Database;

public class Login extends AppCompatActivity {

    private Database db;

    private EditText guiUser;
    private EditText guiPassword;
    private Button guiLogin;
    private TextView guiMessage;

    private Account teacher = null;
    private String userName;
    private String userPassword;
    private int attemptsLogin = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        guiUser = findViewById(R.id.etUser);
        guiPassword= findViewById(R.id.etPassword);
        guiLogin= findViewById(R.id.btnLogin);
        guiMessage= findViewById(R.id.tvMessage);

        db = Database.getInstance(getApplicationContext());


        guiLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = guiUser.getText().toString();
                userPassword = guiPassword.getText().toString();

                if(userName.isEmpty() || userPassword.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter your information", Toast.LENGTH_SHORT).show();
                } else{
                    findUser();
                    if(teacher != null) {
                        authetifyUser();
                    } else{
                        manageFailAttemptsLogin();
                    }
                }
            }
        });
    }

    private void findUser() {
        teacher = db.getTeacherByName(userName);
    }

    private void authetifyUser() {
        if(teacher.getPassword().equals(userPassword) && teacher.getEmail().equals(userName)){
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }else{
            manageFailAttemptsLogin();
        }
    }

    private void manageFailAttemptsLogin() {
        attemptsLogin--;
        if(attemptsLogin == 0){
            guiLogin.setEnabled(false);
            guiMessage.setText("Invalid information.\nYou blocked your account.");
        } else{
            Toast.makeText(getApplicationContext(), "Invalid information", Toast.LENGTH_SHORT).show();
        }
    }
}