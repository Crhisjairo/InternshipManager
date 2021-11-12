package ca.qc.bdeb.internshipmanager.customviews;

import android.content.Context;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur pour stocker les différents comptes d'étudiants dans un dropdown.
 * Chaque élément du dropdown contient une référence à l'objet de type Account.
 */
public class DropdownStudentsAdapter extends ArrayAdapter<Account> {

    private Context context;
    private List<Account> studentAccounts;

    /**
     * Crée un adaptateur avec une liste des comptes (de type étudiants).
     * @param context Contexte de l'application.
     * @param studentsAccount List des comptes de type étudiants.
     */
    public DropdownStudentsAdapter(@NonNull Context context, ArrayList<Account> studentsAccount) {
        super(context, 0, studentsAccount);
        this.studentAccounts = studentsAccount;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.dropdown_item_ajout_stage,parent,false);
        }

        Account currentStudent = studentAccounts.get(position);

        TextView tvNom = convertView.findViewById(R.id.item_dropdown);
        tvNom.setText(studentAccounts.get(position).getFullName());

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Account) resultValue).getFullName();
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
        return studentAccounts.size();
    }

    /**
     * On sélectionne l'étudiant passé en paramètres s'il existe dans l'adaptateur.
     * @param studentAccount
     */
    public void setAccountSelected(Account studentAccount) {
        for (int i = 0; i < studentAccounts.size(); i++) {
            if(studentAccounts.get(i).getAccountId() == studentAccount.getAccountId()){
                Log.d("Icitte", "Étudiant match!");
            }
        }
    }
}
