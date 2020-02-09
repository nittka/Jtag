/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.view;

import java.text.Collator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.google.common.base.Strings;

/**
 * sort jtag-files first, then by name
 */
public class JtagFileSorter extends ViewerSorter {

	public JtagFileSorter() {
	}

	public JtagFileSorter(Collator collator) {
		super(collator);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if ((e1 instanceof IResource) && (e2 instanceof IResource)) {
			IResource resource1 = (IResource)e1;
			IResource resource2 = (IResource)e2;
			String extension1 = Strings.nullToEmpty(resource1.getFileExtension());
			String extension2 = Strings.nullToEmpty(resource2.getFileExtension());
			//folders first by name , then jtag, then files by name
			if(resource1 instanceof IContainer){
				if(resource2 instanceof IContainer){
					return sortByName(resource1, resource2);
				} else{
					return -1;
				}
			} else if(resource2 instanceof IContainer){
				return 1;
			} else if(isJtag(extension1)){
				if(isJtag(extension2)){
					return sortByName(resource1, resource2);
				} else {
					return -1;
				}
			} else if(isJtag(extension2)){
				return 1;
			} else{
				return sortByName(resource1, resource2);
			}
		} else {
			return 0;
		}
	}

	private boolean isJtag(String fileExtension){
		return "jtag".equals(fileExtension);
	}

	private int sortByName(IResource r1, IResource r2){
		String name1 = Strings.nullToEmpty(r1.getName());
		String name2 = Strings.nullToEmpty(r2.getName());
		return name1.compareToIgnoreCase(name2);
	}
}
