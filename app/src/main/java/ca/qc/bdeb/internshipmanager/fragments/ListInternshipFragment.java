package ca.qc.bdeb.internshipmanager.fragments;

import static android.app.Activity.RESULT_OK;

import static ca.qc.bdeb.internshipmanager.activities.MainActivity.INTERNSHIP_ID_TO_MODIFY_KEY;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.activities.InternshipManagementActivity;
import ca.qc.bdeb.internshipmanager.customviews.StagesListAdapter;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.systems.Database;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListInternshipFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListInternshipFragment extends Fragment {

    public static final int ACTIVITY_MODIFIER_RESULT = 1;
    public static final String ID_INTERNSHIP_MODIFY_KEY = "TO_MODIFY";

    private RecyclerView rvListeStages;
    private StagesListAdapter stagesListAdapter;
    private ArrayList<Internship> internships = new ArrayList<>();

    private FloatingActionButton fabAddInternship;

    private Database db;

    public ListInternshipFragment(ArrayList<Internship> internships) {
        // Required empty public constructor
        this.internships = internships;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListInternshipFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListInternshipFragment newInstance(ArrayList<Internship> internships) {
        ListInternshipFragment fragment = new ListInternshipFragment(internships);


        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        db = Database.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_internship, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabAddInternship = view.findViewById(R.id.fabAddInternship);
        fabAddInternship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddInternship();
            }
        });

        //RecyclerView avec adapter
        rvListeStages = view.findViewById(R.id.rvListeStage); //Initialise le recyclerView

        stagesListAdapter = createRecyclerViewAdapter(internships);

        rvListeStages.setAdapter(stagesListAdapter);
        rvListeStages.setLayoutManager(new LinearLayoutManager(getContext())); //On donne au RecyclerView un layout par défaut
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Icitte", "dans le if");
        if(requestCode == ACTIVITY_MODIFIER_RESULT){

            if((resultCode == RESULT_OK) && (data != null)){
                //On met à jour la liste
                Log.d("Info", "Actualiser");
                filterListByCheckboxes();
            }
        }
    }

    /**
     * Crée un adapteur avec la liste d'internships qu'on lui passe.
     * @param listInternship Liste d'internship qui contiendra l'adaptateur.
     * @return StageListAdapter avec la liste passé en paramètre.
     */
    private StagesListAdapter createRecyclerViewAdapter(ArrayList<Internship> listInternship){
        //On définit le listener de chaque item de la liste.
        StagesListAdapter.OnItemClickListener listener = new StagesListAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Internship internship) {
                Toast.makeText(getContext(), "Modifier stage", Toast.LENGTH_SHORT).show();
                onClickModifyStage(internship);
            }
        };

        return new StagesListAdapter(getContext(), listInternship, listener);
    }

    /**
     * Initialise l'activité qui permet d'ajouter un nouveau stage.
     */
    private void onClickAddInternship(){
        Intent intent = new Intent(getContext(), InternshipManagementActivity.class);
        intent.putExtra(INTERNSHIP_ID_TO_MODIFY_KEY, "");

        startActivityForResult(intent, ACTIVITY_MODIFIER_RESULT);
    }

    /**
     *
     * @param internshipToModify
     */
    private void onClickModifyStage(Internship internshipToModify){
        //On démarre l'activité pour modifier l'internship
        Intent intent = new Intent(getContext(), InternshipManagementActivity.class);
        intent.putExtra(ID_INTERNSHIP_MODIFY_KEY, internshipToModify.getIdInternship()); //on passe l'id de l'internship à modifier

        startActivityForResult(intent, ACTIVITY_MODIFIER_RESULT);
    }

    /**
     * Recrée un adaptateur pour le recycler view en fontion des stages qui peuvent être affichés.
     * On ajoute dans une liste les stages qui peuvent être affichés et on crée un adaptateur avec
     * celle-ci.
     */
    public void filterListByCheckboxes(){
        ArrayList<Internship> filterInternships = new ArrayList<>();
        filterInternships.clear();

        internships = db.getAllInternships();

        for (Internship intership : internships) {
            /*
            if(!icon_check_green_flag.isChecked() && intership.getPriority() == Internship.Priority.LOW){
                filterInternships.add(intership);
            }

            if(!icon_check_yellow_flag.isChecked() && intership.getPriority() == Internship.Priority.MEDIUM){
                filterInternships.add(intership);
            }

            if(!icon_check_red_flag.isChecked() && intership.getPriority() == Internship.Priority.HIGH){
                filterInternships.add(intership);
            }
            */

        }

        //TODO faire avec filterInternships
        stagesListAdapter = createRecyclerViewAdapter(internships);

        rvListeStages.setAdapter(stagesListAdapter);
    }

}