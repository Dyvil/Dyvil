package dyvil.tools.compiler.ast.type;

import static dyvil.reflect.Opcodes.*;

import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;

public final class UnknownType extends ASTNode implements IType
{
	@Override
	public void setName(String name, String qualifiedName)
	{
	}
	
	@Override
	public void setName(String name)
	{
	}
	
	@Override
	public String getName()
	{
		return "unknown";
	}
	
	@Override
	public void setQualifiedName(String name)
	{
	}
	
	@Override
	public String getQualifiedName()
	{
		return "unknown";
	}
	
	@Override
	public boolean isName(String name)
	{
		return false;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return null;
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
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
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(Map<String, IType> typeVariables)
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
	public IType resolve(List<Marker> markers, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
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
		return "unknown";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("unknown");
	}
}
