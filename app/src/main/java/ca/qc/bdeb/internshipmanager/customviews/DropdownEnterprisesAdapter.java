package ca.qc.bdeb.internshipmanager.customviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur pour stocker les différents entreprises dans un dropdown.
 * Chaque élément du dropdown contient une référence à l'objet de type Enterprise.
 */
public class DropdownEnterprisesAdapter extends ArrayAdapter<Enterprise> {

    private Context context;
    private List<Enterprise> enterprise;

    /**
     * Crée un adaptateur avec une liste d'entreprises.
     * @param context Contexte de l'application.
     * @param enterprises List des entreprises..
     */
    public DropdownEnterprisesAdapter(@NonNull Context context, ArrayList<Enterprise> enterprises) {
        super(context, 0, enterprises);
        this.enterprise = enterprises;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.dropdown_item_ajout_stage,parent,false);
        }

        Enterprise currentEnterprise = enterprise.get(position);

        TextView tvNom = convertView.findViewById(R.id.item_dropdown);
        tvNom.setText(enterprise.get(position).getName());

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Enterprise) resultValue).getName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            }
        };
    }

    @Override
    public int getCount() {
        return enterprise.size();
    }

}
