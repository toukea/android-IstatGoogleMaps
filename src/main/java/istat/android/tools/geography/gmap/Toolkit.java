package istat.android.tools.geography.gmap;

import com.google.android.gms.maps.model.LatLng;

public class Toolkit {
	public static LatLng midleLatLng(LatLng... point) {
		double latitudeSum=0,longitudeSum=0;
		for(LatLng tmp:point){
			latitudeSum+=tmp.latitude;
			longitudeSum+=tmp.longitude;
		}
		return new LatLng(latitudeSum/point.length, longitudeSum/point.length);
	}
}
