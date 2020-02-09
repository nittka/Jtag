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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.Folder;

public class JtagEditorNavigateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
		boolean isDown = isNavigateDown(event);
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			int offset = isDown ? textSelection.getOffset() + textSelection.getLength() : textSelection.getOffset();

			int offsetToNavigateTo = getOffsetToNavigateTo(editor, offset, isDown);
			if (offsetToNavigateTo >= 0) {
				editor.getSelectionProvider().setSelection(new TextSelection(offsetToNavigateTo, 0));
			}
		}
		return null;
	}

	private int getOffsetToNavigateTo(XtextEditor editor, final int offset, final boolean navigateDown) {
		return editor.getDocument().readOnly(new IUnitOfWork<Integer, XtextResource>() {

			@Override
			public Integer exec(XtextResource state) throws Exception {
				if (state != null && !state.getContents().isEmpty()) {
					EObject model = state.getContents().get(0);
					if (model instanceof Folder) {
						return getTargetFileOffset((Folder) model, offset, navigateDown);
					}
				}
				return -1;
			}
		});
	}

	private int getTargetFileOffset(Folder f, int currentOffset, boolean navigateDown) {
		if (!f.getFiles().isEmpty()) {
			if (navigateDown) {
				File firstFile = f.getFiles().get(0);
				INode node = NodeModelUtils.getNode(firstFile);
				while (node != null) {
					int fileNodeOffset = node.getOffset();
					if (fileNodeOffset > currentOffset) {
						return getOffsetToReveal(node);
					}
					node = node.getNextSibling();
				}
			} else {
				try{
					//set offset to start of current file
					EObject offsetElement = NodeModelUtils.findLeafNodeAtOffset(NodeModelUtils.getNode(f), currentOffset).getSemanticElement();
					File fileAtOffset=EcoreUtil2.getContainerOfType(offsetElement, File.class);
					if(fileAtOffset!=null){
						currentOffset=NodeModelUtils.getNode(fileAtOffset).getOffset();
					}
				}catch(Exception e){
					//ignore
				}
				File lastFile = f.getFiles().get(f.getFiles().size() - 1);
				INode node = NodeModelUtils.getNode(lastFile);
				while (node != null) {
					int fileNodeOffset = node.getOffset();
					if (fileNodeOffset < currentOffset && node.getSemanticElement() instanceof File) {
						return getOffsetToReveal(node);
					}
					node = node.getPreviousSibling();
				}
			}
		}
		return -1;
	}

	private int getOffsetToReveal(INode fileToReveal) {
		return fileToReveal.getOffset();
	}

	private boolean isNavigateDown(ExecutionEvent event) {
		String direction = event.getParameter("de.nittka.tooling.jtag.ui.navigateJtagEntry.direction");
		return "down".equals(direction);
	}
}
