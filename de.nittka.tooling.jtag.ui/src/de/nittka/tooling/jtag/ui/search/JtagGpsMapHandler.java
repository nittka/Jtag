package de.nittka.tooling.jtag.ui.search;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.io.Files;

import de.nittka.tooling.jtag.jtag.Folder;
import de.nittka.tooling.jtag.ui.internal.JtagActivator;

public class JtagGpsMapHandler  extends AbstractHandler{

	@Inject
	private JtagGpsPreview gpsPreview;

//	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ISelection selection = page.getSelection();
		if(selection instanceof TextSelection){
			try {
				XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
				if (editor != null) {
					editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {
						@Override
						public void process(XtextResource state) throws Exception {
							if(state!=null && !state.getContents().isEmpty()){
								if(state.getContents().get(0) instanceof Folder){
									showHtml(gpsPreview.createHtml((Folder)state.getContents().get(0)));
								}
							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(selection instanceof StructuredSelection){
			Iterator<?> iterator = ((StructuredSelection) selection).iterator();
			final Set<IFile> files=new HashSet<>();
			while(iterator.hasNext()){
				Object next = iterator.next();
				if(next instanceof IFile){
					files.add((IFile) next);
				}else if(next instanceof IContainer){
					try {
						((IContainer) next).accept(new IResourceVisitor() {
							
							@Override
							public boolean visit(IResource resource) throws CoreException {
								if(resource instanceof IFile){
									files.add((IFile)resource);
								}
								return true;
							}
						});
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
			showHtml(gpsPreview.createHtml(files));
		}
		return null;
	}

	private void showHtml(String html){
		if(html!=null){
			File tempFile=  JtagActivator.getInstance().getStateLocation().append("JtagShowOnMap.html").toFile();
			try {
				Files.write(html, tempFile, StandardCharsets.ISO_8859_1);
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(tempFile.toURI().toURL());
			} catch (IOException | PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
