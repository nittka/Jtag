package de.nittka.tooling.jtag.ui.linking;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.findrefs.FindReferencesHandler;

import de.nittka.tooling.jtag.jtag.FileName;

public class JtagFindReferencesHandler extends FindReferencesHandler {

	@Override
	protected void findReferences(EObject target) {
		if(target instanceof FileName){
			//find references for document rather than file name object
			super.findReferences(target.eContainer());
		}else{
			super.findReferences(target);
		}
	}
}
