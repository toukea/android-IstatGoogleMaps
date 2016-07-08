package istat.android.tools.geography.gmap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.AsyncTask;
import android.util.Log;

/*
 * Copyright (C) 2014 Istat Dev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 * @author Toukea Tatsi (Istat)
 * 
 */
public class GoogleDirection {
	public static String URL_DIRECTION = "http://maps.googleapis.com/maps/api/directions/xml",
			PARAMS_ORIGINE = "origine",
			PARAMS_DESTINATION = "destination",
			PARAMS_SENSOR = "sensor",
			PARAMS_UNITS = "units",
			PARAMS_MODE = "mode";
	public final static String MODE_DRIVING = "driving";
	public final static String MODE_WALKING = "walking";
	private Document document;

	public GoogleDirection() {
	}

	public Document getDocument(LatLng start, LatLng end, String mode) {
		String url = "http://maps.googleapis.com/maps/api/directions/xml?"
				+ "origin=" + start.latitude + "," + start.longitude
				+ "&destination=" + end.latitude + "," + end.longitude
				+ "&sensor=false&units=metric&mode=driving";

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);
			HttpResponse response = httpClient.execute(httpPost, localContext);
			InputStream in = response.getEntity().getContent();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(in);
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private int getNodeIndex(NodeList nl, String nodename) {
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals(nodename))
				return i;
		}
		return -1;
	}

	private ArrayList<LatLng> decodePoly(String encoded) {
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(position);
		}
		return poly;
	}

	// --------------------------------------------------------------------------------------------------------
	public static AsyncDirectionQuery executeAsyncQuery(LatLng start, LatLng end, String mode,
			DirectionQueryCallBack callBack) {
		return new GoogleDirection().executeAsyncQuery(mode, callBack, start,
				end);
	}

	public AsyncDirectionQuery executeAsyncQuery(String mode, DirectionQueryCallBack callBack,
			LatLng... point) {
		AsyncDirectionQuery task = new AsyncDirectionQuery(mode, callBack);
		task.execute(point);
		return task;
	}

	public DirectionResponse executeQuery(LatLng start, LatLng end, String mode) {
		String url = "http://maps.googleapis.com/maps/api/directions/xml?"
				+ "origin=" + start.latitude + "," + start.longitude
				+ "&destination=" + end.latitude + "," + end.longitude
				+ "&sensor=false&units=metric&mode=" + mode;

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpGet, localContext);
			InputStream in = response.getEntity().getContent();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document doc = builder.parse(in);
			document = doc;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DirectionResponse();
	}

	public class DirectionResponse {
		public String getDurationText() {
			NodeList nl1 = document.getElementsByTagName("duration");
			Node node1 = nl1.item(nl1.getLength() - 1);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "text"));
			Log.i("DurationText", node2.getTextContent());
			return node2.getTextContent();
		}

		public int getDurationValue() {
			NodeList nl1 = document.getElementsByTagName("duration");
			Node node1 = nl1.item(nl1.getLength() - 1);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "value"));
			Log.i("DurationValue", node2.getTextContent());
			return Integer.parseInt(node2.getTextContent());
		}

		public String getDistanceText() {
			NodeList nl1 = document.getElementsByTagName("distance");
			Node node1 = nl1.item(nl1.getLength() - 1);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "text"));
			Log.i("DistanceText", node2.getTextContent());
			return node2.getTextContent();
		}

		public int getDistanceValue() {
			NodeList nl1 = document.getElementsByTagName("distance");
			Node node1 = nl1.item(nl1.getLength() - 1);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "value"));
			Log.i("DistanceValue", node2.getTextContent());
			return Integer.parseInt(node2.getTextContent());
		}

		public String getStartAddress() {
			NodeList nl1 = document.getElementsByTagName("start_address");
			Node node1 = nl1.item(0);
			Log.i("StartAddress", node1.getTextContent());
			return node1.getTextContent();
		}

		public String getEndAddress() {
			NodeList nl1 = document.getElementsByTagName("end_address");
			Node node1 = nl1.item(0);
			Log.i("StartAddress", node1.getTextContent());
			return node1.getTextContent();
		}

		public String getCopyRights() {
			NodeList nl1 = document.getElementsByTagName("copyrights");
			Node node1 = nl1.item(0);
			Log.i("CopyRights", node1.getTextContent());
			return node1.getTextContent();
		}

		public ArrayList<LatLng> getDirection() {
			NodeList nl1, nl2, nl3;
			ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
			nl1 = document.getElementsByTagName("step");
			if (nl1.getLength() > 0) {
				for (int i = 0; i < nl1.getLength(); i++) {
					Node node1 = nl1.item(i);
					nl2 = node1.getChildNodes();

					Node locationNode = nl2.item(getNodeIndex(nl2,
							"start_location"));
					nl3 = locationNode.getChildNodes();
					Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
					double lat = Double.parseDouble(latNode.getTextContent());
					Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
					double lng = Double.parseDouble(lngNode.getTextContent());
					listGeopoints.add(new LatLng(lat, lng));

					locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
					nl3 = locationNode.getChildNodes();
					latNode = nl3.item(getNodeIndex(nl3, "points"));
					ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
					for (int j = 0; j < arr.size(); j++) {
						listGeopoints.add(new LatLng(arr.get(j).latitude, arr
								.get(j).longitude));
					}

					locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
					nl3 = locationNode.getChildNodes();
					latNode = nl3.item(getNodeIndex(nl3, "lat"));
					lat = Double.parseDouble(latNode.getTextContent());
					lngNode = nl3.item(getNodeIndex(nl3, "lng"));
					lng = Double.parseDouble(lngNode.getTextContent());
					listGeopoints.add(new LatLng(lat, lng));
				}
			}

			return listGeopoints;
		}

		public PolylineOptions getPolylineAsItinerary() {
			PolylineOptions out = new PolylineOptions();
			out.addAll(getDirection());
			return out;

		}

	}

	public final class AsyncDirectionQuery extends AsyncTask<LatLng, Void, Void> {
		List<DirectionResponse> out = new ArrayList<DirectionResponse>();
		String mode;
		DirectionQueryCallBack callBack;

		public AsyncDirectionQuery(String mode, DirectionQueryCallBack callBack) {
			this.mode = mode;
			this.callBack = callBack;
		}

		@Override
		protected Void doInBackground(LatLng... params) {
			// TODO Auto-generated method stub
			for (LatLng latLng : params) {
				if (!isCancelled())
					out.add(executeQuery(params[0], latLng, mode));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (callBack != null && !isCancelled())
				callBack.onDirectionQueryComplete(out);
		}

	}

	public interface DirectionQueryCallBack {
		public void onDirectionQueryComplete(
				List<DirectionResponse> directionResponses);

	}
}