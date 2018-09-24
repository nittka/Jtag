package de.nittka.tooling.jtag.ui.search

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.GpsDirectory
import de.nittka.tooling.jtag.ui.JtagFileURIs
import java.io.File
import java.util.List
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchResult

class JtagGpsPreview {

	@Inject
	Provider<XtextResourceSet> rsProvider;
	int resultCount=0

	def String createHtml(ReferenceSearchResult searchResult){
		resultCount=0
		val List<IReferenceDescription> matches=searchResult.matchingReferences
		val rs=rsProvider.get
		val html='''
			<html>
			<head>
			  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
			  <style>
			   .popup img{
			    max-width:200px;
			    max-height:200px;
			   }
			  </style>
			</head>
			<body>
			  <div id="mapdiv"></div>
			  <script src="http://www.openlayers.org/api/OpenLayers.js"></script>
			  <script>
			    var gpsMarkers = [
			      «FOR entry: matches.map[render(rs)].filterNull SEPARATOR ","»
			        «entry»
			      «ENDFOR»
			    ];

			    map = new OpenLayers.Map("mapdiv");
			    map.addLayer(new OpenLayers.Layer.OSM());
			
			
			    var markers = new OpenLayers.Layer.Markers( "Markers" );
			    map.addLayer(markers);
			
			    var bounds =new OpenLayers.Bounds();
			    for (var i = 0; i < gpsMarkers.length; i++){
			      var obj = gpsMarkers[i];
			      addMarker(obj["id"], obj["lon"], obj["lat"], obj["title"], obj["imgLoc"]);
			    }
			    if(gpsMarkers.length>0){
			      var zoom=map.getZoomForExtent(markers.getDataExtent())
			      if(zoom > 11){
			        zoom = 11;
			      }
			      map.setCenter(bounds.getCenterLonLat(), zoom);
			    } else {
			      map.setCenter(bounds.getCenterLonLat(), 3);
			    }
			
			    function addMarker(id, lon, lat, title, imgLocation){
			      var lonLat = new OpenLayers.LonLat(lon,lat)
			          .transform(
			            new OpenLayers.Projection("EPSG:4326"), // Transformation aus dem Koordinatensystem WGS 1984
			            map.getProjectionObject() // in das Koordinatensystem 'Spherical Mercator Projection'
			          );
			      bounds.extend(lonLat);
			      var marker=new OpenLayers.Marker(lonLat);
			      markers.addMarker(marker);
			      marker.events.register('mouseover', marker, function() {
			        var popup = new OpenLayers.Popup.FramedCloud(id, lonLat, null, 
			        '<div class="popup"><div>'+title+'</div><img src="'+imgLocation+'" title="'+imgLocation+'"></div>', 
			        null, false);
			        map.addPopup(popup);
			        marker.events.register('mouseout', marker, setTimeout( function() { popup.destroy(); }, 4000));
			      });
			    }
			  </script>
			</body></html>
		'''
		return if(resultCount>0){
			html
		}else{
			null
		}
	}

	def private String render(IReferenceDescription desc, XtextResourceSet rs){
		val EObject e=rs.getEObject(desc.targetEObjectUri,true)
		if(e instanceof de.nittka.tooling.jtag.jtag.File){
			val file=e as de.nittka.tooling.jtag.jtag.File
			val location=JtagFileURIs.getImageLocation(file)
			if(location!==null){
				val javaFile=new File(location)
				return render(javaFile)
			}
		}
		return null
	}

	def private String render(File f){
		if(f.exists){
			val gpsLoc=getGpsLocation(f)
			if(gpsLoc!==null){
				resultCount=resultCount+1
				return '''{"id":"id«resultCount»", "lon":"«gpsLoc.longitude»", "lat":"«gpsLoc.latitude»", "title":"«f.name»", "imgLoc":"«f.toPath.toUri»"}'''
			}
		}
	}

	def private getGpsLocation(File f){
		val Metadata metadata = ImageMetadataReader.readMetadata(f);
		val dir= metadata.directories.filter(GpsDirectory).head
		if(dir!==null){
			val loc=(dir as GpsDirectory).geoLocation
			if(loc!==null){
				return loc
			}
		}
	}
}