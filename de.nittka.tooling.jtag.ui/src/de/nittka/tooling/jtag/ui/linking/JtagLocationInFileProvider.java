/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.linking;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.DefaultLocationInFileProvider;
import org.eclipse.xtext.util.ITextRegion;

import de.nittka.tooling.jtag.jtag.File;

public class JtagLocationInFileProvider extends
		DefaultLocationInFileProvider {

	@Override
	public ITextRegion getSignificantTextRegion(EObject obj) {
		if(obj instanceof File){
			File doc = ((File) obj);
			if(doc.getFileName() != null){
				return getFullTextRegion(doc.getFileName());
			}
		}
		return super.getSignificantTextRegion(obj);
	}
}
