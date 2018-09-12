package de.nittka.tooling.jtag.ui.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Composite;

public class JtagSearchFileWizard extends JtagFileWizard{

	public JtagSearchFileWizard() {
		mainPageTitle="creates a jtag searches file";
		initialFileContent= "search ";
	}

	@Override
	protected void addExistingJtagFileHint(Composite parent) {
		//no warning 
	}

	@Override
	protected String getProposedFileName(IContainer folder) {
		return "jtagSearches";
	}
}
