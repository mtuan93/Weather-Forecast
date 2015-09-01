# Weather Forecast Anroid Application

### Itroduction

Weather Forecast is an android application that will access user's phone's current location and invoke the Weather Underground service API to display the current hourly weather forecast.
This is my second android application following my [Tic Tac Toe android game application](https://github.com/mtuan93/Tic-Tac-Toe-android). I built this app to learn more about Android Development with Web Services.

### Screenshots

<img src="https://github.com/mtuan93/Weather-Forecast/blob/master/app.png" width="200">
<img src="https://github.com/mtuan93/Weather-Forecast/blob/master/start.png" width="200">
<img src="https://github.com/mtuan93/Weather-Forecast/blob/master/load.png" width="200">
<img src="https://github.com/mtuan93/Weather-Forecast/blob/master/loadsql.png" width="200">

### How the app works

* `Startscreen`: associates with `activity_start_screen.xml`, which contains a start button and an onClick event listener to start `MainActivity`. The purpose of this class is to make a friendly start screen before launch main activity. It can also be used to add welcome message and info about the app.

* `DatabaseManager`: contains an instance of `SQLiteDatabase` and a helper `SQLHelper` which extends `SQLiteOpenHelper`. The purpose of this class is to store the entire JSON string of hourly forecast data and invoking time from Weather Underground Web Services into an SQLite database. The invoking time will be used to decide whether the app is re-launched within the past hour, if yes, the hourly forecast data will be loaded directly from the database, otherwise, a new call to Weather Underground Web Service will be launched.
	* `SQLiteDatabase` has methods to create, delete, execute SQL commands, and perform other common database management tasks. 
	* `SQLiteOpenHelper` is a helper class to manage database creation and version management. By subclassing and implementing `onCreate(SQLiteDatabase)`, `onUpgrade(SQLiteDatabase, int, int)` and optionally `onOpen(SQLiteDatabase)`, and this class takes care of opening the database if it exists, creating it if it does not, and upgrading it as necessary. Transactions are used to make sure the database is always in a sensible state.
	* Main functionalities of DatabaseManager: 
		* `addRow` : insert a new row of JSON string of hourly forecast with the time of invoking the Weather Underground Web Services.
		* `retrieveRow` : return an array of String of two things: the JSON string of hourly forecast data and the time invoking the Weather Underground Services.
* `WeatherAdapter`: extends `SimpleAdapter`, which is an easy adapter to map static data to views defined in an XML file. The data is specified as an ArrayList of Maps. Each entry in the `ArrayList` corresponds to one row in the list. The Maps contain the data for each row. The layout `list_item.xml` defines the views used to display the row, and a mapping from keys in the Map to specific views. In `WeatherAdapter`, each row consists of a `TextView` for the date, an `ImageView` for the icon, `TextViews` for condition, tempurature, and humidity. The icon on `ImageView` is a bitmap, resulting from the download function of class `DownloadImage`. `DownloadImage` is an `Asynctask` that performs downloading the icon by the downloading URL passed from the JSON String from Weather Underground Web Services. 
* `MainActivity`: contains an instance of `DatabaseManager` to store and retrieve forecast data, a `String` for store JSON data from Weather Underground Web Service and a `boolean` to check if the Weather Underground Web Serivce was re-launched within the past hour. `NetworkCallTask` is an `AsyncTask`, which enables proper and easy use of the UI thread. This class allows to perform background operations and publish results on the UI thread without having to manipulate threads and/or handlers. 
	* The class overrides `doInBackGround` method to check if the Weather Underground Web Serivce was re-launched within the past hour, if yes, it will assign its `String` field to the String query from the database, otherwise, it will call `setData`, a helper function to get current location (city and state) of the user's phone, and most importantly, `makeCall`, which will invoke the Weather Underground Web Service to get the hourly forecase data.
	* The class overrides `onPostExecute`, which is executed right after `doInBackGround` finishes. In this method, the `String` weather forecast data will be formatted into an `ArrayList` of `Hashmap`, then an instance of `WeatherAdapter` will be invoked to set these data into the list view.

### Additional Development Tools:
* IDE: Android Studio.
* Helper Libraries: gson-2.3.1.jar, WunderGround API.

### Demo:
There are two easy ways to check this application out:
* Since the app is developed in Android Studio, you can clone this app repository and import it into your Android Studio. Then either run it with an emulator or on an actual device (what I do). More information on how to do this is availabe [here](https://developer.android.com/tools/building/building-studio.html).
* You can download the `apk` file [here](https://github.com/mtuan93/Weather-Forecast/raw/master/app-debug.apk) and install it into your android device. Since this is an unregistered debug version, make sure that you enable `Unknown sources` in `Security` section of the phone.