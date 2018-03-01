package org.skyve.impl.tools.test.sail.language.step.interaction.grids;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.skyve.impl.tools.test.sail.XMLUtil;
import org.skyve.impl.tools.test.sail.execution.Executor;
import org.skyve.impl.tools.test.sail.language.Step;
import org.skyve.impl.util.UtilImpl;

/**
 * New record from a data grid
 * @author mike
 */
@XmlType(namespace = XMLUtil.SAIL_NAMESPACE)
@XmlRootElement(namespace = XMLUtil.SAIL_NAMESPACE)
public class DataGridNew implements Step {
	private String binding;

	public String getBinding() {
		return binding;
	}

	@XmlAttribute(name = "binding", required = true)
	public void setBinding(String binding) {
		this.binding = UtilImpl.processStringValue(binding);
	}

	@Override
	public void execute(Executor executor) {
		executor.execute(this);
	}
}
