package de.nittka.tooling.jtag.ui.search;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IContainer.Manager;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.ui.editor.findrefs.ReferenceQuery;
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchResult;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import com.google.common.io.Files;

import de.nittka.tooling.jtag.jtag.JtagPackage;
import de.nittka.tooling.jtag.jtag.JtagSearches;
import de.nittka.tooling.jtag.jtag.Search;
import de.nittka.tooling.jtag.ui.JtagPerspective;
import de.nittka.tooling.jtag.ui.internal.JtagActivator;
import de.nittka.tooling.jtag.ui.preferences.JtagRootPreferencePage;

//we derive from ReferenceQuery in order to reuse much of Xtext's search infrastructure
//in particular result presentation in the Search View, which is bound to the
//ISearchResult-implementation ReferenceSearchResult 
public class JtagSearchQuery extends ReferenceQuery {

	@Inject
	private ResourceDescriptionsProvider indexProvider;
	@Inject
	private IResourceServiceProvider serviceProvider;
	@Inject
	private JtagSearchResultPreview preview;
	@Inject
	private JtagGpsPreview gps;
	@Inject
	private IPreferenceStoreAccess preferenceStoreAccess;


	private JtagSearch search;
	private ISearchResultViewPart part;
	private String baseLabel;
	private int resultCount;

	void setSearch(Search search, ISearchResultViewPart part){
		resultCount=-1;
		this.search=new JtagSearch(search);
		this.part=part;
		if(search.getName()!=null){
			baseLabel = "Jtag search '"+search.getName()+"'";
		}else{
			int index = ((JtagSearches)search.eContainer()).getSearches().indexOf(search)+1;
			baseLabel = "Jtag search '"+search.eResource().getURI().lastSegment()+"' #"+index;
		}
	}

	//we override this method in order to provider our own search results
	//note that ReferenceSearchResult implements IAcceptor<IReferenceDescription>
	//so we just have to create suitable IReferenceDescriptions for our own
	//search results
	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		resultCount=-1;
		ReferenceSearchResult result=(ReferenceSearchResult)getSearchResult();
		result.reset();
		internalRun(monitor, result);
		result.finish();
		updateLabel(result);
		maybeOpenBrowser(result);
		return (monitor.isCanceled()) ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	private void updateLabel(ReferenceSearchResult result){
		if(part!=null){
			try{
				resultCount=result.getMatchingReferences().size();
				Display.getDefault().asyncExec(new Runnable(){

					@Override
					public void run() {
						part.updateLabel();
					}
				});
			}catch(Exception e){
				//ignore
			}
		}
	}

	@Override
	public String getLabel() {
		if(resultCount>=0){
			return baseLabel+" ("+resultCount+" matches)";
		}else{
			return baseLabel;
		}
	}

	private void maybeOpenBrowser(ReferenceSearchResult result){
		try {
			boolean openGpsWanted=preferenceStoreAccess.getPreferenceStore().getBoolean(JtagRootPreferencePage.OPEN_GPS_BROWSER_ON_JTAG_SEARCH);
			if(openGpsWanted && !result.getMatchingReferences().isEmpty()){
				File tempFile= getTempFileLocation().append("JtagSearchGps.html").toFile();
				String gpsHtml=gps.createHtml(result);
				if(gpsHtml!=null){
					Files.write(gpsHtml, tempFile, StandardCharsets.ISO_8859_1);
					openBrowser(tempFile);
				}
			}
		} catch (Exception e) {
			JtagPerspective.logError("error creating gps html", e);
		}
		try {
			boolean openHtmlWanted=preferenceStoreAccess.getPreferenceStore().getBoolean(JtagRootPreferencePage.OPEN_HTML_BROWSER_ON_JTAG_SEARCH);
			if(openHtmlWanted && !result.getMatchingReferences().isEmpty()){
				File tempFile= getTempFileLocation().append("JtagSearchPreview.html").toFile();
				Files.write(preview.createHtml(result), tempFile, StandardCharsets.ISO_8859_1);
				openBrowser(tempFile);
			}
		} catch (Exception e) {
			JtagPerspective.logError("error creating search html", e);
		}
	}

	private void openBrowser(File tempFile) throws PartInitException, MalformedURLException{
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(tempFile.toURI().toURL());
	}

	private IPath getTempFileLocation(){
		return JtagActivator.getInstance().getStateLocation();
	}

	private void internalRun(IProgressMonitor monitor, IAcceptor<IReferenceDescription> acceptor){
		//our implementation searches the index, matching each visible IResourceDescription against the actual search logic
		Resource resource = search.getResource();
		if(resource==null){
			showSearchNotPossibleInfo();
			return;
		}
		IResourceDescriptions index = indexProvider.getResourceDescriptions(search.getResource());
		Manager containerManager = serviceProvider.getContainerManager();
		List<IContainer> visibleContainer = containerManager.getVisibleContainers(serviceProvider.getResourceDescriptionManager().getResourceDescription(search.getResource()), index);
		for (IContainer container : visibleContainer) {
			if(!monitor.isCanceled()){
				Iterator<IResourceDescription> resourceDescs = container.getResourceDescriptions().iterator();
				while(resourceDescs.hasNext() && !monitor.isCanceled()){
					IResourceDescription resourceDesc = resourceDescs.next();
					List<IReferenceDescription> refs = getReferences(resourceDesc);
					Iterator<IEObjectDescription> descs = resourceDesc.getExportedObjectsByType(JtagPackage.Literals.FILE).iterator();
					while(descs.hasNext() && !monitor.isCanceled()){
						IEObjectDescription desc=descs.next();
						if(search.matches(desc, refs, monitor)){
							//this will cause the reference description to be presented in the search view 
							//a dummy ReferenceDescription for the match is created - in our case one pointing to the text region associated to the Document object
							//we know that there is a corresponding EObjectDescription - we put it there;
							//the URI of the resource (desc.getURI()) would work, but would not provide a nice label for the "referencing" object
							acceptor.accept(new DefaultReferenceDescription(desc.getEObjectURI(), desc.getEObjectURI(), null, -1, null));
						}
					}
				}
			}
		}
	}

	private void showSearchNotPossibleInfo(){
		//cannot rerun search if definition file has changed.
		final ISearchQuery query=this;
		Display.getDefault().asyncExec(new Runnable(){

			public void run() {
				NewSearchUI.removeQuery(query);
				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				MessageDialog.openInformation(shell, "Seach not possible", "The search cannot be executed again. The definition file has been modified.");
			}
		});
	}

	private List<IReferenceDescription> getReferences(IResourceDescription desc){
		return IteratorExtensions.toList(desc.getReferenceDescriptions().iterator());
	}
}