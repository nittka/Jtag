package de.nittka.tooling.jtag.ui.search

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.GpsDirectory
import de.nittka.tooling.jtag.jtag.Folder
import de.nittka.tooling.jtag.ui.JtagFileURIs
import java.io.File
import java.util.List
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchResult
import java.util.Collection
import org.eclipse.core.resources.IFile

class JtagGpsPreview {

	@Inject
	Provider<XtextResourceSet> rsProvider;

	def String createHtml(ReferenceSearchResult searchResult){
		val List<IReferenceDescription> matches=searchResult.matchingReferences
		val rs=rsProvider.get
		val markers= matches.map[render(rs)]
		return createHtml(markers)
	}

	def String createHtml(Folder jtagFolder){
		val markers=jtagFolder.files.map[render]
		return createHtml(markers)
	}

	def String createHtml(Collection<IFile> files){
		val markers=files.map[render(it.location.toFile)].toList
		return createHtml(markers)
	}

	def private String createHtml(List<String> markers){
		val nonNullMarkers=markers.filterNull.toList
		if(nonNullMarkers.empty){
			return null
		}
		return 
		'''
			<html>
			<head>
			  <meta http-equiv="content-type" content="text/html; charset=ISO_8859_1">
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
			      «FOR entry: nonNullMarkers SEPARATOR ","»
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
			      addMarker('id'+(i+1), obj["lon"], obj["lat"], obj["title"], obj["imgLoc"]);
			    }
			    if(gpsMarkers.length>0){
			      var zoom=map.getZoomForExtent(markers.getDataExtent())
			      if(zoom > 13){
			        zoom = 13;
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
	}

	def private String render(IReferenceDescription desc, XtextResourceSet rs){
		val EObject e=rs.getEObject(desc.targetEObjectUri,true)
		if(e instanceof de.nittka.tooling.jtag.jtag.File){
			return render(e as de.nittka.tooling.jtag.jtag.File)
		}
		return null
	}

	def private String render(de.nittka.tooling.jtag.jtag.File file){
		val location=JtagFileURIs.getImageLocation(file)
		if(location!==null){
			val javaFile=new File(location)
			return render(javaFile)
		}
		return null
	}

	def private String render(File f){
		if(f.exists){
			val gpsLoc=getGpsLocation(f)
			if(gpsLoc!==null){
				return '''{"lon":"«gpsLoc.longitude»", "lat":"«gpsLoc.latitude»", "title":"«f.name»", "imgLoc":"«f.toPath.toUri»"}'''
			}
		}
	}

	def private getGpsLocation(File f){
		try{
			val Metadata metadata = ImageMetadataReader.readMetadata(f);
			val dir= metadata.directories.filter(GpsDirectory).head
			if(dir!==null){
				val loc=(dir as GpsDirectory).geoLocation
				if(loc!==null){
					return loc
				}
			}
		} catch(Exception e){
			//ignore
		}
	}
}