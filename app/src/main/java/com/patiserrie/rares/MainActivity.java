package com.patiserrie.rares;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main activity";
    AppCompatActivity activity;
    static final String DEFAULT_GRAPH_ITEM = Item.RSH;
    public String currentItem;
    public static String mMostRecentItemDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        if(getDefaultSharedPreferences(this).getBoolean("NEWS_ENABLED", true)){
            showNewsDialog();
        }

        currentItem = DEFAULT_GRAPH_ITEM;

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainActivityContainer, ItemListFragment.newInstance("",""))
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //and this to handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.home) {
            //showAllItemValuesDialog();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainActivityContainer, ItemListFragment.newInstance("",""))
                    .commit();
            return true;
        }
        else if(id == R.id.settings){
            //Open settings activity
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if(id == R.id.infoIcon){


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About")
                    .setMessage("RuneRares was first published September 17, 2017, by Shn. Most GE-buyable items " +
                            "are updated from price checks on the Runescape forums. Non-GE-buyable items (such as " +
                            "partyhats) are updated via an average of the item's last 10 trades, with the more recent " +
                            "trades having more weight.")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();


            return true;
        }


        else if(id == R.id.feedbackIcon){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Give feedback?")
                    .setMessage("If you have feedback, questions, suggestions, or need to report a bug, " +
                            "please click \"Give Feedback\" below to open up your email app. In order to make " +
                            "the app experience better, your feedback is very much needed!")
                    .setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            composeEmail();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

            return true;
        }
        else if(id == R.id.news){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainActivityContainer, NewsFragment.newInstance("",""))
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }


    private void composeEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"patiserrie0000@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "RuneRares Feedback");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * This method should be called in app versions where the add data button is disabled.
     */
    @Deprecated
    private void showNotAuthorizedDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Add Data - Not Authorized")
                .setMessage("You can only submit new prices when authorized by the admin. " +
                        "In the early stages of this app's development, we are working on figuring " +
                        "out how the authorization process will go about. Please check back in a few days for" +
                        " information on how to submit an authorization request. ")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    @Deprecated
    private void showAddDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_data, null);
        final Spinner itemSpinner = (Spinner) dialogView.findViewById(R.id.itemsSpinner);
        final EditText valueBox = (EditText) dialogView.findViewById(R.id.valueBox);
        final EditText dateBox = (EditText) dialogView.findViewById(R.id.dateBox);
        final Spinner bsSpinner = (Spinner) dialogView.findViewById(R.id.bsSpinner);

        ArrayAdapter<CharSequence> itemSpinnerAdapter, bsSpinnerAdapter;
        itemSpinnerAdapter = new ArrayAdapter<CharSequence>(activity,
                android.R.layout.simple_spinner_item,
                Item.getRareItemList());
        itemSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bsSpinnerAdapter = new ArrayAdapter<CharSequence>(activity,
                android.R.layout.simple_spinner_item,
                new CharSequence[]{Item.B, Item.S});
        bsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bsSpinner.setAdapter(bsSpinnerAdapter);


        itemSpinner.setAdapter(itemSpinnerAdapter);
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //If the user selects an item that is not in the grand exchange,
                //the B/S spinner will be disabled so that the legend can be formatted appropriately in the RareGraph class

                boolean enableSpinner = false;
                if (Item.getItemProperties(Item.getRareItemList()[position].toString()).ismBuyable())
                    enableSpinner = true;
                bsSpinner.setEnabled(enableSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(dialogView);

        //Codes what to do when user adds data.
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String value = valueBox.getText().toString();
                final String name = (String) itemSpinner.getSelectedItem();
                final String bs = (String) bsSpinner.getSelectedItem();
                final String date = (String) dateBox.getText().toString();

                Item addItem = new Item();
                addItem.setmBS(bs);
                addItem.setmName(name);
                addItem.setmDate(date);
                addItem.setmValue(value);

                if(addItem.getmDate().length() != 10){
                    //Date should be in format YYYY-MM-DD. I.e. 2017-09-16 not 2017-9-16. The latter interferes
                    //with how the dates are ordered.
                    Toast.makeText(activity, "Date entered is not valid length", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AddItemAsyncTask().execute(addItem, new AddItemAsyncTask.IOnFinishedListener() {
                    @Override
                    public void onCompleted(boolean success) {
                        String message = success ? "Successfully added data" : "Failed to add data";

                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                        activity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.mainActivityContainer, ChartFragment.newInstance(name, ""))
                                .commit();

                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(activity, "Entry cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    @Deprecated
    private void showChangeGraphDialog() {
        final CharSequence[] itemList = Item.getRareItemList();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select graph");
        builder.setItems(itemList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = (String) itemList[which];
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainActivityContainer, ChartFragment.newInstance(item, ""))
                        .commit();
                currentItem = item;
            }
        });
        builder.create().show();
    }

    private void showNewsDialog() {
        new GetNewsItem().execute(
                new GetNewsItem.IOnNewsItemObtained() {
                    @Override
                    public void onNewsObtained(String[] newsMessage) {
                        String msg = newsMessage[0];
                        new AlertDialog.Builder(activity)
                                .setTitle("Recent News")
                                .setMessage(msg)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                    }
                },
                activity,
                true
        );
    }

    @Deprecated
    private void showAllItemValuesDialog() {
        final String dialogTitle = currentItem;
        new RetrieveItemsAsync().execute(
                new RetrieveItemsAsync.IOnObtainItemsTaskFinished() {
                    @Override
                    public void onCompleted(List<RetrieveItemsAsync.ValuePoint> values) {
                        if (values != null && values.size() > 0) {
                            String[] stringVals = new String[values.size()];
                            for (int iteration = 0; iteration < values.size(); iteration++) {
                                stringVals[iteration] =
                                        values.get(iteration).getmDate() + " || " +
                                                values.get(iteration).getmValue() + "M";
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(dialogTitle + " values")
                                    .setItems(stringVals, null)
                                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();
                        }
                    }
                },
                currentItem
        );
    }


}


