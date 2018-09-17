package de.nittka.tooling.jtag.ui.highlighting;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

import de.nittka.tooling.jtag.jtag.DateSearch;
import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.Folder;
import de.nittka.tooling.jtag.jtag.JtagPackage;
import de.nittka.tooling.jtag.jtag.JtagSearches;

public class JtagSemanticHighlighter implements ISemanticHighlightingCalculator {

	public void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor) {
		if(resource.getContents().size()>0){
			EObject model=resource.getContents().get(0);
			if(model instanceof Folder){
				highlightFolder((Folder) model, acceptor);
			} else if(model instanceof JtagSearches){
				highlightSearch((JtagSearches) model, acceptor);
			}
		}
	}

	private void highlightFolder(Folder folder, IHighlightedPositionAcceptor acceptor){
		List<File> files = folder.getFiles();
		for (File file : files) {
			highlightFileName(file, acceptor);
		}
	}

	private void highlightFileName(File f, IHighlightedPositionAcceptor acceptor){
		highlightFeature(acceptor, f.getFileName(), JtagPackage.Literals.FILE_NAME__FILE_NAME, JtagHighlightingConfiguration.FILENAME_ID);
		highlightFeature(acceptor, f, JtagPackage.Literals.FILE__DATE, JtagHighlightingConfiguration.DATE_ID);
	}

	private void highlightSearch(JtagSearches searches, IHighlightedPositionAcceptor acceptor){
		List<DateSearch> searchList = EcoreUtil2.getAllContentsOfType(searches,  DateSearch.class);
		for (DateSearch search : searchList) {
			highlightFeature(acceptor, search, JtagPackage.Literals.DATE_SEARCH__EXACT, JtagHighlightingConfiguration.DATE_ID);
			highlightFeature(acceptor, search, JtagPackage.Literals.DATE_SEARCH__FROM, JtagHighlightingConfiguration.DATE_ID);
			highlightFeature(acceptor, search, JtagPackage.Literals.DATE_SEARCH__TO, JtagHighlightingConfiguration.DATE_ID);
		}
	}

	private void highlightFeature(IHighlightedPositionAcceptor acceptor, EObject context, EStructuralFeature feature, String id){
		List<INode> nameNodes = NodeModelUtils.findNodesForFeature(context, feature);
		for (INode node : nameNodes) {
			acceptor.addPosition(node.getOffset(), node.getLength(), id);
		}
	}
}