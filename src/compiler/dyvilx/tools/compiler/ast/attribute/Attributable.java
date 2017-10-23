package dyvilx.tools.compiler.ast.attribute;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IAnnotationConsumer;

import java.lang.annotation.ElementType;

public interface Attributable extends IAnnotationConsumer
{
	ElementType getElementType();

	AttributeList getAttributes();

	void setAttributes(AttributeList attributes);

	default Annotation getAnnotation(IClass type)
	{
		return this.getAttributes().getAnnotation(type);
	}

	default boolean skipAnnotation(String type, Annotation annotation)
	{
		return false;
	}

	@Override
	default void setAnnotation(Annotation annotation)
	{
		if (this.skipAnnotation(annotation.getTypeDescriptor(), annotation))
		{
			return;
		}

		this.getAttributes().add(annotation);
	}

	default boolean hasModifier(int modifier)
	{
		return this.getAttributes().hasFlag(modifier);
	}

	default boolean isAbstract()
	{
		return this.hasModifier(Modifiers.ABSTRACT);
	}

	default boolean isAnnotation()
	{
		return this.hasModifier(Modifiers.ANNOTATION);
	}

	default boolean isDefault()
	{
		return this.hasModifier(Modifiers.DEFAULT);
	}

	default boolean isEnumConstant()
	{
		return this.hasModifier(Modifiers.ENUM_CONST);
	}

	default boolean isImplicit()
	{
		return this.hasModifier(Modifiers.IMPLICIT);
	}

	default boolean isInterface()
	{
		return this.hasModifier(Modifiers.INTERFACE);
	}

	default boolean isObject()
	{
		return this.hasModifier(Modifiers.OBJECT);
	}

	default boolean isOverride()
	{
		return this.hasModifier(Modifiers.OVERRIDE);
	}

	default boolean isStatic()
	{
		return this.hasModifier(Modifiers.STATIC);
	}

	default boolean isVarargs()
	{
		return this.hasModifier(Modifiers.VARARGS);
	}
}
