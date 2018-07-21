package com.example.ajay0.contacts;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private Contact[] contacts ;
    private ArrayAdapter<Contact> listAdapter ;
    int totalContactsCount = 0;
    boolean createVcfClicked = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            onCreate2(savedInstanceState);
        }

    }

    public void onCreate2(Bundle savedInstanceState){

        // Find the ListView resource.
        ListView mainListView = (ListView) findViewById( R.id.mainListView );

        // When item is tapped, toggle checked properties of CheckBox and Contact.
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View item, int position, long id) {
                Contact contact = listAdapter.getItem( position );
                contact.toggleChecked();
                ContactViewHolder viewHolder = (ContactViewHolder) item.getTag();
                viewHolder.getCheckBox().setChecked( contact.isChecked() );
            }
        });


        // Create and populate contacts.
        ArrayList<String> temp_to_sort = new ArrayList<String>();
        contacts = (Contact[]) getLastCustomNonConfigurationInstance() ;
        if ( contacts == null ) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String sort_by = sharedPref.getString(SettingsActivity.SettingsScreen.pref_sort_by, "");
            String contacts_to_display = sharedPref.getString(SettingsActivity.SettingsScreen.pref_contacts_to_display, "");

            Cursor cursor = null;

            if(contacts_to_display.equals("")){
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            }
            else{
                String selection = ContactsContract.RawContacts.ACCOUNT_TYPE + "='" + contacts_to_display + "'";
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,selection, null, null);
            }



            /*
            if(contacts_to_display.equals("all")) {
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            }
            else if(contacts_to_display.equals("sim")) {
                String selection = ContactsContract.RawContacts.ACCOUNT_TYPE + "='com.android.contacts.usim'";
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,selection, null, null);
            }
            else if(contacts_to_display.equals("google")) {
                String selection = ContactsContract.RawContacts.ACCOUNT_TYPE + "='com.google'";
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,selection, null, null);
            }
            else if(contacts_to_display.equals("whatsapp")) {
                String selection = ContactsContract.RawContacts.ACCOUNT_TYPE + "='com.whatsapp'";
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,selection, null, null);
            }
            else //if(contacts_to_display.equals("mobile"))
            {
                String selection = ContactsContract.RawContacts.ACCOUNT_TYPE + "='com.xiaomi'";
                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,selection, null, null);
            }
            */


            if(cursor != null) {

                totalContactsCount = cursor.getCount();
                contacts = new Contact[totalContactsCount];

                int i = 0;
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String temp = "Name : " + name + "\nPhone : " + phoneNumber;
                    temp_to_sort.add(temp);
                    //contacts[i++] = new Contact(temp);
                }
                cursor.close();

                Collections.sort(temp_to_sort);
                if(sort_by.equals("descending_order")){
                    Collections.reverse(temp_to_sort);
                }

                Iterator iterator = temp_to_sort.iterator();
                for(int temp_i=0;iterator.hasNext();temp_i++)
                    contacts[temp_i] = new Contact(iterator.next().toString());
            }
        }

        ArrayList<Contact> contactList = new ArrayList<Contact>();
        if(contacts != null)
            contactList.addAll( Arrays.asList(contacts) );

        // Set our custom array adapter as the ListView's adapter.
        listAdapter = new ContactArrayAdapter(this, contactList);
        mainListView.setAdapter( listAdapter );
    }



    /*
    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mf = getMenuInflater();
        mf.inflate(R.menu.main_activity_menu, menu);

        MenuItem item = menu.findItem(R.id.search_id);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                listAdapter.getFilter().filter(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.settings_id){
            Intent intent = new Intent("com.example.ajay0.contacts.SettingsActivity");
            startActivity(intent);
        }
        else if(id == R.id.vcf_id){
            createVCF();
        }
        else if(id == R.id.share_id){
            Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.about_id){
            Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.refresh_id){
            reloadMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    /** Holds contact data. */
    private static class Contact {
        private String name = "" ;
        private boolean checked = false ;
        public Contact() {}
        public Contact( String name ) {
            this.name = name ;
        }
        public Contact( String name, boolean checked ) {
            this.name = name ;
            this.checked = checked ;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public boolean isChecked() {
            return checked;
        }
        public void setChecked(boolean checked) {
            this.checked = checked;
        }
        public String toString() {
            return name ;
        }
        public void toggleChecked() {
            checked = !checked ;
        }
    }

    /** Holds child views for one row. */
    private static class ContactViewHolder {
        private CheckBox checkBox ;
        private TextView textView ;
        public ContactViewHolder() {}
        public ContactViewHolder( TextView textView, CheckBox checkBox ) {
            this.checkBox = checkBox ;
            this.textView = textView ;
        }
        public CheckBox getCheckBox() {
            return checkBox;
        }
        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }
        public TextView getTextView() {
            return textView;
        }
        public void setTextView(TextView textView) {
            this.textView = textView;
        }
    }

    /** Custom adapter for displaying an array of Contact objects. */
    private static class ContactArrayAdapter extends ArrayAdapter<Contact> {

        private LayoutInflater inflater;

        public ContactArrayAdapter( Context context, List<Contact> contactList ) {
            super( context, R.layout.simplerow, R.id.rowTextView, contactList );
            // Cache the LayoutInflate to avoid asking for a new one each time.
            inflater = LayoutInflater.from(context) ;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Contact to display
            Contact contact = this.getItem( position );

            // The child views in each row.
            CheckBox checkBox ;
            TextView textView ;

            // Create a new row view
            if ( convertView == null ) {
                convertView = inflater.inflate(R.layout.simplerow, null);

                // Find the child views.
                textView = (TextView) convertView.findViewById( R.id.rowTextView );
                checkBox = (CheckBox) convertView.findViewById( R.id.CheckBox01 );

                // Optimization: Tag the row with it's child views, so we don't have to
                // call findViewById() later when we reuse the row.
                convertView.setTag( new ContactViewHolder(textView,checkBox) );

                // If CheckBox is toggled, update the contact it is tagged with.
                checkBox.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Contact contact = (Contact) cb.getTag();
                        contact.setChecked( cb.isChecked() );
                    }
                });
            }
            // Reuse existing row view
            else {
                // Because we use a ViewHolder, we avoid having to call findViewById().
                ContactViewHolder viewHolder = (ContactViewHolder) convertView.getTag();
                checkBox = viewHolder.getCheckBox() ;
                textView = viewHolder.getTextView() ;
            }

            // Tag the CheckBox with the Contact it is displaying, so that we can
            // access the contact in onClick() when the CheckBox is toggled.
            checkBox.setTag( contact );

            // Display contact data
            checkBox.setChecked( contact.isChecked() );
            textView.setText( contact.getName() );

            return convertView;
        }

    }

    public Object onRetainCustomNonConfigurationInstance () {
        return contacts ;
    }

    public void shareContacts(View view){
        createVcfClicked = false;
        createVCF();
        createVcfClicked = true;
        Context ctx = getApplicationContext();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String file_type = sharedPref.getString(SettingsActivity.SettingsScreen.pref_file_type, "");

        File oldfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Contacts.vcf");
        File newfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Contacts"+file_type);
        oldfile.renameTo(newfile);

        Uri data1 = Uri.fromFile(newfile);
        ArrayList<Uri> data = new ArrayList<Uri>();
        data.add(data1);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, data);
        shareIntent.setType("file/txt");

        shareIntent.putExtra(Intent.EXTRA_STREAM,data);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Contacts File");
        if(file_type.equals(".txt"))
            shareIntent.putExtra(Intent.EXTRA_TEXT,"Rename the file from 'Contacts.txt' to 'Contacts.vcf'.\nThen click on the file to get all the contacts.");
        else if(file_type.equals(".vcf"))
            shareIntent.putExtra(Intent.EXTRA_TEXT,"Download the file and click on it to import all the contacts in it.");

        startActivity(Intent.createChooser(shareIntent,"Share via"));
    }

    public void createVCF(){
        Context context = getBaseContext();
        File file;
        FileWriter fw = null;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Contacts.vcf/");
            fw = new FileWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer sb;
        for(int i=0;i<totalContactsCount;i++){
            if(contacts[i].isChecked()){

                sb = new StringBuffer();

                String temp[] = contacts[i].getName().split("\n");
                String tempName = temp[0].substring(7);
                String tempNumber = temp[1].substring(8);

                sb.append("\nBEGIN:VCARD");
                sb.append("\nVERSION:2.1");
                //sb.append("\nN:;"+tempName+";;;");
                sb.append("\nFN:"+tempName);
                //sb.append("\nTEL;CELL:"+tempNumber);
                sb.append("\nTEL;TYPE=WORK:"+tempNumber);
                sb.append("\nEND:VCARD");

                try {
                    fw.append(sb);
                    fw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String file_type = sharedPref.getString(SettingsActivity.SettingsScreen.pref_file_type, "");
        if(createVcfClicked)
            Toast.makeText(context, "VCF created in IntenalStorage/Downloads", Toast.LENGTH_SHORT).show();

        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //EditText editText = (EditText) findViewById(R.id.editText);
        //editText.setText(sb);
    }


    public void reloadMainActivity() {
        finish();
        startActivity(getIntent());
    }

}
