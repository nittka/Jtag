package de.nittka.tooling.jtag.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.xtext.ui.util.FileOpener;
import org.eclipse.xtext.util.StringInputStream;

import com.google.common.base.Strings;

public class JtagFileWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {

	@Inject
	private FileOpener fileOpener;

	private IContainer folder;
	private WizardPage mainPage;
	private String currentValidFileName;

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		//enablement in plugin.xml ensures single container selection
		this.folder= (IContainer)selection.getFirstElement();
		setWindowTitle("Jtag new file wizard");
		setHelpAvailable(false);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardPage("createJtagFilePage", 
				"creates a jtag file for the selected folder", null) {

			public void createControl(Composite parent) {
				addPageControls(parent);
				setControl(parent);
			}
		};
		addPage(mainPage);
	}

	private void addPageControls(Composite parent){
		Font font = parent.getFont();
		Composite nameGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		nameGroup.setLayout(layout);
		nameGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL));
		nameGroup.setFont(font);

		Label label = new Label(nameGroup, SWT.NONE);
		label.setText("file name");
		label.setFont(font);

		final Text nameField = new Text(nameGroup, SWT.BORDER);
		nameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				handleNameModify(nameField.getText());
			}
		});
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		nameField.setLayoutData(data);
		nameField.setFont(font);

		String proposedJtagName = folder.getName().replace('.', '_').replaceAll("\\s", "_");
		nameField.setText(proposedJtagName+".jtag");
		nameField.setSelection(0, proposedJtagName.length());

		addExistingJtagFileHint(nameGroup);
	}

	private void addExistingJtagFileHint(Composite parent){
		if(folder instanceof IFolder){
			try {
				for (IResource r : folder.members()) {
					if(r instanceof IFile){
						if("jtag".equals(r.getFileExtension())){
							Label l=new Label(parent, SWT.NONE);
							l.setFont(parent.getFont());
							l.setText("The folder already contains a jtag file: "+r.getName());
							GridData d=new GridData();
							d.horizontalSpan=2;
							l.setLayoutData(d);
							return;
						}
					}
				}
			} catch (CoreException e) {
				//ignore
			}
		}
	}

	private void handleNameModify(String name){
		String errorMessage=null;
		if(Strings.isNullOrEmpty(name) || name.length()==5){
			errorMessage="name must not be empty";
		} else if(!name.endsWith(".jtag")){
			errorMessage="file extension must be jtag";
		} else if(name.indexOf(' ')>=0){
			errorMessage="file name must not contain white spaces";
		} else if(folder.findMember(name)!=null){
			errorMessage="file already exists";
		} else{
			currentValidFileName=name;
		}
		mainPage.setErrorMessage(errorMessage);
		mainPage.setPageComplete(errorMessage==null);
		getContainer().updateButtons();
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		try{
			getContainer().run(true, false, new IRunnableWithProgress() {
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					IFile newFile = folder.getFile(new Path(currentValidFileName));
					try {
						newFile.create(new StringInputStream("folder \"short description of folder content\""), true, monitor);
						fileOpener.openFileToEdit(getShell(), newFile);
						monitor.done();
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}catch(Exception e){
			//catch all
			return false;
		}
		return true;
	}
}
