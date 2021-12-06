package ca.qc.bdeb.internshipmanager.fragments;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.activities.InternshipManagementActivity;
import ca.qc.bdeb.internshipmanager.customviews.ModifyVisitDialog;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.dataclasses.Visit;
import ca.qc.bdeb.internshipmanager.systems.Database;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    public static final int ACTIVITY_MODIFIER_RESULT = 1;
    public static final String ID_INTERNSHIP_MODIFY_KEY = "TO_MODIFY";

    private WeekView weekView;
    private Database db;

    private ArrayList<Visit> visits;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = Database.getInstance(getContext());
        weekView = view.findViewById(R.id.weekView);

        visits = db.getAllVisits();
        visits = Visit.adaptVisitDates(visits);

        db.updateVisits(visits);

        //Pour passer les events à la création du calendar
        weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Nullable
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int i, int i1) {
                List<WeekViewEvent> events = createEventsFromVisits(i, i1);
                return events;
            }
        });

        weekView.setEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(@NonNull WeekViewEvent weekViewEvent, @NonNull RectF rectF) {
                //Toast.makeText(getContext(), "icitte", Toast.LENGTH_SHORT).show();
                //On montre un dialog pour modier le WeekViewEvent
                ModifyVisitDialog modifyVisitDialog = new ModifyVisitDialog(getContext(), getParentFragmentManager(), weekViewEvent);
                modifyVisitDialog.setOnClickOkButtonListener(new ModifyVisitDialog.OnClickOkButtonListener() {
                    @Override
                    public void onClickOk(String newStartTime, String newDuringTime) {

                        for (Visit visit : visits) {
                            if(visit.getVisitId().equals(weekViewEvent.getId())){
                                visit.setStartHour(newStartTime);
                                visit.setDuring(newDuringTime);

                                db.updateVisitById(visit);
                                visits = db.getAllVisits();
                                weekView.notifyDataSetChanged();
                                break;
                            }
                        }

                    }

                    @Override
                    public void onClickModifyIntenship(Internship internship) {
                        //On démarre l'activité pour modifier l'internship
                        Intent intent = new Intent(getContext(), InternshipManagementActivity.class);
                        intent.putExtra(ID_INTERNSHIP_MODIFY_KEY, internship.getIdInternship()); //on passe l'id de l'internship à modifier

                        startActivityForResult(intent, ACTIVITY_MODIFIER_RESULT);
                    }
                });

                modifyVisitDialog.show();
            }
        });

    }

    private List<WeekViewEvent> createEventsFromVisits(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        Date startDate, day;

        try{

            for (Visit visit : visits) {
                Calendar startTime = Calendar.getInstance();
                startDate = new SimpleDateFormat("HH:mm:ss").parse(visit.getStartHour());
                day = new SimpleDateFormat("yyyy-MM-dd").parse(visit.getVisitDate());

                startTime.set(Calendar.HOUR_OF_DAY, startDate.getHours());
                startTime.set(Calendar.MINUTE, startDate.getMinutes());
                startTime.set(Calendar.DAY_OF_MONTH, day.getDay() + 5); //Le +5 c'est à cause de la librairie
                startTime.set(Calendar.MONTH, newMonth - 1);
                startTime.set(Calendar.YEAR, newYear);

                //On calcule l'heure de fin en fonction du during de la visite
                Calendar endTime = (Calendar) startTime.clone();
                endTime.add(Calendar.MINUTE, Integer.parseInt(visit.getDuring()));

                Internship internship = db.queryForInternshipId(visit.getInternshipId());

                WeekViewEvent event = new WeekViewEvent(visit.getVisitId(),
                        getEventTitle(internship, startTime), startTime, endTime);
                //On donne la couleur de la priorité.
                event.setColor(getResources().getColor(internship.getPriorityColorRessourceId()));
                events.add(event);
            }

        }catch (Exception e){
            Log.e("Calendar:", "createEventsFromVisits() " + e);
        }

        return events;
    }

    protected String getEventTitle(Internship internship, Calendar time) {
        String fullName = internship.getStudentAccount().getFullName();


        return String.format(getString(R.string.visitFor) + " " + fullName + " : %02d:%02d %s/%d",
                time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

}