package ca.qc.bdeb.internshipmanager.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.fragments.CalendarFragment;
import ca.qc.bdeb.internshipmanager.fragments.InfoFragment;
import ca.qc.bdeb.internshipmanager.fragments.ListInternshipFragment;
import ca.qc.bdeb.internshipmanager.fragments.LogoutFragment;
import ca.qc.bdeb.internshipmanager.fragments.MapsFragment;
import ca.qc.bdeb.internshipmanager.fragments.SettingsFragment;
import ca.qc.bdeb.internshipmanager.systems.Database;

public class MainActivity extends AppCompatActivity {

    private Database db;
    private ArrayList<Internship> internships;

    public static final int ACTIVITY_MODIFIER_RESULT = 1;
    public static final String INTERNSHIP_ID_TO_MODIFY_KEY = "TO_MODIFY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Database.getInstance(getApplicationContext());
        internships = db.getAllInternships();

        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        // On permet que le click ouvre et ferme le menu a cote
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //On set le ListInternshipFragment comme défaut et on séléctionne le premier élément du drawer
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ListInternshipFragment(internships)).commit();
        navigationView.getMenu().getItem(0).setChecked(true);

        // On set le comportment des clicks sur le menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                item.setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (id){
                    case R.id.nav_internship_list:
                        replaceFragment(new ListInternshipFragment(internships));
                        break;
                    case R.id.nav_map:
                        //ICI PERMISSIN D'ACCESS LOCALISATION
                        replaceFragment(new MapsFragment());
                        break;
                    case R.id.nav_calendar:
                        replaceFragment(new CalendarFragment());
                        break;
                    case R.id.nav_settings:
                        replaceFragment(new SettingsFragment());
                        break;
                    case R.id.nav_logout:
                        replaceFragment(new LogoutFragment());
                        break;
                    case R.id.nav_info:
                        replaceFragment(new InfoFragment());
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

}