package de.nittka.tooling.jtag.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage;

public class JtagRootPreferencePage extends LanguageRootPreferencePage {

	public static final String OPEN_HTML_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openSearchHtml";

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(OPEN_HTML_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results", getFieldEditorParent()));
	}
}
