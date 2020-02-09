/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.nittka.tooling.jtag.ui.internal.JtagActivator;

public class JtagPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout arg0) {
	}

	public static void logError(String errorMessage, Throwable error){
		JtagActivator.getInstance().getLog().log(new Status(IStatus.ERROR, "de.nittka.tooling.jtag.ui", errorMessage, error));
	}

}
