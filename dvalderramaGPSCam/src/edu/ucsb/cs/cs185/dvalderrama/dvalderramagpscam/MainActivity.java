package edu.ucsb.cs.cs185.dvalderrama.dvalderramagpscam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/*
 * Functionality:
 * -Camera app that logs GPS data to an XML file and sets the last picture
 * taken as the root view's background.
 * 
 * Known Issues/To do:
 * -Background is cleared on intent cancellation
 * -Need to use bundle to support rotations
 * -GPS listener is active for too long(need to unbind during onPause/onStop) 
 */
public class MainActivity extends ActionBarActivity {
	//Vars
	private DialogFragment settingsFrag = new SettingsFragment();
	private DialogFragment helpFrag = new HelpFragment();
	private static final String PREFS_NAME = "CameraPreferences";
	private static int photoNumber;
	private static final int defaultPhotophotoNumber = 0;
	private static double longitude = -1;
	private static double latitude = -1;
	private static LocationManager lm;
	private static Location currentLocation;
	private static String photoFilename = "";
	private static String pathToXMLFile;
	private static GPSToFile gpsToFile;
	private static String mCurrentPhotoPath;
	private static final int REQUEST_TAKE_PHOTO = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		// Restore photoNumber tag used in file creation
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		photoNumber = settings.getInt("storedPhotophotoNumber", defaultPhotophotoNumber);
		
		//Start GPS listener
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		startGPSListener();
		
		//Create object to convert image data to xml
		gpsToFile = new GPSToFile();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		//Use the editor to save the photoNumber tagged used (e.g. photo-001.jpg)
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("storedPhotophotoNumber", photoNumber);
		editor.commit();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("storedPhotophotoNumber", photoNumber);
		editor.commit();
	}
	
	
	private static File getOutputMediaFile() throws IOException {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "GPSPics");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("File", "Failed to create directory");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        photoFilename = "photo-" + String.format("%03d", photoNumber) + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + photoFilename);
        mCurrentPhotoPath = mediaFile.toString();
        pathToXMLFile = mediaStorageDir.getPath() + File.separator + "PicListGPS.xml"; 
        return mediaFile;
    }
	

	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = getOutputMediaFile();
	        } catch (IOException ex) {
	        	Log.d("File", "Error creating file");
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	                    Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
	        }
	    }
	}
	
	//Corresponds to the Button at the bottom of the layout
	public void doTakePhoto(View view)
	{
		dispatchTakePictureIntent();
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//Creates a scaled bitmap which is then converted into a drawable and set as the 
		//parent view's background image
		scaleAndSetPicture();
		
		//Write image data to file
		gpsToFile.toXML(photoFilename, pathToXMLFile,  latitude, longitude);
		
		//file is normally written as 'photo-001.jpg' so need to increment the photo counter
		photoNumber++;
	}
	
	private void scaleAndSetPicture() {
		View mImageView = (View) findViewById(R.id.camera_preview);
	    // Get the dimensions of the View
	    int targetW = mImageView.getWidth();
	    int targetH = mImageView.getHeight();

	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;

	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;

	    //Create the drawable from the bitmap
	    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    Drawable d = new BitmapDrawable(bitmap);
	    //Display the drawable on the layout background
	    mImageView.setBackgroundDrawable(d);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	private void startGPSListener(){
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				//Log.d("listener", "listening");
				longitude = location.getLongitude();
				latitude = location.getLatitude();
			}
			@Override
			public void onProviderDisabled(String arg0) {}
			@Override
			public void onProviderEnabled(String arg0) {}
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		};
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	        	//Log.d("settings", "settings pressed");
	        	settingsFrag.show(getSupportFragmentManager(), "settingsFragment");
	            return true;
	        case R.id.action_takephoto:	        	
	            //Get current or last known coordinates before launching the camera intent
	            try{
	            
	            	currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	            	longitude = currentLocation.getLongitude();
	            	latitude = currentLocation.getLatitude();
	            }
	            catch(NullPointerException e){
	            	Log.d("GPS", "getLastKnownLocation() returned NULL");
	            	e.printStackTrace();
	            }
	            //Launch camera intent
	        	dispatchTakePictureIntent();
	            
	            return true;
	        case R.id.action_help:
	        	//Log.d("help", "help pressed");
	        	helpFrag.show(getSupportFragmentManager(), "helpFragment");
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
