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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
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
import de.nittka.tooling.jtag.ui.JtagPerspective;
import de.nittka.tooling.jtag.ui.internal.JtagActivator;

public class JtagGpsMapHandler extends AbstractHandler{

	@Inject
	private JtagGpsPreview gpsPreview;

	@Override
	public boolean isEnabled() {
		try{
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ISelection selection = page.getSelection();
			if(selection instanceof StructuredSelection){
				return true;
			}else if(selection instanceof TextSelection){
				XtextEditor editor = EditorUtils.getActiveXtextEditor();
				if(editor!=null){
					return editor.getDocument().readOnly(new IUnitOfWork<Boolean, XtextResource>(){

						@Override
						public Boolean exec(XtextResource state) throws Exception {
							return getFolder(state)!=null;
						}

					});
				}
			}
		}catch(Exception e){
			JtagPerspective.logError("error evaluating selection", e);
		}
		return false;
	}

	private Folder getFolder(XtextResource state){
		if(state!=null && !state.getContents().isEmpty()){
			if(state.getContents().get(0) instanceof Folder){
				return (Folder)state.getContents().get(0);
			}
		}
		return null;
	}

//	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ISelection selection = page.getSelection();
		if(selection instanceof TextSelection){
			XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
			editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>(){

				@Override
				public void process(XtextResource state) throws Exception {
					Folder folder=getFolder(state);
					if(folder!=null){
						showHtml(gpsPreview.createHtml(folder));
					}
				}

			});
		} else if(selection instanceof StructuredSelection){
			Iterator<?> iterator = ((StructuredSelection) selection).iterator();
			final Set<IFile> files=new HashSet<>();
			while(iterator.hasNext()){
				Object next = iterator.next();
				if(next instanceof IFile){
					files.add((IFile) next);
				}else if(next instanceof IContainer){
					IContainer container = (IContainer) next;
					if(container.isAccessible()){
						try {
							container.accept(new IResourceVisitor() {
								
								@Override
								public boolean visit(IResource resource) throws CoreException {
									if(resource instanceof IFile){
										files.add((IFile)resource);
									}
									return true;
								}
							});
						} catch (CoreException e) {
							JtagPerspective.logError("error collecting files from selection", e);
						}
					}
				}
			}
			showHtml(gpsPreview.createHtml(files));
		}
		return null;
	}

	private void showHtml(String html){
		if(html!=null){
			File tempFile=JtagActivator.getInstance().getStateLocation().append("JtagShowOnMap.html").toFile();
			try {
				Files.write(html, tempFile, StandardCharsets.ISO_8859_1);
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(tempFile.toURI().toURL());
			} catch (IOException | PartInitException e) {
				JtagPerspective.logError("error showing gps html", e);
			}
		}else{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Nothing to show", "Could not find any files containing location information...");
		}
	}
}
