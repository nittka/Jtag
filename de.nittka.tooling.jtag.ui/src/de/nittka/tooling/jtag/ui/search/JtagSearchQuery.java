package de.nittka.tooling.jtag.ui.search;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import de.nittka.tooling.jtag.jtag.JtagPackage;
import de.nittka.tooling.jtag.jtag.Search;

//we derive from ReferenceQuery in order to reuse much of Xtext's search infrastructure
//in particular result presentation in the Search View, which is bound to the
//ISearchResult-implementation ReferenceSearchResult 
public class JtagSearchQuery extends ReferenceQuery {

	@Inject
	private ResourceDescriptionsProvider indexProvider;
	@Inject
	private IResourceServiceProvider serviceProvider;

	private JtagSearch search;

	void setSearch(Search search){
		this.search=new JtagSearch(search);
	}

	//we override this method in order to provider our own search results
	//note that ReferenceSearchResult implements IAcceptor<IReferenceDescription>
	//so we just have to create suitable IReferenceDescriptions for our own
	//search results
	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		ReferenceSearchResult result=(ReferenceSearchResult)getSearchResult();
		result.reset();
		internalRun(monitor, result);
		result.finish();
		return (monitor.isCanceled()) ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	private void internalRun(IProgressMonitor monitor, IAcceptor<IReferenceDescription> acceptor){
		//our implementation searches the index, matching each visible IResourceDescription against the actual search logic
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

	private List<IReferenceDescription> getReferences(IResourceDescription desc){
		return IteratorExtensions.toList(desc.getReferenceDescriptions().iterator());
	}
}