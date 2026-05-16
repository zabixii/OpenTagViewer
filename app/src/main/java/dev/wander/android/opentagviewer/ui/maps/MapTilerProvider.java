package dev.wander.android.opentagviewer.ui.maps;

import android.os.Bundle;
import android.view.View;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import dev.wander.android.opentagviewer.R;

public class MapTilerProvider implements IMapProvider {
    private MapView mapView;

    @Override
    public void onCreate(View rootView, Bundle savedInstanceState) {
        String key = "MAPTILER_KEY_PLACEHOLDER";
        // Updated from Mapbox.getInstance to MapLibre.getInstance
        MapLibre.getInstance(rootView.getContext());
        
        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        
        String styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=" + key;
        mapView.getMapAsync(map -> map.setStyle(new Style.Builder().fromUri(styleUrl)));
    }

    @Override public void onStart() { if(mapView != null) mapView.onStart(); }
    @Override public void onResume() { if(mapView != null) mapView.onResume(); }
    @Override public void onPause() { if(mapView != null) mapView.onPause(); }
    @Override public void onStop() { if(mapView != null) mapView.onStop(); }
    @Override public void onDestroy() { if(mapView != null) mapView.onDestroy(); }
}
