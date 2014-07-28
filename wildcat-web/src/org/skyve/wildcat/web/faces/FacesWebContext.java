package org.skyve.wildcat.web.faces;

import java.util.UUID;

import javax.faces.context.FacesContext;

import org.skyve.wildcat.web.AbstractWebContext;

public class FacesWebContext extends AbstractWebContext {
	private static final long serialVersionUID = -1539528185277420146L;

	public FacesWebContext() {
		super(UUID.randomUUID().toString(),
				FacesContext.getCurrentInstance().getExternalContext().getRequest(),
				FacesContext.getCurrentInstance().getExternalContext().getResponse());
	}
}