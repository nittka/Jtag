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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.outline.quickoutline.QuickOutlinePopup;
import org.eclipse.xtext.ui.editor.outline.quickoutline.ShowQuickOutlineActionHandler;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class JtagShowQuickOutlineActionHandler extends ShowQuickOutlineActionHandler {

	@Inject
	private Injector injector;

	@Override
	protected QuickOutlinePopup createPopup(Shell parent) {
		QuickOutlinePopup  result = new JtagQuickOutlinePopup(parent);
		injector.injectMembers(result);
		return result;
	}
}
