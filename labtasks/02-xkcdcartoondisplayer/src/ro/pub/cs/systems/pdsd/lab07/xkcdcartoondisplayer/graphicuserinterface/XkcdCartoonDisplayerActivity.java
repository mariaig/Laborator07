package ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.graphicuserinterface;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.R;
import ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.entities.XkcdCartoonInfo;
import ro.pub.cs.systems.pdsd.xkcdcartoondisplayer.general.Constants;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class XkcdCartoonDisplayerActivity extends Activity {
	
	private TextView xkcdCartoonTitleTextView;
	private ImageView xkcdCartoonImageView;
	private TextView xkcdCartoonUrlTextView;
	private Button previousButton, nextButton;
	private Bitmap image;
	String pageContent = "";
	String cartoonTitle = "";
	URL cartoonUrl = null;
	URL prevUrl = null;
	URL nextUrl = null;
	private class XkcdCartoonUrlButtonClickListener implements Button.OnClickListener {
		
		String xkcdComicUrl;
		
		public XkcdCartoonUrlButtonClickListener(String xkcdComicUrl) {
			this.xkcdComicUrl = xkcdComicUrl;
		}
		
		@Override
		public void onClick(View view) {
			new XkcdCartoonDisplayerAsyncTask().execute(xkcdComicUrl);
		}
	}
	
	private class XkcdCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XkcdCartoonInfo> {

		@Override
		protected XkcdCartoonInfo doInBackground(String... urls) {
			
			XkcdCartoonInfo xkcdCartoonInfo = new XkcdCartoonInfo();
			
			
			// TODO: exercise 5a)
			// 1. obtain the content of the web page (whose Internet address is stored in urls[0])
			// - create an instance of a HttpClient object
			// - create an instance of a HttpGet object
			// - create an instance of a ResponseHandler object
			// - execute the request, thus obtaining the web page source code
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(urls[0]);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			try {
				pageContent = httpClient.execute(httpGet, responseHandler);
				Log.i(Constants.TAG, pageContent);
			} catch (Exception e) {
				  Log.e(Constants.TAG, e.getMessage());
				  if (Constants.DEBUG) {
				    e.printStackTrace();
				  }
			}
			
			
			// 2. parse the web page source code
			Document document = Jsoup.parse(pageContent);
			Element htmlTag = document.child(0);
			
			// - cartoon title: get the tag whose id equals "ctitle"
//			Element divTagIdCtitle = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
//			xkcdCartoonInfo.setCartoonTitle(divTagIdCtitle.ownText());
			Element title = htmlTag.getElementById(Constants.CTITLE_VALUE);
			xkcdCartoonInfo.setCartoonTitle(title.ownText());
			Log.i(Constants.TAG, "TITLE: " + cartoonTitle);
			
			// - cartoon url
			//   * get the first tag whose id equals "comic"
			//   * get the embedded <img> tag
			//   * get the value of the attribute "src"
			//   * prepend the protocol: "http:"
			Element divTagIdComic = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
			String cartoonInternetAddress = divTagIdComic.getElementsByTag(Constants.IMG_TAG).attr(Constants.SRC_ATTRIBUTE);
			
			
			// - cartoon content: get the input stream attached to the url and decode it into a Bitmap
			
			try {
				cartoonUrl = new URL("http:"+cartoonInternetAddress);
				Log.i(Constants.TAG, "ADDRESS: " + cartoonUrl.toString());
				
				HttpURLConnection connection = (HttpURLConnection) cartoonUrl.openConnection();
				connection.setDoInput(true);
			    connection.connect();
			    InputStream input = connection.getInputStream();
			    image = BitmapFactory.decodeStream(input);
			    xkcdCartoonInfo.setCartoonContent(image);
				
			} catch(Exception e) {
				  Log.e(Constants.TAG, e.getMessage());
				  if (Constants.DEBUG) {
				    e.printStackTrace();
				  }
			}
			
			// - previous cartoon address
			//   * get the first tag whole rel attribute equals "prev"
			//   * get the href attribute of the tag
			//   * prepend the value with the base url: http://www.xkcd.com
			//   * attach the previous button a click listener with the address attached
			
			Element aTagRelPrev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
			String previousCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelPrev.attr(Constants.HREF_ATTRIBUTE);
			xkcdCartoonInfo.setPreviousCartoonUrl(previousCartoonInternetAddress);
		    Log.i(Constants.TAG, "PREV ADDRESS: " + previousCartoonInternetAddress);

			
			
			
			// - next cartoon address
			//   * get the first tag whole rel attribute equals "next"
			//   * get the href attribute of the tag
			//   * prepend the value with the base url: http://www.xkcd.com
			//   * attach the next button a click listener with the address attached
			Element aTagRelNext = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
			String nextCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelNext.attr(Constants.HREF_ATTRIBUTE);
			xkcdCartoonInfo.setNextCartoonUrl(nextCartoonInternetAddress);
			Log.i(Constants.TAG, "NEXT ADDRESS: " + previousCartoonInternetAddress);
			
			
			
			return xkcdCartoonInfo;

		}
		
		@Override
		protected void onPostExecute(XkcdCartoonInfo xkcdCartoonInfo) {
			
			// TODO: exercise 5b)
			// map each member of xkcdCartoonInfo object to the corresponding widget
			// cartoonTitle -> xkcdCartoonTitleTextView
			// cartoonContent -> xkcdCartoonImageView
			// cartoonUrl -> xkcdCartoonUrlTextView
			

			xkcdCartoonTitleTextView.setText(xkcdCartoonInfo.getCartoonTitle());
			xkcdCartoonImageView.setImageBitmap(xkcdCartoonInfo.getCartoonContent());
			xkcdCartoonUrlTextView.setText(xkcdCartoonInfo.getCartoonUrl());

			previousButton.setOnClickListener(new XkcdCartoonUrlButtonClickListener(xkcdCartoonInfo.getPreviousCartoonUrl()));
			nextButton.setOnClickListener(new XkcdCartoonUrlButtonClickListener(xkcdCartoonInfo.getNextCartoonUrl()));
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xkcd_cartoon_displayer);
		
		xkcdCartoonTitleTextView = (TextView)findViewById(R.id.xkcd_cartoon_title_text_view);
		xkcdCartoonImageView = (ImageView)findViewById(R.id.xkcd_cartoon_image_view);
		xkcdCartoonUrlTextView = (TextView)findViewById(R.id.xkcd_cartoon_url_text_view);
		
		previousButton = (Button)findViewById(R.id.previous_button);
		nextButton = (Button)findViewById(R.id.next_button);
		
		new XkcdCartoonDisplayerAsyncTask().execute(Constants.XKCD_INTERNET_ADDRESS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.xkcd_cartoon_displayer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
