package dyvil.tools.compiler.ast.dynamic;

import static dyvil.reflect.Opcodes.*;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class DynamicType extends ASTNode implements IType
{
	@Override
	public void setName(Name name)
	{
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	// IContext
	
	@Override
	public void setFullName(String name)
	{
	}
	
	@Override
	public String getFullName()
	{
		return "dynamic";
	}
	
	@Override
	public void setClass(IClass theClass)
	{
	}
	
	@Override
	public IClass getTheClass()
	{
		return null;
	}
	
	@Override
	public boolean isGenericType()
	{
		return false;
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
	public void setArrayDimensions(int dimensions)
	{
	}
	
	@Override
	public int getArrayDimensions()
	{
		return 0;
	}
	
	@Override
	public boolean isArrayType()
	{
		return false;
	}
	
	@Override
	public IType getSuperType()
	{
		return this;
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
	
	// IContext
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return null;
	}

	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}

	@Override
	public IField resolveField(Name name)
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
	public byte getAccessibility(IMember member)
	{
		return IContext.READ_WRITE_ACCESS;
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
	public IType clone()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "dynamic";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("dynamic");
	}
}
