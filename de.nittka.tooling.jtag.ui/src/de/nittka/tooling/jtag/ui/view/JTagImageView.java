package de.nittka.tooling.jtag.ui.view;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchViewPage;
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchViewTreeNode;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.ui.JtagFileURIs;
import de.nittka.tooling.jtag.ui.JtagXtextEditor;
import de.nittka.tooling.jtag.ui.outline.IgnoreFileOutlineNode;

public class JTagImageView extends ViewPart implements ISelectionListener, IPartListener, ISelectionChangedListener {

	private Composite imageDisplay;
	private int PADDING=20;
	private Image currentImage;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		site.getWorkbenchWindow().getPartService().addPartListener(this);
	}
	
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		imageDisplay=new Composite(parent, SWT.NONE);
		imageDisplay.setBackgroundMode(SWT.INHERIT_FORCE);
		ensureRedrawOnResize();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(imageDisplay, "de.nittka.tooling.jtag.ui.jtagHelp");
	}

	private void ensureRedrawOnResize(){
		//disable image when resizing parent
		imageDisplay.getParent().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				setNewBackground(null);
			}
		});
		//redraw when resizing image composite
		imageDisplay.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				showImage(currentImage);
			}
		});
	}

	@Override
	public void setFocus() {
		if(imageDisplay!=null){
			imageDisplay.setFocus();
		}
	}

	public void partActivated(IWorkbenchPart part) {
		IPostSelectionProvider provider = getSelectionProvider(part);
		if(provider!=null){
			provider.addPostSelectionChangedListener(this);
		}
	}
	public void partDeactivated(IWorkbenchPart part) {
		IPostSelectionProvider provider = getSelectionProvider(part);
		if(provider!=null){
			provider.removePostSelectionChangedListener(this);
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {}
	public void partClosed(IWorkbenchPart part) {}
	public void partOpened(IWorkbenchPart part) {}

	private IPostSelectionProvider getSelectionProvider(IWorkbenchPart part){
		if(part instanceof JtagXtextEditor){
			//image preview for text selections in jtag file
			ISourceViewer viewer = ((JtagXtextEditor) part).getInternalSourceViewer();
			ISelectionProvider provider = viewer.getSelectionProvider();
			if(provider instanceof IPostSelectionProvider){
				return (IPostSelectionProvider)provider;
			}
		} else if(part instanceof ISearchResultViewPart){
			//image preview for tree node selection in search results
			IPage page = ((ISearchResultViewPart)part).getActivePage();
			if(page instanceof ReferenceSearchViewPage){
				final TreeViewer treeViewer=((ReferenceSearchViewPage)page).getViewer();
				return treeViewer;
			}
		}
		return null;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		//image preview for navigator and outline
		if(selection instanceof TreeSelection){
			if(((TreeSelection) selection).size()==1){
				Object element = ((TreeSelection) selection).getFirstElement();
				if(element instanceof IFile){
					showFile(((IFile) element).getLocation().toString(), true);
					return;
				} else if(element instanceof EObjectNode){
					if(isPreviewFromOutline((EObjectNode)element)){
						return;
					}
				} else if(element instanceof IgnoreFileOutlineNode){
					URI fileURI=((IgnoreFileOutlineNode)element).getFileURI();
					showFile(JtagFileURIs.getImageLocation(fileURI), true);
					return;
				}
			}
			hideFile();
		}
	}

	private boolean isPreviewFromOutline(final EObjectNode outlineNode){
		final AtomicBoolean previewShown=new AtomicBoolean(false);
		if("jtag".equals(outlineNode.getEObjectURI().fileExtension())){
			outlineNode.getDocument().readOnly(new IUnitOfWork<Void, XtextResource>() {

				public java.lang.Void exec(XtextResource state) throws Exception {
					EObject objectNode = state.getEObject(outlineNode.getEObjectURI().fragment());
					if(objectNode instanceof File){
						String imageLocation = JtagFileURIs.getImageLocation((File)objectNode);
						if(imageLocation!=null){
							showFile(imageLocation, false);
							previewShown.set(true);
						}
					}
					return null;
				}
			});
		}
		return previewShown.get();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if(event.getSource() instanceof XtextSourceViewer && event.getSelection() instanceof TextSelection){
			XtextSourceViewer viewer=(XtextSourceViewer)event.getSource();
			final TextSelection selection=(TextSelection)event.getSelection();
			((XtextDocument)viewer.getDocument()).readOnly(new IUnitOfWork<Void, XtextResource>() {
				public java.lang.Void exec(XtextResource state) throws Exception {
					ILeafNode node = NodeModelUtils.findLeafNodeAtOffset(state.getParseResult().getRootNode(), selection.getOffset());
					EObject model = NodeModelUtils.findActualSemanticObjectFor(node);
					File file = EcoreUtil2.getContainerOfType(model,File.class);
					if(file!=null){
						ICompositeNode fileNode = NodeModelUtils.findActualNodeFor(file);
						int fileEndOffset=fileNode.getOffset()+fileNode.getLength();
						int selectionEndOffset=selection.getOffset()+selection.getLength();
						if(fileNode.getOffset()<=selection.getOffset() && selectionEndOffset<=fileEndOffset){
							String imageLocation = JtagFileURIs.getImageLocation(file);
							showFile(imageLocation, true);
							return null;
						}
					}
					hideFile();
					return null;
				}
			});
		} else if(event.getSource() instanceof TreeViewer && event.getSelection() instanceof TreeSelection){
			//should be active for Search view only
			TreeSelection s=(TreeSelection)event.getSelection();
			String imageLocation=null;
			if(s.size()==1){
				Object node = s.getFirstElement();
				if(node instanceof ReferenceSearchViewTreeNode){
					Object description = ((ReferenceSearchViewTreeNode) node).getDescription();
					if(description instanceof IReferenceDescription){
						URI targetURI = ((IReferenceDescription) description).getSourceEObjectUri();
						Resource resource = new ResourceSetImpl().getResource(targetURI, true);
						EObject target = resource.getEObject(targetURI.fragment());

						if(target!=null && !(target instanceof File)){
							target=EcoreUtil2.getContainerOfType(target,File.class);
						}
						if(target instanceof File){
							imageLocation = JtagFileURIs.getImageLocation((File)target);
						}
						resource.unload();
					}
				}
			}
			if(imageLocation!=null){
				showFile(imageLocation, false);
			}else{
				hideFile();
			}
		}
	}

	private void hideFile(){
		showFile(null, true);
	}

	private void showFile(String fileLocation, boolean force){
		if(imageDisplay == null || imageDisplay.isDisposed()){
			return;
		}
		try{
			Image image=new Image(imageDisplay.getDisplay(), fileLocation);
			currentImage=image;
			showImage(image);
		}catch (Exception e){
			if(force){
				currentImage=null;
				setNewBackground(null);
			}
		}
	}

	private void showImage(Image image){
		if(image!=null && imageDisplay!=null && !imageDisplay.isDisposed()){
			ImageData imageData = image.getImageData();
			Point availableSize=imageDisplay.getParent().getSize();
			Point imageSize=getImageSize(availableSize, imageData);
			Image scaled=new Image(imageDisplay.getDisplay(),imageData.scaledTo(imageSize.x, imageSize.y));
			imageDisplay.setSize(scaled.getImageData().width, scaled.getImageData().height);
			setNewBackground(scaled);
			disposeImage(image);
			imageDisplay.setLocation((availableSize.x-imageSize.x)/2, (availableSize.y-imageSize.y)/2);
		}
	}


	private void setNewBackground(Image image){
		if(imageDisplay!=null && !imageDisplay.isDisposed()){
			Image oldBackground = imageDisplay.getBackgroundImage();
			disposeImage(oldBackground);
			imageDisplay.setBackgroundImage(image);
		}
	}

	private void disposeImage(Image image){
		if(image!=null && !image.isDisposed()){
			image.dispose();
		}
	}

	private Point getImageSize(Point availableSpace, ImageData imageData){
		float maxWidth=availableSpace.x-PADDING;
		float maxHeight=availableSpace.y-PADDING;
		float ratio=maxWidth/maxHeight;

		float imageWidth=imageData.width;
		float imageHeight=imageData.height;
		float imageRatio=imageWidth/imageHeight;

		if(imageRatio>ratio){
			return new Point((int)(maxWidth), (int)(maxWidth/imageRatio));
		}else{
			return new Point((int)(maxHeight*imageRatio), (int)maxHeight);
		}
	}
}