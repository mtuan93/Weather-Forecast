package tuannguyen.weatherforecast;
/**
 * Created by Tuan on 2/26/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity
{

    private String forecastData;
    private DatabaseManager myDB;
    private boolean isWithinHour;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isWithinHour = false;
        new NetworkCallTask().execute((URL[]) null);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private boolean isForeCastExist(String time)
    {
        if((Double.parseDouble(time) > 0) && (Double.parseDouble(getDateTime()) - Double.parseDouble(time)) > 60)
        {
            return false;
        }
        return true;
    }

    public class NetworkCallTask extends AsyncTask<URL, Integer, Long> {


        @Override
        protected Long doInBackground(URL... params) {
            myDB = new DatabaseManager(MainActivity.this);
            myDB.openReadable();
            String[] data = myDB.retrieveRow();
            if(data != null && isForeCastExist(data[1]))
            {
                isWithinHour = true;
                forecastData = data[0];
            }
            else
            {
                String[] locationData = setData();
                makeCall(locationData, "geolookup");
                forecastData = makeCall(locationData, "hourly");
                myDB = new DatabaseManager(MainActivity.this);
                myDB.addRow(1, forecastData, getDateTime());
            }

            myDB.close();
            return null;
        }

        @Override
        protected void onPostExecute(Long result)
        {

            // store the forecast data in an arraylist of hashmaps
            ArrayList<HashMap<String, String>> forecast= new ArrayList<HashMap<String, String>>();
            // specify how the data is going to be mapped to the adapter
            String[] from = new String[]{"time", "url", "condition", "temp", "humidity"};
            int[] to = new int[]{R.id.time, R.id.icon,
                    R.id.condition, R.id.temp, R.id.humid};

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(forecastData);
            JsonObject object = root.getAsJsonObject();
            JsonArray array = object.get("hourly_forecast").getAsJsonArray();

            for(JsonElement x : array )
            {

                HashMap<String, String> map = new HashMap<String, String>();

                map.put("time",x.getAsJsonObject().get("FCTTIME").getAsJsonObject().get("pretty")
                        .getAsString());
                map.put("condition", x.getAsJsonObject().get("condition").getAsString());
                map.put("temp", x.getAsJsonObject().get("temp").getAsJsonObject().get("english")
                        .getAsString() + "°F");
                map.put("humidity", "Humid. " +x.getAsJsonObject().get("humidity").getAsString
                        ());
                map.put("url", x.getAsJsonObject().get("icon_url").getAsString());

                forecast.add(map);
            }
            WeatherAdapter adapter = new WeatherAdapter(MainActivity.this, forecast,
                    R.layout.list_item, from, to);
            if(isWithinHour)
            {
                Toast.makeText(getApplicationContext(), "Loading data", Toast.LENGTH_SHORT).show();
            }
            ListView list = (ListView)findViewById(R.id.listview);
            list.setAdapter(adapter);
        }

        private String[] setData()
        {
            String key = "30e230f35e6da607";
            String[] coords = getGPSLocation();
            String lat = coords[0];
            String lon = coords[1];

            URL url = null;
            try
            {
                url = new URL("http://api.wunderground" +
                        ".com/api/"+key+"/geolookup/q/"+
                        lat +"," +lon +".json");
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }

            URLConnection website = null;
            try
            {
                website = url.openConnection();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                website.connect();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            JsonParser jp = new JsonParser();
            JsonElement root = null;
            try
            {
                root = jp.parse(new InputStreamReader((InputStream) website.getContent()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            JsonObject rootobj = root.getAsJsonObject();
            JsonObject locationobj = rootobj.get("location").getAsJsonObject();
            String city = locationobj.get("city").getAsString();
            String state = locationobj.get("state").getAsString();

            String[] data = {key, state, city};
            return data;
        }
        private String makeCall(String[] data, String type)
        {
            URL website = null;
            try
            {
                website = new URL("http://api.wunderground.com/api/" +data[0] +"/" +type +"/q/" +data[1] +
                        "/" +data[2] +".json");
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            URLConnection connection = null;
            try
            {
                connection = website.openConnection();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try
            {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder response = new StringBuilder();
            String inputLine;
            try
            {
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.toString();
        }
        public String[] getGPSLocation()
        {
            double lat, lon;
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null)
            {
                lon = location.getLongitude();
                lat = location.getLatitude();
            }
            else
            {
                lon = -75.01428223;
                lat = 40.11690903;
            }
            return new String[]{Double.toString(lat), Double.toString(lon)};
        }
    }
}

