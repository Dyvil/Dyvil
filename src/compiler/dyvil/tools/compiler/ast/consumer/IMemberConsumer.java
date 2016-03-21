package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.constructor.Constructor;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.constructor.Initializer;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IMemberConsumer extends IClassConsumer
{
	void addField(IDataMember field);

	default IDataMember createField(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Field(position, name, type, modifiers, annotations);
	}

	void addProperty(IProperty property);

	default IProperty createProperty(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Property(position, name, type, modifiers, annotations);
	}

	void addConstructor(IConstructor constructor);

	default IConstructor createConstructor(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Constructor(position, modifiers, annotations);
	}

	void addInitializer(IInitializer initializer);

	default IInitializer createInitializer(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Initializer(position, modifiers, annotations);
	}

	void addMethod(IMethod method);

	default IMethod createMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeMethod(position, name, type, modifiers, annotations);
	}
}
