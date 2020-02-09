/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.search

import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.ui.JtagFileURIs
import java.util.List
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchResult

class JtagSearchResultPreview {

	@Inject
	Provider<XtextResourceSet> rsProvider;

	def String createHtml(ReferenceSearchResult searchResult){
	val List<IReferenceDescription> matches=searchResult.matchingReferences
	val rs=rsProvider.get
	'''
		<html>
		  <head>
		    <meta charset="ISO-8859-1">
		    <meta http-equiv="X-UA-Compatible" content="IE=edge">
		    <meta http-equiv="Content-Type" content="text/xhtml;charset=ISO-8859-1">
		    <meta name="viewport" content="width=device-width,initial-scale=1.0">
		    <title>Jtag Search Preview</title>
		    <style>
		    body {
		      font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif;
		      font-size: 10pt;
		      line-height: 14pt;
		      letter-spacing: 0.02rem;
		      color: #000000;
		      /* Permalink - use to edit and share this gradient: http://colorzilla.com/gradient-editor/#607080+0,506070+100 */
		      background: #607080; /* Old browsers */
		      background: -moz-linear-gradient(top, #607080 0%, #506070 100%); /* FF3.6-15 */
		      background: -webkit-linear-gradient(top, #607080 0%,#506070 100%); /* Chrome10-25,Safari5.1-6 */
		      background: linear-gradient(to bottom, #607080 0%,#506070 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
		      filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#607080', endColorstr='#506070',GradientType=0 ); /* IE6-9 */
		    }
		    .container {
		      display: flex;
		      flex-direction: row;
		      flex-wrap: wrap;
		    }
		    .padded {
		      padding : 15px;
		    }
		    .shadow {
		      -webkit-box-shadow: 0px 2px 12px 0px rgba(0,0,0,0.5);
		      -moz-box-shadow: 0px 2px 12px 0px rgba(0,0,0,0.5);
		      box-shadow: 0px 2px 12px 0px rgba(0,0,0,0.5);
		    }
		    .card {
		      margin-right : 30px;  /* 2 times value of css class .padded above */
		      margin-bottom : 30px;  /* 2 times value of css class .padded above */
		      background-color : #ffffff;
		      width : 240px;
		      min-height : 300px;
		    }
		    img{
		      max-width:100%;
		      max-height:100%;
		      width: auto;
		      height: auto;
		      position: absolute;  
		      top: 0;  
		      bottom: 0;  
		      left: 0;  
		      right: 0;  
		      margin: auto;
		    }
		    .image {
		      background-color : #203040;
		      width : 240px;
		      height : 240px;
		      position: relative;
		    }
		    .image a{
		      position: absolute;
		      padding : 15px;
		      text-alginment: bottom;
		      color : #ffffff;
		    }
		    h1 {
		      margin:0;
		      font-size: 14.4pt;
		      letter-spacing: -0.02rem;
		    }
		  </style>
		</head>
		<body>
		  <div class="container padded">
		  «FOR desc:matches»
		    «desc.render(rs)»
		  «ENDFOR»
		   </div>
		</body>
		</html>
	'''
	}

	def private String render(IReferenceDescription desc, XtextResourceSet rs){
		val EObject e=rs.getEObject(desc.targetEObjectUri,true)
		if(e instanceof File){
			val file=e as File
			val location=JtagFileURIs.getImageLocation(file)
			var String imageContent
			if(location!==null){
				val javaFile=new java.io.File(location)
				imageContent='''<img src="file://«location»" title="«javaFile.name»  -  «javaFile.parentFile.absolutePath»">'''
			}else {
				val path=JtagFileURIs.getFilePath(file);
				if(path!==null){
					imageContent='''<a href="file://«path»" title="«path»">«file.fileName.fileName»</a>'''
				}
			}
			if(imageContent!=null)
			'''
					<div class="card shadow">
					    <div class="image">«imageContent»</div>
					    <div class="padded">
					      «IF !file.title.nullOrEmpty» <h1>«file.title»</h1>«ENDIF»
					      «IF !file.description.nullOrEmpty»<p>«file.description»</p>«ENDIF»
					    </div>
					</div>
			'''
		}
	}
}