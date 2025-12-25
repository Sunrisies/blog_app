package com.sunrise.blog

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings

@Composable
fun MapboxScreen(navController: NavController) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(2.0)
            center(Point.fromLngLat(-98.0, 39.5))
            pitch(0.0)
            bearing(0.0)
        }
    }

    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
    ) {
        // 可以在此处添加地图交互设置
//        gestures {
//            // 启用或禁用特定手势
//            val gesturesSettings = GesturesSettings().apply {
//                rotateEnabled = true
//                pitchEnabled = true
//                zoomEnabled = true
//                scrollEnabled = true
//            }
//            updateGesturesSettings(gesturesSettings)
//        }
    }
}
