package ca.qc.bdeb.internshipmanager.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.customviews.DropdownEnterprisesAdapter;
import ca.qc.bdeb.internshipmanager.customviews.DropdownStudentsAdapter;
import ca.qc.bdeb.internshipmanager.customviews.FlagSelector;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.dataclasses.Visit;
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

    private Button btn_add_internship, btnModifyStartInternTime, btnModifyEndInternTime,
            btnModifyStartLunchTime, btnModifyEndLunchTime;

    private CheckBox cbWedInternDay, cbThuInternDay, cbFriInternDay, cbWedAMTutorAvail,
            cbThuAMTutorAvail, cbFriAMTutorAvail, cbWedPMTutorAvail,
            cbThuPMTutorAvail, cbFriPMTutorAvail;

    private RadioGroup rgAverageDuration;

    private EditText etComments;

    private int visitAvgDurationMins;

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
        setViewsData();

        String internshipIdToModify = getIntent().getStringExtra(MainActivity.INTERNSHIP_ID_TO_MODIFY_KEY);
        //On check s'il s'agit d'une modification d'un internship
        if(internshipIdToModify != null && !internshipIdToModify.equals("")){
            setInternshipToModifyData(internshipIdToModify);
        }

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

        //Références des listes dropdown
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

        //Références des views contenant informations des stages

        //Journées de stage
        cbWedInternDay = (CheckBox) findViewById(R.id.cbWedInternDay);
        cbThuInternDay = (CheckBox) findViewById(R.id.cbThuInternDay);
        cbFriInternDay = (CheckBox) findViewById(R.id.cbFriInternDay);

        //Stage schedule
        btnModifyStartInternTime = (Button) findViewById(R.id.btnModifyStartInternTime);
        btnModifyEndInternTime = (Button) findViewById(R.id.btnModifyEndInternTime);
        //Diner schedule
        btnModifyStartLunchTime = (Button) findViewById(R.id.btnModifyStartLunchTime);
        btnModifyEndLunchTime = (Button) findViewById(R.id.btnModifyEndLunchTime);

        //Durée de la visite
        rgAverageDuration = (RadioGroup) findViewById(R.id.rgAverageDuration);
        rgAverageDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch(i) {
                    case R.id.rb30Minutes:
                        visitAvgDurationMins = 30;
                        break;
                    case R.id.rb45Minutes:
                        visitAvgDurationMins = 45;
                        break;
                    case R.id.rb1Hour:
                        visitAvgDurationMins = 60;
                        break;
                }
            }
        });

        //Disponibilité des tuteurs pour le stage
        cbWedAMTutorAvail = (CheckBox) findViewById(R.id.cbWedAMTutorAvail);
        cbThuAMTutorAvail = (CheckBox) findViewById(R.id.cbThuAMTutorAvail);
        cbFriAMTutorAvail = (CheckBox) findViewById(R.id.cbFriAMTutorAvail);

        cbWedPMTutorAvail = (CheckBox) findViewById(R.id.cbWedPMTutorAvail);
        cbThuPMTutorAvail = (CheckBox) findViewById(R.id.cbThuPMTutorAvail);
        cbFriPMTutorAvail = (CheckBox) findViewById(R.id.cbFriPMTutorAvail);

        etComments = (EditText) findViewById(R.id.etComments);

        //On ajoute les text fields pour vérifier s'ils sont vides avant l'ajout du stage.
        inputFields = new ArrayList<>();

        inputFields.add(til_list_etudiants);
        inputFields.add(til_list_enterprises);
    }

    /**
     * Initialise et définit les données des views dans l'activité.
     * Les données des différents dropdowns sont définis icitte.
     */
    private void setViewsData() {
        //On set la liste des étudiants
        list_etudiants = new ArrayList<>();

        for (Account account : db.getStudentsAccount()) {
            list_etudiants.add(account);

        }

        arrayAdapter_etudiants = new DropdownStudentsAdapter(getApplicationContext(), list_etudiants);
        actv_list_etudiants.setAdapter(arrayAdapter_etudiants);

        //On set la liste des entreprises
        list_enterprises = new ArrayList<>();

        for (Enterprise enterprise : db.getEntreprises()) {
            list_enterprises.add(enterprise);

        }

        arrayAdapter_enterprises = new DropdownEnterprisesAdapter(getApplicationContext(), list_enterprises);
        actv_list_enterprises.setAdapter(arrayAdapter_enterprises);


        //On set la liste des années
        //TODO peut-être il faudra changer ça
        et_list_annee_scolaire.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

    }

    /**
     * On définit les données du l'internship à modifier.
     */
    private void setInternshipToModifyData(String idIntenship) {
        internshipToModify = db.getInternshipById(idIntenship);
        //Log.d("Icitte", internshipToModify.toString());

        //On définit le titre de la page
        getSupportActionBar().setTitle(R.string.title_modify_internship);

        //On définit l'image du compte
        ivImageEtudiant.setImageBitmap(internshipToModify.getStudentAccount().getPhoto());

        //On définit l'année scolaire
        et_list_annee_scolaire.setText(internshipToModify.getSchoolYear());

        //On définit le nom de l'entreprise et l'entreprise selected
        actv_list_enterprises.setText(internshipToModify.getEnterprise().getName());
        selectedEnterprise = internshipToModify.getEnterprise();
        setEntrepriseViewsData(selectedEnterprise);

        //On définit le nom de l'étudiant et l'étudiant sélected.
        actv_list_etudiants.setText(internshipToModify.getStudentAccount().getFullName(), false);
        selectedStudent = internshipToModify.getStudentAccount();

        //On définit la priorité
        ib_new_flagSelector.setPriority(internshipToModify.getPriority());

        //On définit le text du boutton
        btn_add_internship.setText(getString(R.string.modify_internship_text));
    }


    private void setEntrepriseViewsData(Enterprise enterprise){
        et_adresse_stage.setText(enterprise.getAddress());
        et_ville_stage.setText(enterprise.getTown());
        et_code_postal_stage.setText(enterprise.getPostalCode());
        et_province_stage.setText(enterprise.getProvince());
    }

    public void onClickModifyPhoto(View view){
        //On demande la permission pour la camd'abord
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_CAMERA_CODE);
    }

    /**
     * Ajoute un nouveau stage en fonction des informations founies.
     * @param view View qui fait appelle à la méthode.
     */
    public void onClickAjouterStage(View view){
        boolean areFieldsFull = true;

        //On vérifie que les fields ne soient pas vides.
        for (TextInputLayout textField : inputFields) {
            if(textField.getEditText().getText().toString().equals("") || textField.getEditText().getText().toString().equals(null)){
                textField.setErrorEnabled(true);
                textField.setError("Vide!");
                areFieldsFull = false;
            } else {
                textField.setErrorEnabled(false);
            }
        }

        if(areFieldsFull){

            //On check s'il faut modifier un internship ou crée un nouveau.
            if(!getIntent().getStringExtra(MainActivity.INTERNSHIP_ID_TO_MODIFY_KEY).equals("")){
                modifyInternship();
            } else{
                createNewInternship();
            }
        }
    }

    private void modifyInternship() {
        if(internshipToModify == null){
            Log.d("Info", "Érreur pour modifier l'internship, Il est null");
            return;
        }

        //Photo du internship
        Bitmap photo = ((BitmapDrawable) ivImageEtudiant.getDrawable()).getBitmap();

        internshipToModify.getStudentAccount().setPhoto(photo);
        internshipToModify.setSchoolYear(et_list_annee_scolaire.getText().toString());
        internshipToModify.setEnterprise(selectedEnterprise);
        internshipToModify.setAccountStudent(selectedStudent);
        internshipToModify.setPriority(ib_new_flagSelector.getPriority());
        //Le même prof reste pour le même internship
        //TODO il faut peut-être modifier la liste des visits aussi. Pour le même, on le change pas
        //internshipToModify.setVisitList();

        //On modifie le compte étudiant avec sa photo
        db.updateAccount(internshipToModify.getStudentAccount());

        //On le modifie dans la bd
        db.updateInternship(internshipToModify);

        returnToPreviousActivity();
    }


    /**
     * Recupère les données dans les field correspondants pour créer un internship.
     */
    private void createNewInternship() {
        //Photo du compte associé au internship
        Bitmap studentPhoto = ((BitmapDrawable) ivImageEtudiant.getDrawable()).getBitmap();

        //Info du Internship
        String anneeScolaire = et_list_annee_scolaire.getText().toString();
        //Info du l'enterprise
        Enterprise entreprise = selectedEnterprise;
        //Account Student
        Account student = selectedStudent;
        //TODO on recupère le seul teacher qui doit exister. Le InternshipSystem va retourner le teacher en fonction du Log In plus tard.
        Account teacher = db.getCurrentTeacherAccount();


        //TODO il faut ajouter icitte les visits en fonctions des données fournis
        //Visit list
        ArrayList<Visit> visitList = new ArrayList<>();
        //visitList.add(new Visit(idInternship, ));


        //On modifie le compte étudiant avec sa nouvelle photo
        student.setPhoto(studentPhoto);
        db.updateAccount(student);

        //On l'ajoute à la BD
        db.insertInternship(anneeScolaire, entreprise.getEnterpriseId(), student.getAccountId(),
                teacher.getAccountId(), visitList,ib_new_flagSelector.getPriority());

        //On retourne à l'activité précedente après avoir insérer le nouveau étudiant
        returnToPreviousActivity();
    }

    /**
     * Permet de retourner à l'activité précédente
     */
    private void returnToPreviousActivity() {
        Intent intentMessage = new Intent();

        setResult(RESULT_OK, intentMessage);
        finish();
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