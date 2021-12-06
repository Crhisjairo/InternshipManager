package ca.qc.bdeb.internshipmanager.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.systems.Database;

public class ModifyVisitDialog extends Dialog {

    private Context context;
    private FragmentManager fragmentManager;

    private WeekViewEvent weekViewEvent;
    private Button btnNewStartTime, btnNewEndTime, btnOk;
    private TextView tvStudentName;
    private String newStartTime, newDuringTime;

    private Internship internship;

    private Calendar startTimeCal, endTimeCal;

    private OnClickOkButtonListener onClickOkButtonListener;


    public ModifyVisitDialog(@NonNull Context context, FragmentManager fragmentManager, WeekViewEvent weekViewEvent) {
        super(context);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.weekViewEvent = weekViewEvent;

        setCurrentTime();
    }

    private void setCurrentTime() {
        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
        startTimeCal = Calendar.getInstance();
        startTimeCal.set(Calendar.HOUR_OF_DAY, weekViewEvent.getStartTime().getTime().getHours());
        startTimeCal.set(Calendar.MINUTE, weekViewEvent.getStartTime().getTime().getMinutes());

        newStartTime = formater.format(startTimeCal.getTime());

        endTimeCal = Calendar.getInstance();
        endTimeCal.set(Calendar.HOUR_OF_DAY, weekViewEvent.getEndTime().getTime().getHours());
        endTimeCal.set(Calendar.MINUTE, weekViewEvent.getEndTime().getTime().getMinutes());

        long diff = getDateDiff(startTimeCal.getTime(), endTimeCal.getTime(), TimeUnit.MINUTES);
        newDuringTime = Long.toString(diff);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_modify_layout);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER);

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentName.setText(weekViewEvent.getName());

        btnNewStartTime = findViewById(R.id.btnNewStartTime);
        btnNewStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newHour = materialTimePicker.getHour();
                        int newMinute = materialTimePicker.getMinute();
                        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
                        startTimeCal = Calendar.getInstance();

                        startTimeCal.set(Calendar.HOUR_OF_DAY, newHour);
                        startTimeCal.set(Calendar.MINUTE, newMinute);

                        newStartTime = formater.format(startTimeCal.getTime());

                        String startHourText = newHour + ":" + newMinute;
                        //on set le text du boutton
                        btnNewStartTime.setText(startHourText);
                    }
                });

                materialTimePicker.show(fragmentManager, "ABC");
            }
        });

        btnNewEndTime = findViewById(R.id.btnNewEndTime);
        btnNewEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newHour = materialTimePicker.getHour();
                        int newMinute = materialTimePicker.getMinute();
                        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
                        endTimeCal = Calendar.getInstance();

                        endTimeCal.set(Calendar.HOUR_OF_DAY, newHour);
                        endTimeCal.set(Calendar.MINUTE, newMinute);

                        long diff = getDateDiff(startTimeCal.getTime(), endTimeCal.getTime(), TimeUnit.MINUTES);

                        newDuringTime = Long.toString(diff);

                        String endHourText = newHour + ":" + newMinute;
                        //on set le text du boutton
                        btnNewEndTime.setText(endHourText);
                    }
                });

                materialTimePicker.show(fragmentManager, "ABC");
            }
            });

        btnOk = findViewById(R.id.okButton);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(onClickOkButtonListener != null){
                    onClickOkButtonListener.onClick(newStartTime, newDuringTime);
                }

                hide();
            }
        });

        //On set les données des bouttons.
        try{
            String startTimeText = weekViewEvent.getStartTime().getTime().getHours() + ":" + weekViewEvent.getStartTime().getTime().getMinutes();
            btnNewStartTime.setText(startTimeText);

            String endTimeText = weekViewEvent.getEndTime().getTime().getHours() + ":" + weekViewEvent.getEndTime().getTime().getMinutes();
            btnNewEndTime.setText(endTimeText);
        }catch (Exception e){
            Log.e("ModifyVisitDialog", "onCreate(): " + e);
        }
    }

    public interface OnClickOkButtonListener{
        public void onClick(String newStartTime, String newDuringTime);
    }

    public void setOnClickOkButtonListener(OnClickOkButtonListener onClickOkButtonListener){
        this.onClickOkButtonListener = onClickOkButtonListener;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
