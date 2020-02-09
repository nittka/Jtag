/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.outline;

import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.EStructuralFeatureNode;

import de.nittka.tooling.jtag.jtag.Folder;
import de.nittka.tooling.jtag.jtag.JtagPackage;

public class IgnoreFileOutlineNode extends EStructuralFeatureNode {

	public IgnoreFileOutlineNode(IOutlineNode parent, Folder owner, Image image, String fileName) {
		super(owner, JtagPackage.eINSTANCE.getFolder_Ignore(), parent, image, fileName, true);
	}

	/**
	 * platform URI of the file corresponding to the Outline node
	 * */
	public URI getFileURI(){
		URI folderURI=getEObjectURI().trimSegments(1);
		String fileName = (String)getText();
		if(fileName.indexOf('#')>=0){
			fileName=fileName.replaceAll("#", "%23");
		}
		return folderURI.appendSegment(fileName);
	}
}
