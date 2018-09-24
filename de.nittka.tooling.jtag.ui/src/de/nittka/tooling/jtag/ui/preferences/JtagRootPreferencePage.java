package de.nittka.tooling.jtag.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage;

public class JtagRootPreferencePage extends LanguageRootPreferencePage {

	public static final String OPEN_HTML_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openSearchHtml";
	public static final String OPEN_GPS_BROWSER_ON_JTAG_SEARCH="de.nittka.tooling.jtag.prefs.openGpsHtml";

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(OPEN_HTML_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results (Images)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(OPEN_GPS_BROWSER_ON_JTAG_SEARCH, "open browser for Jtag search results (GPS-Locations)", getFieldEditorParent()));	}
}
