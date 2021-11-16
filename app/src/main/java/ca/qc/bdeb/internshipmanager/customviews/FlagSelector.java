package ca.qc.bdeb.internshipmanager.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;

import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.R;

/**
 * Image Button personalisé avec trois valeurs. Le boutton alterne entre trois valeurs de priorité:
 * LOW, MEDIUM, HIGH.
 */
public class FlagSelector extends androidx.appcompat.widget.AppCompatImageButton {

    private Internship.Priority priority;
    private FlagSelectorListener flagSelectorListener;

    /**
     * Crée un sélécteur de priorité.
     * @param context Context de l'application.
     * @param attrs Attributs.
     */
    public FlagSelector(Context context, AttributeSet attrs) {
        super(context, attrs);

        //setBackground(null);

        //On donne le flag sans couleur par défaut
        setImageResource(R.drawable.ic_flag);

        //On donne l'état initial. Par défault, flag verte
        setPriority(Internship.Priority.LOW);
        updateImageResource();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        //on change la valeur de priority
        int next = ((priority.ordinal() + 1) % Internship.Priority.values().length);

        setPriority(Internship.Priority.values()[next]);

        //Si jamais le listener est setted.
        if(flagSelectorListener != null){
            flagSelectorListener.onChangeState();
        }
        return true;
    }

    /**
     * Met à jour l'image selon la priorité.
     */
    private void updateImageResource() {
        switch (priority){
            case LOW:
                //setImageResource(R.drawable.ic_flag_green_24);
                setImageTintList(ColorStateList
                        .valueOf(getResources().getColor(R.color.green_flag)));
                break;
            case MEDIUM:
                setImageTintList(ColorStateList
                        .valueOf(getResources().getColor(R.color.yellow_flag)));
                break;
            case HIGH:
                setImageTintList(ColorStateList
                        .valueOf(getResources().getColor(R.color.red_flag)));

                break;
        }
        setScaleType(ScaleType.FIT_CENTER);
    }

    /**
     * Recupère la priorité actuelle du selecteur.
     * @return Priorité actuelle sélectionnée.
     */
    public Internship.Priority getPriority(){
        return priority;
    }

    /**
     * Definit la priorité du flag.
     * Utiliser lorsqu'on charge les données de la BD.
     */
    public void setPriority(Internship.Priority newPriority){
        if(newPriority == null){
            return;
        }
        priority = newPriority;

        updateImageResource();
    }

    /**
     * Permet d'ajouter d'autres actions lorsqu'on change de priorité.
     */
    public interface FlagSelectorListener {
        void onChangeState();
    }

    /**
     * Définit un listener pour ajouter d'autres actions lorsqu'on change de priorité.
     * @param flagSelectorListener
     */
    public void setSelectorListener(FlagSelectorListener flagSelectorListener) {
        this.flagSelectorListener = flagSelectorListener;
    }
}
