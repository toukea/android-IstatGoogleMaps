package istat.android.tools.geography.gmap;

/**
 * <h1>GmapZoomer<h1>
 * this class allow you to make a google map's multiple zoom states.
 * @author Toukea tatsi jephte (Istat)
 */
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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
public class GmapZoomer {
	private Context context;
	private GoogleMap mMap;
	private boolean inRun = false;
	private boolean stopMotionOnClick = true, stopMotionOnScroll = true;
	public static String EXTRA_WAIT = "wait", EXTRA_ZOOM = "zoom",
			EXTRA_TIME = "time", EXTRA_LAT = "lat", EXTRA_LGNT = "lgt";
	protected Handler ZommerHandler = new Handler() {
		public void handleMessage(Message msg) {
			onZoomComplete(msg);
		};
	};

	public GmapZoomer(Context context, GoogleMap map) {
		this.context = context;
		this.mMap = map;
		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				// TODO Auto-generated method stub
				if (stopMotionOnClick)
					stopZoomTask();
			}
		});
	}

	public ZoomState obteinewZoomState() {
		return new ZoomState();
	}

	public ZoomState obteinewZoomState(int wait, float zoom, LatLng point) {
		return new ZoomState(wait, zoom, point);
	}

	public ZoomState obteinewZoomState(float zoom, LatLng point) {
		return new ZoomState(0, zoom, point);
	}

	public ZoomState obteinewZoomState(int wait, float zoom, float lat,
			float lng) {
		return new ZoomState(wait, zoom, new LatLng(lat, lng));
	}

	public ZoomState obteinewZoomState(float zoom, float lat, float lng) {
		return new ZoomState(zoom, new LatLng(lat, lng));
	}

	public void onZoomComplete(Message msg) {
		// TODO WHEN TASK COMPLETE
	}

	public void onZoomTaskStart() {
		// TODO WHEN TASK START
		inRun = true;

	}

	public void stopZoomTask() {
		inRun = false;

		try {
			mMap.stopAnimation();
		} catch (Exception e) {
		}
	}

	public void executeZoomMotion(ZoomState... states) {
		executeZoomMotion(java.util.Arrays.asList(states));
	}

	public void executeZoomMotion(final List<ZoomState> zoomState) {
		onZoomTaskStart();
		new Thread() {
			@Override
			public void run() {
				for (ZoomState state : zoomState) {
					if (!inRun)
						break;
					try {
						state.executeZoom().join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}.start();
	}

	public Context getContext() {
		return context;
	}

	public boolean isRunning() {
		return inRun;
	}

	public void setStopMotionOnClick(boolean stopMotionOnClick) {
		this.stopMotionOnClick = stopMotionOnClick;
	}

	public boolean isStopMotionOnClick() {
		return stopMotionOnClick;
	}

	public boolean isStopMotionOnScroll() {
		return stopMotionOnScroll;
	}

	public void setStopMotionOnScroll(boolean stopMotionOnScroll) {
		this.stopMotionOnScroll = stopMotionOnScroll;
	}

	/**
	 * <h1>ZoomState<h1>
	 * this class represent a Zoom state that can be add to the GmapZoomer in
	 * order tho perform a Google map Zoom.
	 * 
	 * @author Toukea tatsi jephte (Istat)
	 */
	public class ZoomState {
		private int wait = 0;
		private float zoom = 0;
		private LatLng point = new LatLng(0, 0);
		private int zoomTime = -1;
		private float bearing = 0;
		private float titl = 0;
		private boolean zoomStateTaskinRun = false;
		private Handler handler;
		private CancelableCallback callback = new CancelableCallback() {

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				if (isStopMotionOnScroll())
					stopZoomTask();
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub

			}

		};

		private ZoomState() {
			initHandler();
		}

		private ZoomState(int wait, CameraPosition cmPosition) {
			this.wait = wait;
			this.zoom = cmPosition.zoom;
			this.point = cmPosition.target;
			initHandler();
		}

		private ZoomState(CameraPosition cmPosition) {
			this.wait = 0;
			this.zoom = cmPosition.zoom;
			this.point = cmPosition.target;
			initHandler();
		}

		private ZoomState(final float zoom, final LatLng point) {
			wait = 0;
			this.zoom = zoom;
			this.point = point;
			initHandler();
		}

		private ZoomState(final int wait, final float zoom, final LatLng point) {
			this.wait = wait;
			this.zoom = zoom;
			this.point = point;
			initHandler();
		}

		void initHandler() {
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					try {
						if (point != null && mMap != null) {
							if (zoomTime < 0)
								mMap.animateCamera(CameraUpdateFactory
										.newCameraPosition(getCameraUpdate()),
										callback);
							else
								mMap.animateCamera(CameraUpdateFactory
										.newCameraPosition(getCameraUpdate()),
										zoomTime, callback);

						}
						zoomStateTaskinRun = false;
						Bundle bundle = new Bundle();
						bundle.putDouble(EXTRA_WAIT, wait);
						bundle.putDouble(EXTRA_ZOOM, zoom);
						bundle.putDouble(EXTRA_LAT, point.latitude);
						bundle.putDouble(EXTRA_WAIT, point.longitude);
						Message message = new Message();
						message.setData(bundle);
						ZommerHandler.sendMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			};
		}

		private CameraPosition getCameraUpdate() {
			CameraPosition motion = new CameraPosition.Builder().target(point)
					.zoom(zoom).bearing(bearing).tilt(titl).build();
			return motion;
		}

		private Thread executeZoom() {
			zoomStateTaskinRun = true;
			Thread thread = new Thread() {
				public void run() {
					try {
						Thread.sleep(wait);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message msg = handler.obtainMessage();

					if (inRun && zoomStateTaskinRun)
						handler.sendMessage(msg);
				}

			};
			thread.start();
			return thread;
		}

		public void stopZoomStateTask() {
			zoomStateTaskinRun = false;
		}

		public ZoomState setZoomWait(int wait) {
			this.wait = wait;
			return this;
		}

		public ZoomState setZoomLevel(float level) {
			this.zoom = level;
			return this;
		}

		public ZoomState setZoomTime(int time) {
			this.zoomTime = time;
			initHandler();
			return this;
		}

		public ZoomState setpointCoordinate(final LatLng point) {
			this.point = point;
			return this;
		}

		public ZoomState setPoint(float lat, float lng) {
			point = new LatLng(lat, lng);
			return this;
		}

		public ZoomState setPoint(LatLng point) {
			this.point = point;
			return this;
		}

		public ZoomState setCallback(CancelableCallback callback) {
			this.callback = callback;
			return this;
		}

		public ZoomState setBearing(float bearing) {
			this.bearing = bearing;
			return this;
		}

		public ZoomState setTitl(float titl) {
			if (titl > 90)
				titl = 90;
			else if (titl < 0)
				titl = 0;
			this.titl = titl;

			return this;
		}

		public float getBearing() {
			return bearing;
		}

		public CancelableCallback getCallback() {
			return callback;
		}

		public LatLng getPoint() {
			return point;
		}

		public float getTitl() {
			return titl;
		}

		public int getWait() {
			return wait;
		}

		public float getZoom() {
			return zoom;
		}

		public int getZoomTime() {
			return zoomTime;
		}

		public ZoomStateList setNextZoom(final ZoomState state) {
			callback = new CancelableCallback() {

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					// if(callback!=null)callback.onFinish();
					executeZoomMotion(state);
					// Toast.makeText(context, "calback:"+zoom,
					// Toast.LENGTH_LONG).show();
				}

				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					if (state.callback != null)
						state.callback.onCancel();
				}
			};

			return new ZoomStateList().setNextZoom(state);
		}

		public ZoomState clone() {
			ZoomState state = new ZoomState();
			state.setBearing(bearing);
			state.setCallback(callback);
			state.setPoint(point);
			state.setTitl(titl);
			state.setZoomLevel(zoom);
			state.setZoomTime(zoomTime);
			state.setZoomWait(wait);
			return state;

		}

		public boolean isRunning() {
			return zoomStateTaskinRun;
		}

	}

	/**
	 * <h1>ZoomStateList<h1>
	 * this class represent a ZoomState List.
	 * 
	 * @author Toukea tatsi jephte (Istat)
	 */
	public static class ZoomStateList {

		private List<ZoomState> zoomList = new ArrayList<ZoomState>();

		public List<ZoomState> getZoomStateList() {
			return zoomList;
		}

		public ZoomStateList setNextZoom(final ZoomState state) {
			if (zoomList.size() > 0) {
				zoomList.get(zoomList.size() - 1).setNextZoom(state);
			}
			zoomList.add(state);
			return this;
		}
	}

	// ---------------------------------------------------------------------
	public class ZoomLevelComputer {

	}
}
