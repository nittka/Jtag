package de.nittka.tooling.jtag.ui;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.folding.FoldedPosition;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import de.nittka.tooling.jtag.jtag.Folder;
import de.nittka.tooling.jtag.ui.preferences.JtagRootPreferencePage;


public class JtagXtextEditor extends XtextEditor {

	@Override
	public void createPartControl(Composite parent) {
		setHelpContextId("de.nittka.tooling.jtag.ui.jtagHelp");
		super.createPartControl(parent);
		boolean foldPreference=getPreferenceStore().getBoolean(JtagRootPreferencePage.FOLD_FILE_DESCRIPTIONS);
		boolean shouldFold=foldPreference && getDocument().readOnly(new IUnitOfWork<Boolean, XtextResource>() {

			public Boolean exec(XtextResource state) throws Exception {
				try{
					return state.getContents().get(0) instanceof Folder;
				}catch(Exception e){
					
				}
				return false;
			};
		});
		if(shouldFold){
			ProjectionAnnotationModel model = ((ProjectionViewer) getSourceViewer()).getProjectionAnnotationModel();
			foldRegionsOnStartup(model);
		}
	}

	private void foldRegionsOnStartup(ProjectionAnnotationModel model){
		List<Annotation> changes=new ArrayList<Annotation>(); 
		Iterator<?> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()){
			Object next = iterator.next();
			if(next instanceof ProjectionAnnotation){
				ProjectionAnnotation pa = (ProjectionAnnotation) next;
				Position position = model.getPosition(pa);
				if(position instanceof FoldedPosition){
					pa.markCollapsed();
					changes.add(pa);
				}
			}
		}
		model.modifyAnnotations(null,null, changes.toArray(new Annotation[0]));
	}
}
