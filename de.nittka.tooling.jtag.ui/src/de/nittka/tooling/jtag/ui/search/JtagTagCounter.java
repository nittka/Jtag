package de.nittka.tooling.jtag.ui.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IContainer.Manager;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;

import com.google.common.base.Splitter;

import de.nittka.tooling.jtag.jtag.JtagPackage;

public class JtagTagCounter {


	@Inject
	private ResourceDescriptionsProvider indexProvider;
	@Inject
	private IResourceServiceProvider serviceProvider;

	public List<String> getTags(Resource resource){
		return new ArrayList<String>(getTagCount(resource).keySet());
	}

	public Map<String, AtomicInteger> getTagCount(Resource resource){
		Map<String, AtomicInteger> tagCount=new HashMap<String, AtomicInteger>();
		IResourceDescriptions index = indexProvider.getResourceDescriptions(resource);
		Manager containerManager = serviceProvider.getContainerManager();

		List<IContainer> visibleContainer = containerManager.getVisibleContainers(serviceProvider.getResourceDescriptionManager().getResourceDescription(resource), index);
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
		return tagCount;
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
