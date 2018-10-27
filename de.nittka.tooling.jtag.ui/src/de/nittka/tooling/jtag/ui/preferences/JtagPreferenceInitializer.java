package de.nittka.tooling.jtag.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;

import com.google.inject.Inject;

public class JtagPreferenceInitializer extends AbstractPreferenceInitializer {

	@Inject
	private IPreferenceStoreAccess preferenceStoreAccess;

	@Override
	public void initializeDefaultPreferences() {
		preferenceStoreAccess.getWritablePreferenceStore().setDefault(JtagRootPreferencePage.FOLD_FILE_DESCRIPTIONS, true);
		preferenceStoreAccess.getWritablePreferenceStore().setDefault(JtagRootPreferencePage.OUTLINE_SHOW_IGNORED_FILES, false);
		preferenceStoreAccess.getWritablePreferenceStore().setDefault(JtagRootPreferencePage.OPEN_HTML_BROWSER_ON_JTAG_SEARCH, true);
		preferenceStoreAccess.getWritablePreferenceStore().setDefault(JtagRootPreferencePage.OPEN_GPS_BROWSER_ON_JTAG_SEARCH, false);
		preferenceStoreAccess.getWritablePreferenceStore().setDefault(JtagRootPreferencePage.PREVENT_REFACTORING, true);
	}

}
