package geller.omry.heartme;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import android.widget.EditText;
import android.widget.TextView;


import geller.omry.heartme.Model.BloodTestManger;

public class MainActivity extends AppCompatActivity {

    private final String HAPPY_FACE="☺";
    private final String SAD_FACE="☹";
    private EditText  testName;
    private EditText testResult;
    private String bloodTestName;
    private TextView smileyFace;
    private TextView category;
    private BloodTestManger bloodTestManger;
;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(!haveNetworkConnection()){
            findViewById(R.id.main_layout).setVisibility(View.INVISIBLE);

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("No internet connection");
            builder.setCancelable(false);
            builder.setMessage("The App can't work if it's not connected to internet\n" +
                    "please connect your device and come back to us ☺");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    System.exit(-1);
                }
            });
            builder.create().show();
        }
        else
            bloodTestManger=BloodTestManger.getManagerInstance(); // this object is responsible for getting blood tests data and evaluating user's blood test input

        testName=(EditText)findViewById(R.id.name);

        testName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {

                    String userCategoryInput=v.getText().toString();

                    if(userCategoryInput == null || userCategoryInput.isEmpty()) {
                        v.setError("Input cannot be empty");
                        return false;
                    }

                    bloodTestName=bloodTestManger.getRelevantBloodTestCategory(userCategoryInput);

                }
                return false;
            }
        });

        testResult=(EditText)findViewById(R.id.result);

        testResult.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    //press ACTION_DONE on keyboard after inserting a numeric input to get a result!

                    String number = v.getText().toString();

                    if (number == null || number.isEmpty()) {
                        v.setError("Input cannot be empty");
                        return false;
                    }

                    if(!isBloodTestNameValid()){
                        return false;
                    }

                    int myTestResult = Integer.valueOf(number);

                    int globalTestResult = Integer.valueOf(bloodTestManger.getBloodTestResult(bloodTestName));

                    category.setText(bloodTestName);

                    if (myTestResult < globalTestResult)
                        smileyFace.setText(HAPPY_FACE);
                    else
                        smileyFace.setText(SAD_FACE);

                    bloodTestName=null;
                }
                return false;
            }
        });

        smileyFace=(TextView)findViewById(R.id.smiley_face);

        category=(TextView)findViewById(R.id.category);
    }


    /**
     * This method checks if the user's blood test input is valid ( not empty,not null and has a relevnat category).
     * @return true if the user's input has a relevnat category in the dataset and false if its null,empty,or the category is unknown.
     */
    public boolean isBloodTestNameValid(){

        if (bloodTestName == null && !testName.getText().toString().isEmpty())
            bloodTestName = bloodTestManger.getRelevantBloodTestCategory(testName.getText().toString());

        smileyFace.setText("");

        if (bloodTestName == null)
        {
            category.setText("");
            testName.setError("Input cannot be empty");
            return false;
        }

        if (bloodTestName.equalsIgnoreCase("Unknown")) {
            category.setText("Unknown");
            bloodTestName = null;
            return false;
        }

        if (bloodTestManger.getBloodTestResult(bloodTestName) == null) {
            category.setText("Unknown");
            bloodTestName = null;
            return false;
        }

        return true;
    }

    /**
     * This method checks for network connection Mobile or Wifi.
     * @return - true if the device has any kind of internet connection and false if its disconnected.
     */
    private boolean haveNetworkConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
