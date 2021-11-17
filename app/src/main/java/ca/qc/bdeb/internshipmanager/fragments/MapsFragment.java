package ca.qc.bdeb.internshipmanager.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.systems.Database;

public class MapsFragment extends Fragment {

    private ArrayList<Internship> internshipList;
    private Database db;
    private GoogleMap googleMap;
    private CheckBox cbLowPriority, cbMediumPriority, cbHighPriority;

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
            addInternsIntoMap(filterMarkPoints());

            //TODO Faire les UI contros fonctionner
            //Configuration de l'affichage de la Map
            UiSettings mapSettings = googleMap.getUiSettings();
            mapSettings.setZoomControlsEnabled(true);
            mapSettings.setMyLocationButtonEnabled(true);
            mapSettings.setScrollGesturesEnabled(true);
            LatLng montreal = new LatLng(45.495759,-73.703628);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(montreal, 12));
        }

    };

    /**
     * Afficher l'emplacement des stagiares dans google maps
     */
    private void addInternsIntoMap(ArrayList<Internship> filteredInternshipList) {
        for (Internship internship: filteredInternshipList) {

            LatLng positionInternship = getLatLngFromAddress(internship.getEnterprise().getAddress());

            float markerColor = 0;
            float[] redColorHue = new float[3];
            float[] greenColorHue = new float[3];
            float[] yellowColorHue = new float[3];
            Color.colorToHSV(getResources().getColor(R.color.green_flag), greenColorHue);
            Color.colorToHSV(getResources().getColor(R.color.yellow_flag), yellowColorHue);
            Color.colorToHSV(getResources().getColor(R.color.red_flag), redColorHue);

            if(internship.getPriority() == Internship.Priority.LOW){
                markerColor = greenColorHue[0];
            }else if(internship.getPriority() == Internship.Priority.MEDIUM){
                markerColor = yellowColorHue[0];
            }else if(internship.getPriority() == Internship.Priority.HIGH){
                markerColor = redColorHue[0];
            }

            googleMap.addMarker(new MarkerOptions()
                    .position(positionInternship)
                    .title(internship.getStudentAccount().getFullName())
                    .snippet(internship.getEnterprise().getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            );
        }
    }


    //TODO ajout de permissions pour le map (my position)
    
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

    private ArrayList<Internship> filterMarkPoints(){
        googleMap.clear();

        ArrayList<Internship> filteredInternships = new ArrayList<>();
        for (Internship intership : internshipList) {
            if (!cbLowPriority.isChecked() && intership.getPriority() == Internship.Priority.LOW) {
                filteredInternships.add(intership);
            }

            if (!cbMediumPriority.isChecked() && intership.getPriority() == Internship.Priority.MEDIUM) {
                filteredInternships.add(intership);
            }

            if (!cbHighPriority.isChecked() && intership.getPriority() == Internship.Priority.HIGH) {
                filteredInternships.add(intership);
            }
        }

        return filteredInternships;
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
        internshipList = db.getAllInternships();

        //Checkboxes pour faire le filtrage des stages
        cbLowPriority = view.findViewById(R.id.cbLowPriority);
        cbMediumPriority = view.findViewById(R.id.cbMediumPriority);
        cbHighPriority = view.findViewById(R.id.cbHighPriority);
        setCheckboxesListener();

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
        addInternsIntoMap(filterMarkPoints());
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


}