package dyvilx.tools.compiler.ast.consumer;

import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.constructor.CodeConstructor;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.constructor.Initializer;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.field.Property;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.type.IType;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public interface IMemberConsumer<F extends IDataMember> extends IClassConsumer, IDataMemberConsumer<F>
{
	default boolean acceptEnums()
	{
		return false;
	}

	@Override
	void addDataMember(F field);

	@Override
	F createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations);

	void addProperty(IProperty property);

	default IProperty createProperty(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Property(position, name, type, modifiers, annotations);
	}

	void addConstructor(IConstructor constructor);

	default IConstructor createConstructor(SourcePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeConstructor(position, modifiers, annotations);
	}

	void addInitializer(IInitializer initializer);

	default IInitializer createInitializer(SourcePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Initializer(position, modifiers, annotations);
	}

	void addMethod(IMethod method);

	default IMethod createMethod(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeMethod(position, name, type, modifiers, annotations);
	}
}
