package dyvilx.tools.compiler.ast.attribute;

import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;

import java.lang.annotation.ElementType;

public interface Attributable
{
	ElementType getElementType();

	AttributeList getAttributes();

	void setAttributes(AttributeList attributes);

	IAnnotation getAnnotation(IClass type);

	default boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		return true;
	}
}
