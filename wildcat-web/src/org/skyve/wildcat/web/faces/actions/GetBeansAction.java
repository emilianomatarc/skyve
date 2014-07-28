package org.skyve.wildcat.web.faces.actions;

import java.util.List;
import java.util.Map;

import org.skyve.domain.Bean;
import org.skyve.metadata.module.query.Query;
import org.skyve.persistence.DocumentFilter;
import org.skyve.persistence.DocumentQuery;
import org.skyve.wildcat.persistence.AbstractPersistence;
import org.skyve.wildcat.web.faces.FacesAction;

public class GetBeansAction extends FacesAction<List<Bean>> {
	private String bizModule;
	private String queryName;
	private Map<String, Object> parameters;
	public GetBeansAction(String bizModule, String queryName, Map<String, Object> parameters) {
		this.bizModule = bizModule;
		this.queryName = queryName;
		this.parameters = parameters;
	}
	
	@Override
	public List<Bean> callback() throws Exception {
		Query query = ActionUtil.getQuery(bizModule, queryName);
		DocumentQuery documentQuery = query.constructDocumentQuery(null, null);
		
		if (parameters != null) {
			StringBuilder substring = new StringBuilder(32);
			DocumentFilter documentFilter = documentQuery.getFilter();
			for (String parameterName : parameters.keySet()) {
				substring.setLength(0);
				substring.append('%').append(parameters.get(parameterName)).append('%');
				documentFilter.addLike(parameterName, substring.toString());
			}
		}

		AbstractPersistence persistence = AbstractPersistence.get();
		return persistence.retrieve(documentQuery,
										Integer.valueOf(0),
										Integer.valueOf(250));
	}

}