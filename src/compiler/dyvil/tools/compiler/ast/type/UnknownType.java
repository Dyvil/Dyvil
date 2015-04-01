package dyvil.tools.compiler.ast.type;

import static dyvil.reflect.Opcodes.*;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
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
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class UnknownType extends ASTNode implements IType
{
	@Override
	public void setName(Name name)
	{
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified("unknown");
	}
	
	// IContext
	
	@Override
	public void setFullName(String name)
	{
	}
	
	@Override
	public String getFullName()
	{
		return "unknown";
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
		return null;
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
	
	
	
	// IContext
	
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
	public FieldMatch resolveField(Name name)
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
		return 0;
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
	public IType clone()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "unknown";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("Object");
	}
}
