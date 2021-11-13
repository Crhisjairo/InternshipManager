package ca.qc.bdeb.internshipmanager.customviews;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.systems.Database;

import java.util.ArrayList;
import java.util.Collections;

public class StagesListAdapter extends RecyclerView.Adapter<StagesListAdapter.StagesViewHolder>{
    private final ArrayList<Internship> internshipList;
    private final OnItemClickListener listener;
    private LayoutInflater inflater;

    /**
     * Crée un adaptateur avec une reférence aux liste des stages.
     * Chaque stage peut être modifié et supprimé.
     * @param context Context de l'application.
     * @param internshipList List des stages chargés.
     */
    public StagesListAdapter(Context context, ArrayList<Internship> internshipList, OnItemClickListener listener){
        inflater = LayoutInflater.from(context);
        this.internshipList = internshipList;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onEditClick(Internship item);
    }

    /**
     * Trie la liste des stages par priorité.
     * Elle met à jour le données de l'adapteur au complet.
     */
    public void sortByPriority(){
        Collections.sort(internshipList);
    }

    /**
     * Trie la liste des stages par priorité à l'inverse.
     * Elle met à jour le données de l'adapteur au complet.
     */
    public void reversePriority() {
        sortByPriority();
        Collections.reverse(internshipList);
    }

    /**
     * Trie la liste des stages par nom.
     * Elle met à jour le données de l'adapteur au complet.
     */
    public void sortName(){
        Collections.sort(internshipList, Internship.NameComparator);
    }

    /**
     * Trie la liste des stages par nom à l'inverse.
     * Elle met à jour le données de l'adapteur au complet.
     */
    public void reverseName() {
        sortName();
        Collections.reverse(internshipList);
    }


    @NonNull
    @Override
    public StagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = inflater.inflate(R.layout.stage_item, parent, false);

        return new StagesViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull StagesViewHolder holder, int position) {
        Internship currentInternship = internshipList.get(position);

        holder.setInternship(currentInternship);
        holder.bind(internshipList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return internshipList.size();
    }

    /**
     * Crée un contenant avec les information de chaque stage dans le recycler view.
     * Chaque contenant contient un Internship et les références des éléments de la view de ce contenant.
     */
    public class StagesViewHolder extends RecyclerView.ViewHolder {

        final StagesListAdapter adapter;
        private Internship intership;

        //Les views d'un item de la liste.
        private TextView tvStudentName;
        private ImageView ivPhoto;
        private FlagSelector ivItemFlagSelector;
        private ImageButton ibDelete;

        /**
         * Crée un contenant.
         * @param itemView View de l'item courrant.
         * @param adapter Adapteur courrant.
         */
        public StagesViewHolder(@NonNull View itemView, StagesListAdapter adapter){
            super(itemView);
            initViews();
            //setClickListeners(itemView.getContext());

            this.adapter = adapter;
        }


        /**
         * Initialise les views de ce contenant.
         */
        private void initViews(){
            //On récupère les reférences
            tvStudentName = (TextView) itemView.findViewById(R.id.tvItemNom);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            ivItemFlagSelector = itemView.findViewById(R.id.ivItemFlagSelector);
            ibDelete = itemView.findViewById(R.id.ibDelete);

            //On set les listenners pour les bouttons
            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmerEffacerItem(view.getContext());
                }
            });

            ivItemFlagSelector.setSelectorListener(new FlagSelector.FlagSelectorListener() {
                @Override
                public void onChangeState() {
                    intership.setPriority(ivItemFlagSelector.getPriority());

                    //On modifie le internship dans la bd.
                    Database.getInstance(itemView.getContext()).updateInternship(intership);

                    //Log.d("Icitte", "dans le flagSelector: " + ivItemFlagSelector.getPriority().toString());
                    //Log.d("Icitte", "dans le stage: " + stage.getPriority().toString());
                }
            });

        }

        /**
         * Établi la référence du stage
         * @param intership TestStage avec les données du stage
         */
        public void setInternship(Internship intership){
            this.intership = intership;

            //On donne les valeurs aux élements du holder
            tvStudentName.setText(intership.getStudentAccount().getFullName());
            ivItemFlagSelector.setPriority(intership.getPriority());
            //On donne l'id de l'image
            ivPhoto.setImageBitmap(intership.getStudentAccount().getPhoto());

            ivItemFlagSelector.setPriority(intership.getPriority());
         }


        /**
         * Crée un AlertDialog avant d'effacer le contact et son holder.
         * @param context context de l'application.
         */
        public void confirmerEffacerItem(Context context){

            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(context.getString(R.string.confirm_delete_title));
            alertDialog.setMessage(context.getString(R.string.confirm_delete_message));

            //On déclare les actions du button oui
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //On efface l'entrée
                    int index = internshipList.indexOf(intership);

                    internshipList.remove(intership);
                    Database.getInstance(itemView.getContext()).deleteInternship(intership.getIdInternship());

                    notifyItemRemoved(index);
                }
            });

            //On déclare les actions du button non
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //On ne fait rien
                }
            });

            alertDialog.show();
        }


        public void bind(final Internship internship, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onEditClick(internship);
                }
            });
        }
    }
}
