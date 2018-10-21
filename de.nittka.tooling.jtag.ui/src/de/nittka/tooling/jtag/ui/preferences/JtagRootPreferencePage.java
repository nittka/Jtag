package de.nittka.tooling.jtag.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage;

public class JtagRootPreferencePage extends LanguageRootPreferencePage {

	public static final String FOLD_FILE_DESCRIPTIONS="de.nittka.tooling.jtag.prefs.foldFilesOnOpenEditor";
	public static final String OPEN_HTML_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openSearchHtml";
	public static final String OPEN_GPS_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openGpsHtml";
	public static final String PREVENT_REFACTORING="de.nittka.tooling.jtag.prefs.preventRefactoring";

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(FOLD_FILE_DESCRIPTIONS, "fold file descriptions on opening Jtag editor", getFieldEditorParent()));
		addField(new BooleanFieldEditor(OPEN_HTML_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results (Images)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(OPEN_GPS_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results (GPS-Locations)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PREVENT_REFACTORING, "prevent dangerous file refactorings in Jtag Navigator", getFieldEditorParent()));
	}

	@Override
	protected void noDefaultAndApplyButton() {}
}
