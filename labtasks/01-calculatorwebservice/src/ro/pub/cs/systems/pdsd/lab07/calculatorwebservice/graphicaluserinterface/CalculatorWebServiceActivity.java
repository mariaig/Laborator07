package ro.pub.cs.systems.pdsd.lab07.calculatorwebservice.graphicaluserinterface;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import ro.pub.cs.systems.pdsd.lab07.calculatorwebservice.R;
import ro.pub.cs.systems.pdsd.lab07.calculatorwebservice.general.Constants;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class CalculatorWebServiceActivity extends Activity {
	
	private EditText operator1EditText, operator2EditText;
	private TextView resultTextView;
	private Spinner operationsSpinner, methodsSpinner;
	private String content="";
	
	private class CalculatorWebServiceThread extends Thread {
		
		@Override
		public void run() {
			
			// TODO: exercise 4
			// get operators 1 & 2 from corresponding edit texts (operator1EditText, operator2EditText)
			// signal missing values through error messages
			// get operation from operationsSpinner
			String operator1 = operator1EditText.getText().toString();
			String operator2 = operator2EditText.getText().toString();
			operationsSpinner = (Spinner) findViewById(R.id.operations_spinner);
			String operation = operationsSpinner.getSelectedItem().toString();
			
			if(operator1 == null || operator2 == null || "".equals(operator1) || "".equals(operator2)) {
				Log.e(Constants.TAG, "Both operators should be set!");
				return;
			}
			
			// create an instance of a HttpClient object
			HttpClient httpClient = new DefaultHttpClient();
			
			// get method used for sending request from methodsSpinner
			methodsSpinner = (Spinner)findViewById(R.id.methods_spinner);
			String method = methodsSpinner.getSelectedItem().toString();
			
			//ResponseHandler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			
			if("GET".equals(method)) {
				// 1. GET
				// a) build the URL into a HttpGet object (append the operators / operations to the Internet address)
				// b) create an instance of a ResultHandler object
				// c) execute the request, thus generating the result
				
				//a
				HttpGet httpGet = new HttpGet(Constants.GET_WEB_SERVICE_ADDRESS
                        + "?" + Constants.OPERATION_ATTRIBUTE + "=" + operation
                        + "&" + Constants.OPERATOR1_ATTRIBUTE + "=" + operator1
                        + "&" + Constants.OPERATOR2_ATTRIBUTE + "=" + operator2);
				
				
				//c
				try {
					content = httpClient.execute(httpGet, responseHandler);
				} catch (Exception e) {
					  Log.e(Constants.TAG, e.getMessage());
					  if (Constants.DEBUG) {
					    e.printStackTrace();
					  }
				}
				
			} else if("POST".equals(method)) {
				// 2. POST
				// a) build the URL into a HttpPost object
				// b) create a list of NameValuePair objects containing the attributes and their values (operators, operation)
				// c) create an instance of a UrlEncodedFormEntity object using the list and UTF-8 encoding and attach it to the post request
				// d) create an instance of a ResultHandler object
				// e) execute the request, thus generating the result
				
				//a
				HttpPost httpPost = new HttpPost(Constants.POST_WEB_SERVICE_ADDRESS);
				
				//b
				List<NameValuePair> params = new ArrayList<NameValuePair>();        
				params.add(new BasicNameValuePair(Constants.OPERATION_ATTRIBUTE, operationsSpinner.getSelectedItem().toString()));
				params.add(new BasicNameValuePair(Constants.OPERATOR1_ATTRIBUTE, operator1));
				params.add(new BasicNameValuePair(Constants.OPERATOR2_ATTRIBUTE, operator2));
				
				try {
					//c
					UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
					httpPost.setEntity(urlEncodedFormEntity);
					
					//e
					try {
						content = httpClient.execute(httpPost, responseHandler);
					} catch (Exception e) {
						  Log.e(Constants.TAG, e.getMessage());
						  if (Constants.DEBUG) {
						    e.printStackTrace();
						  }
					}
					
				} catch (UnsupportedEncodingException unsupportedEncodingException) {
				  Log.e(Constants.TAG, unsupportedEncodingException.getMessage());
				  if (Constants.DEBUG) {
				    unsupportedEncodingException.printStackTrace();
				  }						
				}
			}
			// display the result in resultTextView
			resultTextView.post(new Runnable() {
				
				@Override
				public void run() {
					resultTextView.setText(content);
					
				}
			});
		}
	}
	
	private DisplayResultButtonClickListener displayResultButtonClickListener = new DisplayResultButtonClickListener();
	private class DisplayResultButtonClickListener implements Button.OnClickListener {
		
		@Override
		public void onClick(View view) {
			new CalculatorWebServiceThread().start();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculator_web_service);
		
		operator1EditText = (EditText)findViewById(R.id.operator1_edit_text);
		operator2EditText = (EditText)findViewById(R.id.operator2_edit_text);
		
		resultTextView = (TextView)findViewById(R.id.result_text_view);
		
		operationsSpinner = (Spinner)findViewById(R.id.operations_spinner);
		methodsSpinner = (Spinner)findViewById(R.id.methods_spinner);
		
		Button displayResultButton = (Button)findViewById(R.id.display_result_button);
		displayResultButton.setOnClickListener(displayResultButtonClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calculator_web, menu);
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
