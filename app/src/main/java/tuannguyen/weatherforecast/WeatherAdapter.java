package tuannguyen.weatherforecast;
/**
 * Created by Tuan on 2/26/2015.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class WeatherAdapter extends SimpleAdapter
{
    private Context context;
    private ArrayList<HashMap<String, String>> forecast;
    private int view;
    private String[] from;
    private int[] to;

    public WeatherAdapter(Context context, ArrayList<HashMap<String, String>> forecast, int view, String[] from, int[] to)
    {
        super(context, forecast,
                view, from, to);
        this.context = context;
        this.forecast = forecast;
        this.view = view;
        this.from = from;
        this.to = to;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView day = (TextView) rowView.findViewById(to[0]);
        ImageView icon = (ImageView) rowView.findViewById(to[1]);
        TextView condition = (TextView) rowView.findViewById(to[2]);
        TextView temp = (TextView) rowView.findViewById(to[3]);
        TextView humid = (TextView) rowView.findViewById(to[4]);

        day.setText(forecast.get(position).get(from[0]));
        icon.setTag(forecast.get(position).get(from[1]));
        condition.setText(forecast.get(position).get(from[2]));
        temp.setText(forecast.get(position).get(from[3]));
        humid.setText(forecast.get(position).get(from[4]));

        new DownloadImage().execute(icon);
        return rowView;
    }

    public class DownloadImage extends AsyncTask<ImageView, Void, Bitmap>
    {

        ImageView imageView;

        @Override
        protected Bitmap doInBackground(ImageView... imageViews){
            // set the image view in this class (which references the view in the adapter class)
            this.imageView = imageViews[0];
            // download the appropriate image using the url in the tag
            return download((String)imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            imageView.setImageBitmap(result);
        }

        private Bitmap download(String url)
        {

            Bitmap bmp = null;
            try
            {
                URL myurl = new URL(url);
                HttpURLConnection con = (HttpURLConnection)myurl.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;

            }
            catch(Exception e)
            {}
            return bmp;
        }
    }
}