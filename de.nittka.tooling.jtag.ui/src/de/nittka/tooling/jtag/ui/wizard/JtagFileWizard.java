/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.wizard;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
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
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;
import org.eclipse.xtext.ui.util.FileOpener;

import com.google.common.base.Strings;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.Folder;
import de.nittka.tooling.jtag.jtag.JtagFactory;
import de.nittka.tooling.jtag.ui.JtagPerspective;
import de.nittka.tooling.jtag.ui.quickfix.JtagQuickfixProvider;

public class JtagFileWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.INewWizard {

	@Inject
	private FileOpener fileOpener;
	@Inject
	private ISerializer jtagSerializer;
	@Inject
	private XtextResourceSetProvider resourceSetProvider;
	@Inject
	private JtagQuickfixProvider quickfixes;

	private IContainer folder;
	private WizardPage mainPage;
	private String currentValidFileName;

	protected String mainPageTitle="creates a jtag file for the selected folder";

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
				mainPageTitle, null) {

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

		String proposedJtagName=getProposedFileName(folder);
		nameField.setText(proposedJtagName+".jtag");
		nameField.setSelection(0, proposedJtagName.length());

		addExistingJtagFileHint(nameGroup);
	}

	protected String getProposedFileName(IContainer folder){
		String proposedJtagName = folder.getName().replace('.', '_').replaceAll("\\s", "_");
		return proposedJtagName;
	}

	protected void addExistingJtagFileHint(Composite parent){
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
		} else if(fileExists(folder, name)){
			errorMessage="file already exists";
		} else{
			currentValidFileName=name;
		}
		mainPage.setErrorMessage(errorMessage);
		mainPage.setPageComplete(errorMessage==null);
		getContainer().updateButtons();
	}

	private boolean fileExists(IContainer container, String name){
		try {
			IResource[] members = container.members();
			if(members!=null){
				for (IResource member : members) {
					if(member.getName().equalsIgnoreCase(name)){
						return true;
					}
				}
			}
		} catch (CoreException e) {
			JtagPerspective.logError("error checking file already exists",e);
		}
		return false;
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
						newFile.create(new ByteArrayInputStream(getInitialFileContent().getBytes(newFile.getCharset())), true, monitor);
						fileOpener.openFileToEdit(getShell(), newFile);
						monitor.done();
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}catch(Exception e){
			JtagPerspective.logError("error creating jtag file", e);
		}
		return true;
	}

	protected String getInitialFileContent() throws CoreException{
		JtagFactory factory = JtagFactory.eINSTANCE;
		Folder jtagFolder = factory.createFolder();
		jtagFolder.setDescription("short description of folder content");

		IResource[] files = folder.members();
		List<String> fileNames=new ArrayList<>();
		for (IResource iResource : files) {
			if(iResource instanceof IFile){
				fileNames.add(iResource.getName());
			}
		}
		List<File> filesToAdd = quickfixes.getFiles(folder, fileNames);
		jtagFolder.getFiles().addAll(filesToAdd);

		ResourceSet rs = resourceSetProvider.get(folder.getProject());
		Resource resource = rs.createResource(URI.createPlatformResourceURI(folder.getProject().getName()+"/test.jtag",true));
		resource.getContents().add(jtagFolder);

		String result = jtagSerializer.serialize(jtagFolder, SaveOptions.newBuilder().noValidation().format().getOptions());
		return result;
	}
}
