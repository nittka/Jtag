package de.nittka.tooling.jtag.ui.search;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.copyqualifiedname.ClipboardUtil;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.base.Joiner;

public class JtagUsedTagsHandler  extends AbstractHandler{

	@Inject
	private JtagTagCounter tagCounter;

//	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
			if (editor != null) {
				editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {
					@Override
					public void process(XtextResource state) throws Exception {
						toClipBoard(tagCounter.getTagCount(state));
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void toClipBoard(Map<String, AtomicInteger> tagCount){
		List<String>sortedTags=new ArrayList<String>(tagCount.keySet());
		Collections.sort(sortedTags, Collator.getInstance(Locale.getDefault()));
		List<String> tagAndCount=new ArrayList<String>();
		for (String tag : sortedTags) {
			tagAndCount.add(tag+" ("+tagCount.get(tag)+")");
		}
		ClipboardUtil.copy(Joiner.on("\n").join(tagAndCount));
		
	}
}
