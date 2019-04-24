package com.example.multipartentity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private Button upload, pick;
	private ProgressDialog dialog;
	MultipartEntity entity;
	GridView gv;
	int count = 0;
	public ArrayList<String> map = new ArrayList<String>();
	Bundle b;
	TextView noImage;
	ArrayList<String> ImgData;
	ArrayList<String> image_name_list = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		b = getIntent().getExtras();

		noImage = (TextView) findViewById(R.id.noImage);
		upload = (Button) findViewById(R.id.btnUpload);
		pick = (Button) findViewById(R.id.btnPicture);
		gv = (GridView) findViewById(R.id.gridview);
		gv.setAdapter(new ImageAdapter(this));

		if (b != null) {
			ImgData = b.getStringArrayList("IMAGE");
			for (int i = 0; i < ImgData.size(); i++) {
				map.add(ImgData.get(i).toString());
				String path = String.valueOf(ImgData.get(i).toString());//it contain your path of image..im using a temp string..
				String filename = path.substring(path.lastIndexOf("/") + 1);
				Toast.makeText(this, filename, Toast.LENGTH_SHORT).show();
				image_name_list.add(filename);
			}
		} else {
			noImage.setVisibility(View.VISIBLE);
		}

		upload.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
			   // if(count < image_name_list.size())
			    {
                    //new ImageUploadTask().execute(count + "", "pk" + count + ".jpg");
                   new ImageUploadTask().execute(count+"",String.valueOf(count)+".jpg");
                }
				//new ImageUploadTask().execute();
			}
		});

		pick.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent i3 = new Intent(MainActivity.this, UploadActivity.class);
				startActivity(i3);
			}
		});

	}

	class ImageUploadTask extends AsyncTask<String, Void, String> {

		String sResponse = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(MainActivity.this, "Uploading",
					"Please wait...", true);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				String url = "http://diamondcoaches.us-west-2.elasticbeanstalk.com/api/ECO/postImgexp";
				//"https://www.empulseitbangalore.com//ECO/api/ECO/postImgexp";
				//for (int i = 0; i < ImgData.size(); i++)
				{
					int j = Integer.parseInt(params[0]);
					Bitmap bitmap = decodeFile(ImgData.get(j));
					HttpClient httpClient = new DefaultHttpClient();
					HttpContext localContext = new BasicHttpContext();
					HttpPost httpPost = new HttpPost(url);
					entity = new MultipartEntity();

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.JPEG, 100, bos);
					byte[] data = bos.toByteArray();

				/*entity.addPart("user_id", new StringBody("199"));
				entity.addPart("club_id", new StringBody("10"));*/

					entity.addPart("Files", new ByteArrayBody(data, "image/jpg", params[1]));
					//entity.addPart("Files", new ByteArrayBody(data, "image/jpg", params[2]));

					httpPost.setEntity(entity);
					HttpResponse response = httpClient.execute(httpPost, localContext);
					sResponse = EntityUtils.getContentCharSet(response.getEntity());
					System.out.println("sResponse : " + sResponse);
				}
				} catch(Exception e){
					if (dialog.isShowing())
						dialog.dismiss();
					Log.e(e.getClass().getName(), e.getMessage(), e);
				}
			return sResponse;
		}

		@Override
		protected void onPostExecute(String sResponse) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
				if (sResponse != null) {
					Toast.makeText(getApplicationContext(),
							sResponse + " Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                    if (count < image_name_list.size()) {
                        new ImageUploadTask().execute(count + "", "hm" + count + ".jpg");
                    }
                    count++;
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}
	}

	public Bitmap decodeFile(String filePath) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);
		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;
		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);
		return bitmap;
	}

	private class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return map.size();
		}

		public Object getItem(int position) {
			return null;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85,
						Gravity.CENTER));
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setPadding(1, 1, 1, 1);

			} else {
				imageView = (ImageView) convertView;
			}

			imageView
					.setImageBitmap(BitmapFactory.decodeFile(map.get(position)));
			return imageView;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		MainActivity.this.finish();
	}
}
