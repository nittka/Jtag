package de.nittka.tooling.jtag.ui.linking;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hyperlinking.HyperlinkHelper;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkAcceptor;
import org.eclipse.xtext.ui.editor.hyperlinking.XtextHyperlink;

import com.google.inject.Inject;

import de.nittka.tooling.jtag.jtag.FileName;
import de.nittka.tooling.jtag.ui.JtagFileURIs;

public class JtagHyperlinkHelper extends HyperlinkHelper {

	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;

	public void createHyperlinksByOffset(XtextResource resource, int offset, IHyperlinkAcceptor acceptor) {
		EObject element = eObjectAtOffsetHelper.resolveElementAt(resource, offset);
		if(element instanceof FileName){
			createHyperlinkForReferencedFile((FileName)element, acceptor);
		}else{
			super.createHyperlinksByOffset(resource, offset, acceptor);
		}
	}

	private void createHyperlinkForReferencedFile(FileName file, IHyperlinkAcceptor acceptor){
		URI uri = JtagFileURIs.getReferencedResourceURI(file);
		if(uri!=null){
			ICompositeNode node = NodeModelUtils.findActualNodeFor(file);
			Region region=new Region(node.getOffset(), node.getLength());
			XtextHyperlink hyperlink = getHyperlinkProvider().get();
			hyperlink.setHyperlinkRegion(region);
			hyperlink.setURI(uri);
			hyperlink.setHyperlinkText("open File");
			acceptor.accept(hyperlink);
		}
	}
}
