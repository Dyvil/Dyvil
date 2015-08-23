package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
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
		return Name.auto;
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
		return true;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return Types.ANY;
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
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
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
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/types/UnknownType", "instance", "Ldyvil/reflect/types/UnknownType;", false);
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
	}
	
	@Override
	public IType clone()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "auto";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("auto");
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}
	
	@Override
	public int hashCode()
	{
		return UNKNOWN;
	}
}
