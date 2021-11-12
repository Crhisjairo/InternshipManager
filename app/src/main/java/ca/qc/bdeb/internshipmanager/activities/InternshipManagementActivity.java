package ca.qc.bdeb.internshipmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.systems.Database;

public class InternshipManagementActivity extends AppCompatActivity {

    private Database db;
    public static final int ACTIVITY_MODIFIER_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internship_management);

        db = Database.getInstance(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_MODIFIER_RESULT){
            if((resultCode == RESULT_OK) && (data != null)){
                //On met à jour la liste
                Log.d("Info", "Actualiser");
                //filterListByCheckboxes();
            }
        }

    }

}