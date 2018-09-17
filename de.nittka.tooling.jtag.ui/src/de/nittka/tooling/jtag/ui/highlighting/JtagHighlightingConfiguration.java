package de.nittka.tooling.jtag.ui.highlighting;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

public class JtagHighlightingConfiguration extends DefaultHighlightingConfiguration {

	public static final String FILENAME_ID = "filename";
	public static final String DATE_ID = "date";

	@Override
	public void configure(IHighlightingConfigurationAcceptor acceptor) {
		super.configure(acceptor);
		acceptor.acceptDefaultHighlighting(FILENAME_ID, "Filename", filenameTextStyle());
		acceptor.acceptDefaultHighlighting(DATE_ID, "Date", dateTextStyle());
	}

	//bold black
	public TextStyle filenameTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		return textStyle;
	}

	//darker grey
	public TextStyle dateTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(90, 90, 90));
		return textStyle;
	}
}
