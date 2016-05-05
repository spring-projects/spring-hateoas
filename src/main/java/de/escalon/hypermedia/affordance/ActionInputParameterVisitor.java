package de.escalon.hypermedia.affordance;

public interface ActionInputParameterVisitor {
	String visit(ActionInputParameter inputParameter, String parentParamName, String paramName, Object propertyValue);
}