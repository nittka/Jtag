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
import org.eclipse.xtext.ui.editor.findrefs.FindReferencesHandler;

import de.nittka.tooling.jtag.jtag.FileName;

public class JtagFindReferencesHandler extends FindReferencesHandler {

	@Override
	protected void findReferences(EObject target) {
		if(target instanceof FileName){
			//find references for document rather than file name object
			super.findReferences(target.eContainer());
		}else{
			super.findReferences(target);
		}
	}
}
