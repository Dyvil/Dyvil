package dyvilx.tools.compiler.ast.consumer;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.constructor.CodeConstructor;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.constructor.Initializer;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.field.Property;
import dyvilx.tools.compiler.ast.type.IType;

public interface IMemberConsumer<F extends IDataMember> extends IClassConsumer, IMethodConsumer, IDataMemberConsumer<F>
{
	default boolean acceptEnums()
	{
		return false;
	}

	@Override
	void addDataMember(F field);

	@Override
	F createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes);

	void addProperty(IProperty property);

	default IProperty createProperty(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Property(position, name, type, attributes);
	}

	void addConstructor(IConstructor constructor);

	default IConstructor createConstructor(SourcePosition position, AttributeList attributes)
	{
		return new CodeConstructor(position, attributes);
	}

	void addInitializer(IInitializer initializer);

	default IInitializer createInitializer(SourcePosition position, AttributeList attributes)
	{
		return new Initializer(position, attributes);
	}
}
