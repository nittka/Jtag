/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;

import com.google.common.base.Joiner;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.Folder;
import de.nittka.tooling.jtag.jtag.JtagConfig;

public class JtagResourceDescriptionStrategy extends
		DefaultResourceDescriptionStrategy {

	@Override
	public boolean createEObjectDescriptions(EObject eObject,
			IAcceptor<IEObjectDescription> acceptor) {
		if(eObject instanceof File){
			File doc=(File)eObject;
//			QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(eObject);
			QualifiedName qualifiedName= QualifiedName.create(doc.getFileName().getFileName());
			if(qualifiedName!=null){
				Map<String,String> map=new HashMap<String, String>();
				if(doc.getDate()!=null){
					map.put("date", doc.getDate());
				}
				if(doc.getTitle()!=null){
					map.put("title", doc.getTitle());
				}
				if(doc.getDescription()!=null){
					map.put("desc", doc.getDescription());
				}
				List<String> tags=getTags(doc);
				if(!tags.isEmpty()){
					map.put("tags", Joiner.on(";").join(tags));
				}
				acceptor.accept(EObjectDescription.create(qualifiedName, eObject, map));
			}
			return false;
		} else if(eObject instanceof JtagConfig){
			acceptor.accept(EObjectDescription.create(QualifiedName.create(eObject.eResource().getURI().lastSegment()), eObject));
			return true;
		} else {
			return super.createEObjectDescriptions(eObject, acceptor);
		}
	}

	private List<String> getTags(File f){
		List<String> result=new ArrayList<String>();
		result.addAll(f.getTags());
		Folder folder=(Folder)f.eContainer();
		result.addAll(folder.getTags());
		return result;
	}
}
