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
