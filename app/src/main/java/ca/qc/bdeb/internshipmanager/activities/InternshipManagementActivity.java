package ca.qc.bdeb.internshipmanager.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.customviews.DropdownEnterprisesAdapter;
import ca.qc.bdeb.internshipmanager.customviews.DropdownStudentsAdapter;
import ca.qc.bdeb.internshipmanager.customviews.FlagSelector;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.systems.Database;
import de.hdodenhof.circleimageview.CircleImageView;

public class InternshipManagementActivity extends AppCompatActivity {

    private Database db;

    public static final int INTENT_PHOTO_RESULT = 1;
    public static final int PERMISSION_CAMERA_CODE = 1;

    private ArrayList<TextInputLayout> inputFields;

    private CircleImageView ivImageEtudiant;
    private FloatingActionButton btnEditPhoto;

    private FlagSelector ib_new_flagSelector;

    private TextInputLayout til_list_etudiants, til_list_enterprises;

    private EditText et_adresse_stage,
            et_ville_stage, et_code_postal_stage, et_province_stage, et_list_annee_scolaire;

    private AutoCompleteTextView actv_list_etudiants, actv_list_enterprises;

    private Button btn_add_internship;

    private ArrayList<Account> list_etudiants;
    private ArrayList<Enterprise> list_enterprises;

    private DropdownStudentsAdapter arrayAdapter_etudiants;
    private DropdownEnterprisesAdapter arrayAdapter_enterprises;

    private Account selectedStudent;
    private Enterprise selectedEnterprise;

    private Internship internshipToModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internship_management);

        db = Database.getInstance(getApplicationContext());

        initViews();
    }

    private void initViews() {
        //On définit la toolbar comme back arrow
        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Photo de profil
        ivImageEtudiant = (CircleImageView) findViewById(R.id.ivImageEtudiant);
        btnEditPhoto = (FloatingActionButton) findViewById(R.id.btnEditPhoto);

        ib_new_flagSelector = (FlagSelector) findViewById(R.id.ib_new_flagSelector); //Pour la priorité

        //Références des listes
        //LIST ÉTUDIANTS
        til_list_etudiants = (TextInputLayout) findViewById(R.id.til_list_etudiants);
        actv_list_etudiants = (AutoCompleteTextView) findViewById(R.id.actv_list_etudiants);
        actv_list_etudiants.setThreshold(1);
        actv_list_etudiants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedStudent = (Account) adapterView.getItemAtPosition(i);

                //On définit l'image du compte
                ivImageEtudiant.setImageBitmap(selectedStudent.getPhoto());

                Log.d("INFO", selectedStudent.toString() + "");
            }
        });

        /**
        //LIST ENTERPRISES
        til_list_enterprises = (TextInputLayout) findViewById(R.id.til_list_nom_enterprises);
        actv_list_enterprises = (AutoCompleteTextView) findViewById(R.id.actv_list_nom_enterprises);
        actv_list_enterprises.setThreshold(1);
        actv_list_enterprises.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEnterprise = (Enterprise) adapterView.getItemAtPosition(i);
                //On set les informations de l'entreprise dans les fields pas modifiables
                setEntrepriseViewsData(selectedEnterprise);

                Log.d("Info", selectedEnterprise.toString() + "");
            }
        });

        //LIST ANNÉE SCOLAIRE
        et_list_annee_scolaire = (EditText) findViewById(R.id.et_annee_scolaire);

        //Références des autres fields
        et_adresse_stage = (EditText) findViewById(R.id.et_adresse_stage);
        et_ville_stage = (EditText) findViewById(R.id.et_ville_stage);
        et_code_postal_stage = (EditText) findViewById(R.id.et_code_postal_stage);
        et_province_stage = (EditText) findViewById(R.id.et_province_stage);

        btn_add_internship = (Button) findViewById(R.id.btn_add_internship);

        //On ajoute les text fields pour vérifier s'ils sont vides avant l'ajout du stage.
        inputFields = new ArrayList<>();

        inputFields.add(til_list_etudiants);
        inputFields.add(til_list_enterprises);

         */
    }

    public void onClickModifyPhoto(View view){
        //On demande la permission pour la camd'abord
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_CAMERA_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_CAMERA_CODE:
                //On check si on a la permission ou pas
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //On lance l'intent pour recupérer la photo
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, INTENT_PHOTO_RESULT);

                }else{
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == INTENT_PHOTO_RESULT){

            if(data == null){
                return;
            }

            //Recupère la photo
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            //On le définit dans l'ImageView
            ivImageEtudiant.setImageBitmap(photo);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}