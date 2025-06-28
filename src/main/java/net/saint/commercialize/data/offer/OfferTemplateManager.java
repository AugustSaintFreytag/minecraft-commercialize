package net.saint.commercialize.data.offer;

import java.util.ArrayList;
import java.util.List;

public final class OfferTemplateManager {

	// Properties

	private List<OfferTemplate> templates = new ArrayList<>();

	// Access

	public int size() {
		return templates.size();
	}

	public List<OfferTemplate> getTemplates() {
		return templates;
	}

	// Mutation

	public void registerTemplate(OfferTemplate template) {
		templates.add(template);
	}

	public void clearTemplates() {
		templates.clear();
	}

}
