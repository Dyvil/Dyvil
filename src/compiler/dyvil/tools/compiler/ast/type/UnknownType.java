package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

import static dyvil.reflect.Opcodes.*;

public class UnknownType implements IType
{
	@Override
	public int typeTag()
	{
		return UNKNOWN;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified("var");
	}
	
	// IContext
	
	@Override
	public IClass getTheClass()
	{
		return Types.OBJECT_CLASS;
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
	public IType getSuperType()
	{
		return null;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		return this;
	}
	
	// IContext
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	// Compilation
	
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
	}
	
	@Override
	public int getLoadOpcode()
	{
		return ALOAD;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return AALOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return ASTORE;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return AASTORE;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return ARETURN;
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/type/UnknownType", "instance", "Ldyvil/reflect/type/UnknownType;", false);
	}
	
	@Override
	public IType clone()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "var";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("var");
	}
}
