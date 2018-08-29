package de.nittka.tooling.jtag.ui.hover;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;

import de.nittka.tooling.jtag.jtag.FileName;

public class JtagHoverProvider extends DefaultEObjectHoverProvider {

	@Override
	protected boolean hasHover(EObject o) {
		if(o instanceof FileName){
			return true;
		}
		return super.hasHover(o);
	}

	@Override
	protected String getFirstLine(EObject o) {
		if(o instanceof FileName){
			return "<b>"+getLabel(o)+"</b>";
		}
		return super.getFirstLine(o);
	}
}
