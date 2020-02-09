/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage;

public class JtagRootPreferencePage extends LanguageRootPreferencePage {

	public static final String FOLD_FILE_DESCRIPTIONS="de.nittka.tooling.jtag.prefs.foldFilesOnOpenEditor";
	public static final String OUTLINE_SHOW_IGNORED_FILES="de.nittka.tooling.jtag.prefs.showIgnoredFilesInOutline";
	public static final String OPEN_HTML_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openSearchHtml";
	public static final String OPEN_GPS_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openGpsHtml";
	public static final String PREVENT_REFACTORING="de.nittka.tooling.jtag.prefs.preventRefactoring";

	@Override
	protected void createFieldEditors() {
		addLabel("Jtag Editor");
		addField(new BooleanFieldEditor(FOLD_FILE_DESCRIPTIONS, "fold file descriptions on opening Jtag editor", getFieldEditorParent()));
		addField(new BooleanFieldEditor(OUTLINE_SHOW_IGNORED_FILES, "show ignored files in outline", getFieldEditorParent()));
		addLabel("Jtag Search");
		addField(new BooleanFieldEditor(OPEN_HTML_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results (Images)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(OPEN_GPS_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results (GPS-Locations)", getFieldEditorParent()));
		addLabel("Jtag Navigator");
		addField(new BooleanFieldEditor(PREVENT_REFACTORING, "prevent dangerous file refactorings in Jtag Navigator", getFieldEditorParent()));
	}

	private void addLabel(String text){
		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		label.setText(text);
	}

	@Override
	protected void noDefaultAndApplyButton() {}
}
