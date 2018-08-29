package de.nittka.tooling.jtag.ui.hover;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.hover.html.DefaultHoverDocumentationProvider;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.FileName;
import de.nittka.tooling.jtag.ui.JtagFileURIs;

public class JTagEObjectHover extends DefaultHoverDocumentationProvider {

	private static String SPACING_POSTFIX="<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>";

	@Override
	public String getDocumentation(EObject object) {
		if(object instanceof FileName){
			String location=JtagFileURIs.getImageLocation((File)object.eContainer());
			if(location!=null){
				String folder=new java.io.File(location).getParent();
				return "<img src=\"file://"+location+"\" height=120px><br>"+folder+SPACING_POSTFIX;
			}
		}
		return super.getDocumentation(object);
	}
}
