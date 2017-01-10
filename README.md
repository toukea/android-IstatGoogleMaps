# Istat Gmaps
An Android Library allowing to perform smart and pretty Zoom using Google Map motion.

# Perform multiple Zoom and motion

 ```java
    GmapZoomer zoomer = new GmapZoomer(mContext, mMap);
    GmapZoomer.ZoomState state = zoomer.newZoomState(15,16f,3.23245f,4.11222f);
    GmapZoomer.ZoomState state2 = zoomer.newZoomState(15,16f,3.23245f,4.11222f);
    state.setNextZoom(state2);
    zoomer.executeZoomMotion(state);
 ```

# Trail map route using Google direction

Document in progress... ;-)
