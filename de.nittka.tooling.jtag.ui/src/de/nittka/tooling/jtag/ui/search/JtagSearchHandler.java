/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.search;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Provider;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.common.base.Predicates;
import com.google.inject.Inject;

import de.nittka.tooling.jtag.jtag.JtagSearches;
import de.nittka.tooling.jtag.jtag.Search;
import de.nittka.tooling.jtag.jtag.SearchReference;
import de.nittka.tooling.jtag.ui.JtagPerspective;

public class JtagSearchHandler extends AbstractHandler {

	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;
	@Inject
	private Provider<JtagSearchQuery> queryProvider;
	@Inject
	IResourceValidator validator;

	@Override
	public boolean isEnabled() {
		XtextEditor editor = EditorUtils.getActiveXtextEditor();
		if(editor!=null){
			return editor.getDocument().readOnly(new IUnitOfWork<Boolean, XtextResource>(){

				@Override
				public Boolean exec(XtextResource state) throws Exception {
					return isSearchState(state);
				}

			});
		}else{
			return false;
		}
	}

	private boolean isSearchState(XtextResource state){
		if(state != null && !state.getContents().isEmpty()){
			EObject content = state.getContents().get(0);
			return content instanceof JtagSearches;
		}
		return false;
	}

	//entry point adapted from FindReferencesHandler
//	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
			final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
			editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {

				@Override
				public void process(XtextResource state) throws Exception {
					if(isSearchState(state)){
						//at this point we just make sure that seach is only executed on search objects
						//(also excluding comments and white spaces before the search keyword)
						EObject target = eObjectAtOffsetHelper.resolveContainedElementAt(state, selection.getOffset());
						Search search=EcoreUtil2.getContainerOfType(target, Search.class);
						if(search==null){
							MessageDialog.openError(Display.getCurrent().getActiveShell(), "Cannot execute Jtag search", "There seems to be a syntax error preventing the search.");
						}else{
							ICompositeNode searchNode = NodeModelUtils.findActualNodeFor(search);
							if(search !=null && searchNode!=null && searchNode.getOffset()<selection.getOffset()){
								execute(search);
							}
						}
					}
				}

			});
		} catch (Exception e) {
			JtagPerspective.logError("error executing jtag search", e);
		}
		return null;
	}

	private boolean hasErrors(Search s, AtomicReference<String> namedSearchWithError){
		List<Issue> errors = validator.validate(s.eResource(), CheckMode.NORMAL_AND_FAST, CancelIndicator.NullImpl);
		if(!errors.isEmpty()){
			String searchFragment = s.eResource().getURIFragment(s);
			for (Issue issue : errors) {
				if(issue.getSeverity()==Severity.ERROR){
					if(issue.getUriToProblem()!=null){
						String errorFragment = issue.getUriToProblem().fragment();
						if(errorFragment.equals(searchFragment) || errorFragment.startsWith(searchFragment+"/")){
							return true;
						}
					}
				}
			}
		}
		List<SearchReference> reusedSearches = EcoreUtil2.getAllContentsOfType(s, SearchReference.class);
		for (SearchReference ref : reusedSearches) {
			if(hasErrors(ref.getSearch(), namedSearchWithError)){
				if(namedSearchWithError.get()==null){
					namedSearchWithError.set(ref.getSearch().getName());
				}
				return true;
			}
		}
		return false;
	}

	//the essential parts from ReferenceQueryExecutor
	public void execute(Search search) {
		AtomicReference<String> namedSearchWithError=new AtomicReference<String>(null);
		if(hasErrors(search, namedSearchWithError)){
			String message="The search definition has errors.";
			if(namedSearchWithError.get()!=null){
				message=message+" The error is within the reused search "+namedSearchWithError.get()+".";
			}
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Cannot execute Jtag search", message);
			return;
		}
		JtagSearchQuery query=queryProvider.get();
		ISearchResultViewPart part = NewSearchUI.activateSearchResultView();
		//the essential initialization of JtagSearchQuery (the actual search context)
		query.setSearch(search, part);
		//dummy initialization of ReferenceQuery
		query.init(null, Predicates.<IReferenceDescription>alwaysTrue(), query.getLabel());
		NewSearchUI.runQueryInBackground(query);
	}
}
