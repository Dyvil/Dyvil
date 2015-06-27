package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class AnyType implements IType
{
	@Override
	public int typeTag()
	{
		return ANY;
	}

	@Override
	public Name getName()
	{
		return Name.any;
	}

	@Override
	public IClass getTheClass()
	{
		return Types.OBJECT_CLASS;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return true;
	}
	
	@Override
	public boolean isSuperTypeOf2(IType type)
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		Types.OBJECT_CLASS.getMethodMatches(list, instance, name, arguments);
	}

	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}

	@Override
	public byte getVisibility(IClassMember member)
	{
		return Types.OBJECT_CLASS.getVisibility(member);
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return "java/lang/Object";
	}

	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ljava/lang/Object;");
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append("Ljava/lang/Object;");
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/type/AnyType", "instance", "Ldyvil/reflect/type/AnyType;");
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("any");
	}

	@Override
	public IType clone()
	{
		return this;
	}
}
