/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.refactoring;

import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;

import de.nittka.tooling.jtag.ui.preferences.JtagRootPreferencePage;

class JtagRefactoringPreventer {

	@Inject
	private IPreferenceStoreAccess preferences;
	@Inject
	private IWorkbench wb;

	private IResource resource;
	static String NAME="prevent file operations";

	void setResourceToMove(Object object) {
		boolean preventRefactoring=preferences.getPreferenceStore().getBoolean(JtagRootPreferencePage.PREVENT_REFACTORING);
		if(preventRefactoring && object instanceof IResource && isJtagNavigator()){
			resource=(IResource)object;
		}
	}

	//if the user previews the refactoring, wb.getActiveWorkbenchWindow() is null (due to the dialog)
	//we simply assume if the Jtag Navigator is active in any workbench, the refactoring is done from there
	private boolean isJtagNavigator(){
		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();
		for (IWorkbenchWindow w : windows) {
			if(w.getActivePage().getActivePart() instanceof IViewPart){
				String id = ((IViewPart)w.getActivePage().getActivePart()).getSite().getId();
				if("de.nittka.tooling.jtag.navigatorView".equals(id)){
					return true;
				}
			}
		}
		return false;
	}

	RefactoringStatus checkConditions(String operation) throws OperationCanceledException {
		if(resource instanceof IContainer){
			return failStatus(operation, "folders", resource);
		} else if(resource instanceof IFile){
			if(!"jtag".equals(resource.getFileExtension())){
				return failStatus(operation, "non-jtag files", resource);
			}
		}
		return null;
	}

	private RefactoringStatus failStatus(String operation, String what, IResource resource){
		return RefactoringStatus.createFatalErrorStatus(String.format("%s %s is not permitted (%s)", operation, what, resource.getName()));
	}
}
