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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.nittka.tooling.jtag.ui.JtagPerspective;

public class JtagMaximizeImageViewHandler extends AbstractHandler{

	private IWorkbenchPart lastActivePart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPart activePart = window.getActivePage().getActivePart();
		IWorkbenchPage[] pages = window.getPages();
		for (IWorkbenchPage page : pages) {
			IViewReference[] views = page.getViewReferences();
			for (IViewReference view : views) {
				if("de.nittka.tooling.jtag.imageView".equals(view.getId())){
					boolean restore=view.getPage().isPageZoomed();
					view.getPage().toggleZoom(view);
					if(restore){
						try{
							if(lastActivePart!=null){
								window.getActivePage().activate(lastActivePart);
							}
						}catch(Exception e){
							JtagPerspective.logError("error while activating the previously active part", e);
						}
						lastActivePart=null;
					}else{
						lastActivePart=activePart;
					}
				}
			}
		}
		return null;
	}
}
