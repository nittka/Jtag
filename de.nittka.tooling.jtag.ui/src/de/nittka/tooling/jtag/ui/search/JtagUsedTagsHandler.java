package de.nittka.tooling.jtag.ui.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IContainer.Manager;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.copyqualifiedname.ClipboardUtil;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import de.nittka.tooling.jtag.jtag.JtagPackage;

public class JtagUsedTagsHandler  extends AbstractHandler{

	@Inject
	private ResourceDescriptionsProvider indexProvider;
	@Inject
	private IResourceServiceProvider serviceProvider;

//	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			XtextEditor editor = EditorUtils.getActiveXtextEditor(event);
			if (editor != null) {
				editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {
					@Override
					public void process(XtextResource state) throws Exception {
						Map<String, AtomicInteger> tagCount=new HashMap<String, AtomicInteger>();
						IResourceDescriptions index = indexProvider.getResourceDescriptions(state);
						Manager containerManager = serviceProvider.getContainerManager();

						List<IContainer> visibleContainer = containerManager.getVisibleContainers(serviceProvider.getResourceDescriptionManager().getResourceDescription(state), index);
						for (IContainer container : visibleContainer) {
							for (IEObjectDescription desc : container.getExportedObjectsByType(JtagPackage.Literals.FILE)) {
								String optTags = desc.getUserData("tags");
								if(optTags!=null){
									for (String tag : Splitter.on(";").trimResults().split(optTags)) {
										add(tag, tagCount);
									}
								}
							};
						}
						toClipBoard(tagCount);
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
		Collections.sort(sortedTags);
		List<String> tagAndCount=new ArrayList<String>();
		for (String tag : sortedTags) {
			tagAndCount.add(tag+" ("+tagCount.get(tag)+")");
		}
		ClipboardUtil.copy(Joiner.on("\n").join(tagAndCount));
		
	}

	private void add(String tag, Map<String, AtomicInteger> tagCount){
		if(tag!=null){
			AtomicInteger count=tagCount.get(tag);
			if(count==null){
				count=new AtomicInteger();
				tagCount.put(tag, count);
			}
			count.incrementAndGet();
		}
	}

}
