package soap.sophanith.soap;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.HttpsTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mConvert;
    private EditText mFahrenheit;
    private TextView mAfterConvert;
    private String mEditTextInput;
    private ProgressBar mProgressBar;
    private String mValueReceive;
    private Webservice webservice;
    private String mValueCelsiusReceive;
    private boolean statusClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFahrenheit = findViewById(R.id.input_text);
        mConvert = findViewById(R.id.convert_button);
        mAfterConvert = findViewById(R.id.after_convert);
        mProgressBar = findViewById(R.id.progress_bar);
        mConvert.setOnClickListener(this);

        checkNetworkStatus();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mConvert.getId()) {
            doConvertFahrenheitToCelsius();
        }
    }

    private void doConvertFahrenheitToCelsius() {
        if (!mFahrenheit.getText().toString().isEmpty()) {
            statusClick = true;
            mProgressBar.setVisibility(View.VISIBLE);
            webservice = new Webservice();
            webservice.execute();
            mEditTextInput = mFahrenheit.getText().toString();
        } else {
            mAfterConvert.setText("Please input the value");
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Attention")
                    .setMessage("Please Input the value for convert!")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
    }

    private class Webservice extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mProgressBar.setVisibility(View.VISIBLE);
            String NAMESPACE = "https://www.w3schools.com/xml/";
            String MAIN_REQUEST_URL = "https://www.w3schools.com/xml/tempconvert.asmx";
            String SOAP_ACTION_FAHRENHEIT = "https://www.w3schools.com/xml/FahrenheitToCelsius";
            String SOAP_ACTION_CELSIUS = "https://www.w3schools.com/xml/CelsiusToFahrenheit";
            final String METHOD_NAME_FAHRENHEIT = "FahrenheitToCelsius";
            final String METHOD_NAME_CELSIUS = "CelsiusToFahrenheit";

            SoapObject requestFahrenheit = new SoapObject(NAMESPACE, METHOD_NAME_FAHRENHEIT);
            SoapObject requestCelsius = new SoapObject(NAMESPACE, METHOD_NAME_CELSIUS);

            try {
                requestFahrenheit.addProperty("Fahrenheit", mEditTextInput);
                requestFahrenheit.addProperty("Celsius", mValueCelsiusReceive);

                SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                soapEnvelope.dotNet = true;

                if (statusClick) {
                    soapEnvelope.setOutputSoapObject(requestFahrenheit);
                } else {
                    soapEnvelope.setOutputSoapObject(requestCelsius);
                }

                HttpTransportSE transport = new HttpTransportSE(Proxy.NO_PROXY, MAIN_REQUEST_URL, 20000);
                transport.debug = true;
                transport.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");

                transport.call(SOAP_ACTION_FAHRENHEIT, soapEnvelope);
                SoapPrimitive resultFahrenheit = (SoapPrimitive) soapEnvelope.getResponse();
                mValueReceive = String.valueOf(resultFahrenheit);

                transport.call(SOAP_ACTION_CELSIUS, soapEnvelope);
                SoapPrimitive resultCelsius = (SoapPrimitive) soapEnvelope.getResponse();
                mValueCelsiusReceive = String.valueOf(resultCelsius);

                Log.i("ResponseData", "Result Celsius: " + resultCelsius);
            } catch (Exception ex) {
                Log.e("ResponseData", "Error: " + ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.GONE);
            mAfterConvert.setText(mValueReceive + " F");
        }
    }

    private void checkNetworkStatus() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        assert conMgr != null;
        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            Toast.makeText(this, "You are connecting to the Internet!", Toast.LENGTH_SHORT).show();
        } else if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {
            Toast.makeText(this, "You are not connecting to the Internet!", Toast.LENGTH_SHORT).show();
        }
    }
}
