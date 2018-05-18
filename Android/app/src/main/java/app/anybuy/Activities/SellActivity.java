package app.anybuy.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import app.anybuy.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

public class SellActivity extends AppCompatActivity {


    private TextView textView;


    // use to get the location
    protected FusedLocationProviderClient mFusedLocationClient;
    double lattitude = -1;
    double longitude = -1;

    //use to get the address from the location
    List<Address> addresses;
    Geocoder geocoder;

    String userCountryCode;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        textView = (TextView) findViewById(R.id.locationTextViewID);


        //get the location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //get the address from the location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            //textView.setText("altetude: " + location.getLatitude() + " \n Longtitude: " + location.getLongitude());

                            lattitude = location.getLatitude();
                            longitude = location.getLongitude();

                            geocoder = new Geocoder(SellActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(lattitude, longitude, 1);

                                //get the info of the user
                                // String address = addresses.get(0).getAddressLine(0);
                                //String area = addresses.get(0).getLocality();
                                //String city = addresses.get(0).getAdminArea();
                                String countryName = addresses.get(0).getCountryName();

                                userCountryCode = addresses.get(0).getCountryCode();

                                String postalCode = addresses.get(0).getPostalCode();

                                // textView.setText("contry code: " + countryCode + " \n country name: " + count);
                            } catch (Exception e) {
                                System.out.println("didnt work");
                            }
                            //textView.setText(countryName);
                        }
                    }
                });

        System.out.println("heyyyyyyyyyyyyyyyyy " + userCountryCode);
    }


    // tryed to make a function for it but failed, feel free to try


   /* String countryCode = "not working";
    //this function will get the address
    public String getUserCountryCode(FusedLocationProviderClient m) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return countryCode;
        }

        m.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            //textView.setText("altetude: " + location.getLatitude() + " \n Longtitude: " + location.getLongitude());

                            lattitude = location.getLatitude();
                            longitude = location.getLongitude();

                            geocoder = new Geocoder(SellActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(lattitude, longitude, 1);

                                //get the info of the user
                               // String address = addresses.get(0).getAddressLine(0);
                                //String area = addresses.get(0).getLocality();
                                //String city = addresses.get(0).getAdminArea();
                                String countryName = addresses.get(0).getCountryName();

                                 countryCode = addresses.get(0).getCountryCode();

                                String postalCode = addresses.get(0).getPostalCode();

                               // textView.setText("contry code: " + countryCode + " \n country name: " + count);
                            } catch (Exception e) {
                                System.out.println("didnt work");
                            }
                            //textView.setText(countryName);
                        }
                    }
                });

    System.out.println("heyyyyyyyyyyyyyyyy " + countryCode);
        return countryCode;
    }
*/
}
