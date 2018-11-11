package de.nittka.tooling.jtag.ui.outline;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.ui.editor.outline.quickoutline.QuickOutlinePopup;
import org.eclipse.xtext.ui.editor.outline.quickoutline.ShowQuickOutlineActionHandler;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class JtagShowQuickOutlineActionHandler extends ShowQuickOutlineActionHandler {

	@Inject
	private Injector injector;

	@Override
	protected QuickOutlinePopup createPopup(Shell parent) {
		QuickOutlinePopup  result = new JtagQuickOutlinePopup(parent);
		injector.injectMembers(result);
		return result;
	}
}
