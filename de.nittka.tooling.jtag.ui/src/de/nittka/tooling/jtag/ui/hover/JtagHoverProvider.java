/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.hover;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;

import de.nittka.tooling.jtag.jtag.FileName;

public class JtagHoverProvider extends DefaultEObjectHoverProvider {

	@Override
	protected boolean hasHover(EObject o) {
		if(o instanceof FileName){
			return true;
		}
		return super.hasHover(o);
	}

	@Override
	protected String getFirstLine(EObject o) {
		if(o instanceof FileName){
			return "<b>"+getLabel(o)+"</b>";
		}
		return super.getFirstLine(o);
	}
}
