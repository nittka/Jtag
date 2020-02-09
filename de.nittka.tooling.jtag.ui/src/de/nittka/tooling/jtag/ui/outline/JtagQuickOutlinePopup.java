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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.editor.outline.quickoutline.QuickOutlinePopup;

import de.nittka.tooling.jtag.ui.JtagPerspective;
import de.nittka.tooling.jtag.ui.view.JTagImageView;

public class JtagQuickOutlinePopup extends QuickOutlinePopup {

	public JtagQuickOutlinePopup(Shell parent) {
		super(parent);
	}

	@Override
	protected TreeViewer createTreeViewer(Composite parent, int style) {
		TreeViewer treeViewer=super.createTreeViewer(parent, style);
		addJtagImageViewSelectionListener(treeViewer);
		return treeViewer;
	}

	private void addJtagImageViewSelectionListener(TreeViewer treeViewer){
		try{
			IViewReference[] viewRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
			for (IViewReference viewRef : viewRefs) {
				if("de.nittka.tooling.jtag.imageView".equals(viewRef.getId())){
					final JTagImageView view = (JTagImageView)viewRef.getView(false);
					treeViewer.getTree().addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							if(view!=null){
								view.selectionChanged(null, treeViewer.getSelection());
							}
						}
					});
					return;
				}
			}
		} catch(Exception e){
			JtagPerspective.logError("error adding Jtag Image view selection listener for quick outline", e);
		}
	}
}
