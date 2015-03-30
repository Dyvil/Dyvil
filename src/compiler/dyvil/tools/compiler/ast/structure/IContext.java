package dyvil.tools.compiler.ast.structure;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;

public interface IContext
{
	public static final byte	INVISIBLE			= 0;
	public static final byte	READ_ACCESS			= 1;
	public static final byte	WRITE_ACCESS		= 2;
	public static final byte	READ_WRITE_ACCESS	= 3;
	public static final byte	STATIC				= 4;
	public static final byte	SEALED				= 5;
	
	public boolean isStatic();
	
	public IType getThisType();
	
	public Package resolvePackage(Name name);
	
	public IClass resolveClass(Name name);
	
	public ITypeVariable resolveTypeVariable(Name name);
	
	public FieldMatch resolveField(Name name);
	
	public MethodMatch resolveMethod(IValue instance, Name name, IArguments arguments);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	public ConstructorMatch resolveConstructor(IArguments arguments);
	
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
	public byte getAccessibility(IMember member);
}
