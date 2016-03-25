package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.constructor.CodeConstructor;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.constructor.Initializer;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IMemberConsumer<F extends IDataMember> extends IClassConsumer, IDataMemberConsumer<F>
{
	@Override
	void addDataMember(F field);

	@Override
	F createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations);

	void addProperty(IProperty property);

	default IProperty createProperty(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Property(position, name, type, modifiers, annotations);
	}

	void addConstructor(IConstructor constructor);

	default IConstructor createConstructor(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeConstructor(position, modifiers, annotations);
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
