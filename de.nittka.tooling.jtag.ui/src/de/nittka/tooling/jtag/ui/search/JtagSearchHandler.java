package de.nittka.tooling.jtag.ui.search;

import javax.inject.Provider;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.base.Predicates;
import com.google.inject.Inject;

import de.nittka.tooling.jtag.jtag.Search;

public class JtagSearchHandler extends AbstractHandler {

	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;
	@Inject
	private Provider<JtagSearchQuery> queryProvider;

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
			e.printStackTrace();
		}
		return null;
	}

	//the essential parts from ReferenceQueryExecutor
	public void execute(Search search) {
		JtagSearchQuery query=queryProvider.get();
		String searchName=search.getId()!=null?search.getId():"unnamed Xarchive search";
		//dummy initialization of ReferenceQuery
		query.init(null, Predicates.<IReferenceDescription>alwaysTrue(), searchName);
		//the essential initialization of XarchiveSearchQuery (the actual search context)
		query.setSearch(search);
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(query);
	}
}
