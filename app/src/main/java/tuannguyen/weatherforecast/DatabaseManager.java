package tuannguyen.weatherforecast;
/**
 * Created by Tuan on 2/26/2015.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager
{
    public static final String DB_NAME = "weather";
    public static final String DB_TABLE = "forecasts";
    public static final int DB_VERSION = 1;
    private static final String CREATE_TABLE = " CREATE TABLE " + DB_TABLE + " (id INTEGER " +
            "PRIMARY KEY, hourlyData VARCHAR, time DATETIME);";
    private SQLHelper helper;
    private SQLiteDatabase db;
    private Context context;

    public DatabaseManager(Context c)
    {
        this.context = c;
        helper = new SQLHelper(c);
        this.db = helper.getWritableDatabase();
    }

    public DatabaseManager openReadable() throws SQLException
    {
        helper = new SQLHelper(context);
        db = helper.getReadableDatabase();
        return this;
    }

    public void close()
    {
        helper.close();
    }

    public void addRow(Integer c, String json, String time)
    {

        if(!checkExists(DB_TABLE, "id", "1"))
        {
            ContentValues newForecast = new ContentValues();
            newForecast.put("id", c);
            newForecast.put("hourlyData", json);
            newForecast.put("time", time);
            try
            {
                db.insertOrThrow(DB_TABLE, null, newForecast);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            ContentValues data = new ContentValues();
            data.put("id", c);
            data.put("hourlyData", json);
            data.put("time", time);
            db.update(DB_TABLE, data, "id=" + c, null);
        }
    }

    public boolean checkExists(String TableName, String field, String fieldValue)
    {
        String Query = "Select * from " + TableName + " where " + field + " = " + fieldValue;
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            return false;
        }
        return true;
    }

    public String[] retrieveRow()
    {
        String[] columns = new String[]{"id", "hourlyData", "time"};
        Cursor cursor = db.query(DB_TABLE, columns, null, null, null, null, null);
        String forecast = "";
        String time = "";

        if(cursor != null) {
            cursor.moveToFirst();

            // just get first result
            if (!cursor.isAfterLast()) {
                forecast = cursor.getString(1);
                time = cursor.getString(2);

                cursor.close();
            } else
                return null;

            return new String[]{forecast, time};
        }
        return null;
    }

    public class SQLHelper extends SQLiteOpenHelper
    {
        public SQLHelper(Context c)
        {
            super(c, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }
}
