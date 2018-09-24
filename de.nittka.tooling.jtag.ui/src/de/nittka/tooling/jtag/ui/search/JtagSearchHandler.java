package de.nittka.tooling.jtag.ui.search;

import java.util.List;

import javax.inject.Provider;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.search.ui.NewSearchUI;
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
import de.nittka.tooling.jtag.ui.JtagPerspective;

public class JtagSearchHandler extends AbstractHandler {

	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;
	@Inject
	private Provider<JtagSearchQuery> queryProvider;
	@Inject
	IResourceValidator validator;

	//entry point adapted from FindReferencesHandler
//	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
			if (editor != null) {
				final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
				editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {
					@Override
					public void process(XtextResource state) throws Exception {
						EObject target = eObjectAtOffsetHelper.resolveContainedElementAt(state, selection.getOffset());
						//at this point we just make sure that seach is only executed on search objects
						//(also excluding comments and white spaces before the search keyword)
						Search search=EcoreUtil2.getContainerOfType(target, Search.class);
						ICompositeNode searchNode = NodeModelUtils.findActualNodeFor(search);
						if(search !=null && searchNode!=null && searchNode.getOffset()<selection.getOffset()){
							execute(search);
						}
					}
				});
			}
		} catch (Exception e) {
			JtagPerspective.logError("error executing jtag search", e);
		}
		return null;
	}

	private boolean hasErrors(Search s){
		List<Issue> errors = validator.validate(s.eResource(), CheckMode.NORMAL_AND_FAST, CancelIndicator.NullImpl);
		if(!errors.isEmpty()){
			String searchFragment = s.eResource().getURIFragment(s);
			for (Issue issue : errors) {
				if(issue.getSeverity()==Severity.ERROR){
					String errorFragment = issue.getUriToProblem().fragment();
					if(errorFragment.startsWith(searchFragment)){
						return true;
					}
				}
			}
		}
		return false;
	}

	//the essential parts from ReferenceQueryExecutor
	public void execute(Search search) {
		if(hasErrors(search)){
			return;
		}
		JtagSearchQuery query=queryProvider.get();
		String searchName=getSearchName(search);
		//dummy initialization of ReferenceQuery
		query.init(null, Predicates.<IReferenceDescription>alwaysTrue(), searchName);
		//the essential initialization of XarchiveSearchQuery (the actual search context)
		query.setSearch(search);
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(query);
	}

	private String getSearchName(Search search){
		if(search.getName()!=null){
			return "Jtag search '"+search.getName()+"'";
		}else{
			int index = ((JtagSearches)search.eContainer()).getSearches().indexOf(search)+1;
			return "Jtag search '"+search.eResource().getURI().lastSegment()+"' #"+index;
		}
	}
}
