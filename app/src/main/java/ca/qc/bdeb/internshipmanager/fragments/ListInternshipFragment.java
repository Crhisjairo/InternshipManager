package ca.qc.bdeb.internshipmanager.fragments;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListInternshipFragment(ArrayList<Internship> internships) {
        // Required empty public constructor
        this.internships = internships;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListInternshipFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListInternshipFragment newInstance(ArrayList<Internship> internships, String param1, String param2) {
        ListInternshipFragment fragment = new ListInternshipFragment(internships);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


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

        //RecyclerView avec adapter
        rvListeStages = view.findViewById(R.id.rvListeStage); //Initialise le recyclerView
        /*
        ArrayList<Internship> inter = new ArrayList<>();
        inter.add(internships.get(0));
        inter.add(internships.get(1));
        inter.add(internships.get(2));
        inter.add(internships.get(3));
        */

        stagesListAdapter = createRecyclerViewAdapter(internships);

        //rvListeStages.setAdapter(stagesListAdapter);
        //rvListeStages.setLayoutManager(new LinearLayoutManager(getContext())); //On donne au RecyclerView un layout par défaut
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
                //On démarre l'activité pour modifier l'internship
                Intent intent = new Intent(getContext(), InternshipManagementActivity.class);
                intent.putExtra(ID_INTERNSHIP_MODIFY_KEY, internship.getIdInternship()); //on passe l'id de l'internship à modifier

                startActivityForResult(intent, ACTIVITY_MODIFIER_RESULT);
            }
        };

        return new StagesListAdapter(getContext(), listInternship, listener);
    }

    /**
     *
     * @param internshipToModify
     */
    private void onClickModifyStage(Internship internshipToModify){
        Intent intent = new Intent(getContext(), InternshipManagementActivity.class);
        intent.putExtra(ID_INTERNSHIP_MODIFY_KEY, internshipToModify.getIdInternship());

        startActivityForResult(intent, ACTIVITY_MODIFIER_RESULT);
    }

}