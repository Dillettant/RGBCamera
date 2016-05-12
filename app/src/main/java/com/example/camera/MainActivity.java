package com.example.camera;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;

import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import android.net.Uri;
import android.content.Intent;
import android.provider.MediaStore;

import java.io.File;
import android.os.Environment;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;


public class MainActivity extends Activity {

	private static final String TAG = "CallCamera";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
	private static boolean FLAG = false;
		
	Uri fileUri = null;
	ImageView photoImage = null;
	  
	private File getOutputPhotoFile() {
		
		  File directory = new File(Environment.getExternalStoragePublicDirectory(
		                Environment.DIRECTORY_PICTURES), getPackageName());
		  
		  if (!directory.exists()) {
		    if (!directory.mkdirs()) {
		      Log.e(TAG, "Failed to create storage directory.");
		      return null;
		    }
		  }
		  
		  String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new Date());
		  
		  return new File(directory.getPath() + File.separator + "IMG_"  
		                    + timeStamp + ".jpg");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		photoImage = (ImageView) findViewById(R.id.photo_image);
	
		final Button callCameraButton = (Button)
		findViewById(R.id.button_callcamera);
	
		callCameraButton.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				fileUri = Uri.fromFile(getOutputPhotoFile());
				i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ );
			}
		});

		//the Calculate button
		final Button CalculateButton = (Button)
		findViewById(R.id.button_regcolor);

		CalculateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				TextView illumination = (TextView) findViewById(R.id.illumination);
				if (FLAG == false) {
					illumination.setText("Please take a photo first.");
				}
				else {
					bitmap2Gray();// image to gray and show the image
					illumination.setText("Please press the image to get RGB.");
				}
			}
		});

		photoImage.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int x = (int) event.getX();
				int y = (int) event.getY();

				//取出原来的图片
				photoImage.setDrawingCacheEnabled(true);
				photoImage.buildDrawingCache();
				Bitmap image = ((BitmapDrawable)photoImage.getDrawable()).getBitmap();
				photoImage.setDrawingCacheEnabled(false);

				if (event.getAction() == MotionEvent.ACTION_UP) {
					int color = image.getPixel(x, y);
					int r = Color.red(color);
					int g = Color.green(color);
					int b = Color.blue(color);
					int L = (r*299 + g*587 + b*114 + 500) / 1000;
					Log.i(TAG, "r=" + r + ",g=" + g + ",b=" + b);
					TextView illumination = (TextView) findViewById(R.id.illumination);
					illumination.setText("L=" + L + ", r=" + r + ", g=" + g + ", b="
							+ b);
					CalculateButton.setTextColor(Color.rgb(r, g, b));
					CalculateButton.setText("Calculated");

				}
				return true;
			}
		});

	}

	/**
	 * image to gray
	 * show the image
	 */
	private void bitmap2Gray()
	{
		//取出原来的图片
		photoImage.setDrawingCacheEnabled(true);
		photoImage.buildDrawingCache();
		Bitmap image = ((BitmapDrawable)photoImage.getDrawable()).getBitmap();
		photoImage.setDrawingCacheEnabled(false);

		Bitmap final_image = toGrayscale(image);

		//显示图片
		photoImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		photoImage.setImageBitmap(final_image);
	}


	/**
	 *
	 * @param bmpOriginal
	 * @return
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/**
	 * the original codes
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
		    if (resultCode == RESULT_OK) {
		      Uri photoUri = null;
		      if (data == null) {
		        // A known bug here! The image should have saved in fileUri
		        Toast.makeText(this, "Image saved successfully", 
		                       Toast.LENGTH_LONG).show();
		        photoUri = fileUri;
		      } else {
		        photoUri = data.getData();
		        Toast.makeText(this, "Image saved successfully in: " + data.getData(), 
		                       Toast.LENGTH_LONG).show();
		      }
		      showPhoto(photoUri.getPath());
				FLAG = true;
		    } else if (resultCode == RESULT_CANCELED) {
		      Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
		    } else {
		      Toast.makeText(this, "Callout for image capture failed!", 
		                     Toast.LENGTH_LONG).show();
		    }
		  }
	}
	
	private void showPhoto(String photoUri) {
		  File imageFile = new File (photoUri);
		  if (imageFile.exists()){
		     Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
		     BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
		     photoImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		     photoImage.setImageDrawable(drawable);
		  }       
	}

}