package edu.ubbcluj.canvasAndroid.backend.repository.restApi;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import edu.ubbcluj.canvasAndroid.NavigationDrawerFragment;
import edu.ubbcluj.canvasAndroid.backend.repository.CoursesDAO;
import edu.ubbcluj.canvasAndroid.backend.util.network.CheckNetwork;
import edu.ubbcluj.canvasAndroid.model.ActiveCourse;

public class RestCoursesDAO extends AsyncTask<String, Void, String> implements
		CoursesDAO {

	private NavigationDrawerFragment ndf;
	private ArrayList<ActiveCourse> data;
	
	@Override
	protected String doInBackground(String... urls) {
		String response = "";

		for (String url : urls) {
			response = RestInformationDAO.getData(url);
		}

		data = new ArrayList<ActiveCourse>();
		
		if (!CheckNetwork.isNetworkOnline(null)) {
			return "No connection";
		}
		
		// Decode JSON data and getting an ActivityStream array
		String jsonSource = response.replace("while(1);", "");

		try {
			JSONArray jArr = new JSONArray(jsonSource);
			
			for (int i = 0; i < jArr.length(); i++) {
				JSONObject jObj = jArr.getJSONObject(i);
				data.add(convertJSONtoStr(jObj));
			}
			
		} catch (JSONException e) {
			Log.e("Json Courses", e.getMessage(), new Error());
		}
		
		return response;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		ndf.setActiveCourses(data);
	}

	private ActiveCourse convertJSONtoStr(JSONObject jObj) {
		
		ActiveCourse activeCourse = new ActiveCourse();
		
		try {
			activeCourse.setId(jObj.getInt("id"));
			activeCourse.setName(jObj.getString("name"));
			
//			Log.d("Course", jObj.getString("name"));
		} catch (JSONException e) {
			Log.e("JSON Courses", e.getMessage());
		}
		
		return activeCourse;
	}

	public NavigationDrawerFragment getNdf(){
		return ndf;
	}
	
	public void setNdf(NavigationDrawerFragment ndf){
		this.ndf = ndf;
	}
}