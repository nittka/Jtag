package de.nittka.tooling.jtag.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import de.nittka.tooling.jtag.ui.JtagPerspective;
import de.nittka.tooling.jtag.ui.internal.JtagActivator;

public class NoJtagFileDecorator extends BaseLabelProvider implements ILightweightLabelDecorator, IResourceChangeListener {

	private static final ImageDescriptor ICON = JtagActivator.imageDescriptorFromPlugin(
			"de.nittka.tooling.jtag.ui","icons/jtag_missing.png");
	Cache<IContainer, Boolean> cache = CacheBuilder.newBuilder().expireAfterWrite(500, TimeUnit.MILLISECONDS)
			.maximumSize(100).weakKeys().build(new CacheLoader<IContainer, Boolean>() {
				@Override
				public Boolean load(IContainer key) throws Exception {
					return hasJtagFile(key);
				}
			});

	public NoJtagFileDecorator() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			try {
				if (resource.exists()) {
					if (resource.getType() == IResource.PROJECT && !resource.getProject().isOpen()) {
						// no overlays necessary
					} else if (resource instanceof IContainer) {
						IProject project = resource.getProject();
						boolean isJtagProject = project != null && cache.getUnchecked(resource.getProject());
						if (isJtagProject && !(resource instanceof IProject)) {
							IContainer container = (IContainer) resource;
							addOverlay(null, decoration);
							if (!cache.getUnchecked(container)) {
								addOverlay(ICON, decoration);
							} else if (!allSubFoldersHaveJtagFile(container)) {
								addOverlay(ICON, decoration);
							}
						}
					}
				}
			} catch (Exception e) {
				JtagPerspective.logError("error updating missing jtag decorator", e);
			}
		}
	}

	private void addOverlay(ImageDescriptor overlay, IDecoration decoration) {
		decoration.addOverlay(overlay, IDecoration.TOP_RIGHT);
	}

	private boolean allSubFoldersHaveJtagFile(IContainer container) throws CoreException, ExecutionException {
		IResource[] members = container.members();
		for (IResource r : members) {
			if (r instanceof IContainer) {
				IContainer c2 = (IContainer) r;
				if (!cache.get(c2)) {
					return false;
				}
				if (!allSubFoldersHaveJtagFile(c2)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasJtagFile(IContainer container) throws CoreException {
		IResource[] members = container.members();
		for (IResource r : members) {
			if (r instanceof IFile) {
				if ("jtag".equals(r.getFileExtension())) {
					return true;
				}
			}
		}
		return false;
	}

	//update parents' decorations on resource change
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {

				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if (delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.ADDED) {
						if (delta.getResource() != null) {
							refresh(delta.getResource().getParent());
						}
						return false;
					}
					return true;
				}
			});
		} catch (CoreException e) {
			JtagPerspective.logError("error preparing missing jtag decorator update", e);
		}
	}

	private void refresh(IContainer container){
		IContainer toRefresh = container;
		List<IContainer> toRefreshList=new ArrayList<>();
		while(toRefresh!=null){
			toRefreshList.add(toRefresh);
			toRefresh=toRefresh.getParent();
		}
		if(!toRefreshList.isEmpty()){
			fireLabelProviderChanged(new LabelProviderChangedEvent(this, toRefreshList.toArray()));
		}
	}
}