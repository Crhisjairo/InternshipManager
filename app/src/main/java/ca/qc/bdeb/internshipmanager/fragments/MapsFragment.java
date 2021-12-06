package ca.qc.bdeb.internshipmanager.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import ca.qc.bdeb.internshipmanager.ConnectionValidation;
import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.dataclasses.Visit;
import ca.qc.bdeb.internshipmanager.systems.Database;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private ArrayList<Internship> internshipList;
    private ArrayList<Internship> internsToVisist = new ArrayList<>();
    private Database db;
    private GoogleMap googleMap;
    private CheckBox cbLowPriority, cbMediumPriority, cbHighPriority;
    private FloatingActionButton fabAddVisits;
    private Hashtable<String, Internship> filteredInternshipTable = new Hashtable<String, Internship>();
    private boolean allowAccessLocation;

    public MapsFragment(boolean allowAccessLocation) {
        super();
        this.allowAccessLocation = allowAccessLocation;
    }

//    private PassDataHandler passDataHandler;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap Mmap) {

            googleMap = Mmap;
            addInternsIntoMap();

            //Configuration de l'affichage de la Map
            UiSettings mapSettings = googleMap.getUiSettings();
            mapSettings.setZoomControlsEnabled(true);
            LatLng montreal = new LatLng(45.495759,-73.703628);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(montreal, 12));
            if(allowAccessLocation) {
                mapSettings.setMyLocationButtonEnabled(true);
            }
            mapSettings.setScrollGesturesEnabled(true);
        }

    };

    /**
     * Afficher l'emplacement des stagiares dans google maps
     */
    private void addInternsIntoMap() {

        //Valider la liste de stages à afficher
        verifyVisibilityInternship();

        //Parcourir chaque element de mon table pour l'afficher dans la map
        Enumeration<String> key = filteredInternshipTable.keys();
        while (key.hasMoreElements()) {
            String idInternship = key.nextElement();

            //Definir la position de mon marker
            Enterprise enterprise = filteredInternshipTable.get(idInternship).getEnterprise();
            LatLng positionInternship = getLatLngFromAddress(enterprise.getAddress());

            //Definir la couleur de mon marker
            float markerColor = 0;
            float[] redColorHue = new float[3];
            float[] greenColorHue = new float[3];
            float[] yellowColorHue = new float[3];
            Color.colorToHSV(getResources().getColor(R.color.green_flag), greenColorHue);
            Color.colorToHSV(getResources().getColor(R.color.yellow_flag), yellowColorHue);
            Color.colorToHSV(getResources().getColor(R.color.red_flag), redColorHue);

            if(filteredInternshipTable.get(idInternship).getPriority() == Internship.Priority.LOW){
                markerColor = greenColorHue[0];
            }else if(filteredInternshipTable.get(idInternship).getPriority() == Internship.Priority.MEDIUM){
                markerColor = yellowColorHue[0];
            }else if(filteredInternshipTable.get(idInternship).getPriority() == Internship.Priority.HIGH){
                markerColor = redColorHue[0];
            }

            //Afficher marker
            if(positionInternship != null) {
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(positionInternship)
                        .title(filteredInternshipTable.get(idInternship).getStudentAccount().getFullName())
                        .snippet(enterprise.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                );
                marker.setTag(idInternship);
            }
        }

        //Set le listener pour l'ensemble de markers qui sont affiches dans la map
        googleMap.setOnMarkerClickListener(this);

    }


    //TODO ajout de permissions pour le map (my position)

    /**
     * Transformer l'adresse type string dans un latitude/longitude
     * @param address l'emplacement du stage
     * @return la combinaison de la lat et long de l'adresse
     */
    private LatLng getLatLngFromAddress(String address) {
        LatLng positionStageLonLat = null;
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> listAddress = null;
            try {
                listAddress = geocoder.getFromLocationName(address, 1);
                if (listAddress != null && listAddress.size() > 0) {
                    Address addressEntreprise = listAddress.get(0);
                    positionStageLonLat = new LatLng(addressEntreprise.getLatitude(), addressEntreprise.getLongitude());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return positionStageLonLat;
    }


    /**
     *
     * @return
     */
    private Hashtable<String, Internship> verifyVisibilityInternship(){
        googleMap.clear();
        filteredInternshipTable.clear();

        for (Internship intership : internshipList) {
            if (!cbLowPriority.isChecked() && intership.getPriority() == Internship.Priority.LOW) {
                filteredInternshipTable.put(intership.getIdInternship(), intership);
            }

            if (!cbMediumPriority.isChecked() && intership.getPriority() == Internship.Priority.MEDIUM) {
                filteredInternshipTable.put(intership.getIdInternship(), intership);
            }

            if (!cbHighPriority.isChecked() && intership.getPriority() == Internship.Priority.HIGH) {
                filteredInternshipTable.put(intership.getIdInternship(), intership);
            }
        }

        return filteredInternshipTable;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {



        return inflater.inflate(R.layout.fragment_maps, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = Database.getInstance(getContext());
        internshipList = db.getInternshipFromOneTeacher(ConnectionValidation.authId);

        //Checkboxes pour faire le filtrage des stages
        cbLowPriority = view.findViewById(R.id.cbLowPriority);
        cbMediumPriority = view.findViewById(R.id.cbMediumPriority);
        cbHighPriority = view.findViewById(R.id.cbHighPriority);
        setCheckboxesListener();

        fabAddVisits = view.findViewById(R.id.fabAddVisits);
        fabAddVisits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddVisit();
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    /**
     * Comportement du filtre des stages par rapport aux drapeaux de priorité.
     * Les stages vont se cacher en fonction des drapeaux cochées.
     * @param view View qui fait appelle à la méthode.
     */
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        CheckBox flag = view.findViewById(view.getId());
        //On définit le drawable en fonction s'il est clické ou pas
        if(checked) {
            flag.setButtonDrawable(R.drawable.ic_flag_unclick);
        }
        else{
            flag.setButtonDrawable(R.drawable.ic_flag);
        }
        addInternsIntoMap();
    }

    /**
     * Définit les listeners pour les checkboxes pour faire le filtrage des stages.
     */
    private void setCheckboxesListener() {
        cbLowPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckboxClicked(view);
            }
        });

        cbMediumPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckboxClicked(view);
            }
        });

        cbHighPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCheckboxClicked(view);
            }
        });
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        Internship intern = filteredInternshipTable.get(marker.getTag());

        if(internsToVisist.contains(intern)){
            internsToVisist.remove(intern);
        } else{
            internsToVisist.add(intern);
        }

        Log.d("ICITTE", "onMarkerClick: " + intern.getStudentAccount().getFullName());
        Log.d("ICITTE", "Taille: " + internsToVisist.size());

        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    public void onClickAddVisit(){
        ArrayList<Visit> visitsToInsert = new ArrayList<>();
        boolean someVisitsNotAdded = false;

        //On ajoute les visites selectionnées. Le calendrier va s'encharger d'adapter les dates.
        for (Internship internship : internsToVisist) {
            //On crée une visite pour l'internship courrant. On format les dates.
            // Les marges entre les heures se verifient dans le calendrier.
            ArrayList<Visit> visits = Visit.createVisitsFromIntership(internship); //On a 3 visites pour le stage

            if(visits.isEmpty()){
                //Message pour dire que le stage ne possède pas des journées de stage
                showAlertDialog(getContext().getString(R.string.visitNotAddedTitle),
                        getContext().getString(R.string.internshipWithoutDays));
            }

            //On check que le stage ne possède pas plus de 3 visites et qu'il ne possède plus d'une
            //visite par journée, si jamais c'est le cas, on affiche un popup pour aviser l'utilisateur.
            //Ces 3 visites sont forcés à être en journées séparées.
            for (Visit visit : visits) {
                //Pour chaque visite, on check si une visite exite avec la même journée.
                if(Visit.containsVisitAtSameDay(internship, visit)){
                    someVisitsNotAdded = true;
                    continue;
                }

                visitsToInsert.add(visit);
            }

        }

        //On affiche un popup si jamais des visites n'ont pas été ajoutés.
        if(someVisitsNotAdded){
            showAlertDialog(getContext().getString(R.string.visitNotAddedTitle),
                    getContext().getString(R.string.someVisitsAtSameDay));
        }

        if(!visitsToInsert.isEmpty()){
            db.insertVisits(visitsToInsert);
        }

        internsToVisist.clear();
    }


    private void showAlertDialog(String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        //On déclare les actions du button oui
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getContext().getString(R.string.okText), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }


}