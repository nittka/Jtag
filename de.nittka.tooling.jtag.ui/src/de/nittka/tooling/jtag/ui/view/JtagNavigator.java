package de.nittka.tooling.jtag.ui.view;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.navigator.CommonNavigator;

import de.nittka.tooling.jtag.ui.JtagPerspective;

public class JtagNavigator extends CommonNavigator {

	StatusLineContributionItem folderInfo = new StatusLineContributionItem(
			"de.nittka.tooling.jtag.navigatorView.childCount", 25);

	@Override
	protected void initListeners(TreeViewer viewer) {
		super.initListeners(viewer);
		addContainerChildCountStatusInfo(viewer);
		addOpenClosedProjectListener(viewer);
	}

	private void addOpenClosedProjectListener(TreeViewer viewer){
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TreeSelection treeSelection = (TreeSelection) event.getSelection();
				if(treeSelection.size()==1){
					Object selection = treeSelection.getFirstElement();
					if(selection instanceof IProject){
						IProject project=(IProject)selection;
						if(!project.isOpen()){
							try {
								project.open(new NullProgressMonitor());
							} catch (CoreException e) {
								JtagPerspective.logError("error while opening project", e);
							}
						}
					}
				}
			}
		});
	}

	private void addContainerChildCountStatusInfo(TreeViewer viewer) {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				getViewSite().getActionBars().getStatusLineManager().remove(folderInfo);
				TreeSelection treeSelection = (TreeSelection) event.getSelection();
				if (treeSelection.size() == 1) {
					Object selection = treeSelection.getFirstElement();
					if (selection instanceof IContainer) {
						String countString = getChildCountString((IContainer) selection);
						if (countString != null) {
							folderInfo.setVisible(true);
							folderInfo.setText(countString);
							getViewSite().getActionBars().getStatusLineManager().add(folderInfo);
						}
					}
				}
			}
		});
	}

	private String getChildCountString(IContainer c) {
		int fileCount = 0;
		int folderCount = 0;
		try {
			for (IResource r : c.members()) {
				if (r instanceof IContainer) {
					folderCount++;
				} else if ((r instanceof IFile) && !"jtag".equals(r.getFileExtension())) {
					fileCount++;
				}
			}
		} catch (CoreException e) {
			return null;
		}
		return fileCount + " files, " + folderCount + " folders";
	}
}
