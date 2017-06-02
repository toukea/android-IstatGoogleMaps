package istat.android.tools.geography.gmap;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;

public abstract class MarkerAdapter<Item> implements OnInfoWindowClickListener,
		OnMarkerClickListener {
	Context mContext;
	GoogleMap mMap;
	List<Item> items = new ArrayList<Item>();
	List<Marker> markers = new ArrayList<Marker>();
	private Item selectedItem;

	private void addMarker(Item item) {
		Marker mkr = onAddMarker(item);
		markers.add(mkr);
	}

	public void notifyDataSetChanged() {
		removeMarkers();
		for (Item item : items) {
			addMarker(item);
		}

	}

	public MarkerAdapter(Context context, GoogleMap map) {
		mMap = map;
		mContext = context;
		mMap.setOnInfoWindowClickListener(this);
		mMap.setOnMarkerClickListener(this);
	}

	public abstract Marker onAddMarker(Item item);

	@Override
	public void onInfoWindowClick(Marker marker) {
		
		int id = markers.indexOf(marker);
		if (mOnItemMarkerSelected != null) {
			if (!mOnItemMarkerSelected.onDisplayItemMarkerInfoWindow(
					selectedItem, id)) {
				marker.hideInfoWindow();
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		
		int index = markers.indexOf(marker);
		selectedItem = items.get(index);
		if (mOnItemMarkerSelected != null)
			mOnItemMarkerSelected.onItemMarkerSelected(selectedItem, index);
		return false;
	}

	public void removeMarkers() {
		mMap.clear();
		items.clear();
	}

	public int getSelectedItemIndex() {
		if (selectedItem == null)
			return -1;
		return items.indexOf(selectedItem);
	}

	public void setOnItemMarkerSelected(
			OnItemMarkerSelected<Item> onItemMarkerSelected) {
		mOnItemMarkerSelected = onItemMarkerSelected;
	}

	public void setItems(List<Item> phs) {
		removeMarkers();
		addItems(phs);
		notifyDataSetChanged();
	}

	public void addItem(Item ph) {
		items.add(ph);
	}

	public void addItems(List<Item> phs) {
		items.addAll(phs);
	}

	public Item getItem(int index) {
		return getItems().get(index);
	}

	public List<Item> getItems() {
	
		return items;
	}

	public GoogleMap getMap() {
		return mMap;
	}

	public Context getContext() {
		return mContext;
	}

	public Item getSelectedItem() {
		return selectedItem;
	}

	public Marker getSelectedMarker() {
		int index = getSelectedItemIndex();
		if (index >= 0) {
			return markers.get(index);
		} else {
			return null;
		}
	}

	public Item getItemWithMarker(Marker marker) {
		return getItems().get(markers.indexOf(marker));
	}

	public static interface OnItemMarkerSelected<Item> {
		public void onItemMarkerSelected(Item item, int index);

		public boolean onDisplayItemMarkerInfoWindow(Item item, int index);
	}

	private OnItemMarkerSelected<Item> mOnItemMarkerSelected;
}
