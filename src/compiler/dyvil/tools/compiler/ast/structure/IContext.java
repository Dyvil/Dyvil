package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

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
	
	public IField resolveField(Name name);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
	public byte getAccessibility(IMember member);
	
	public static IMethod resolveMethod(MarkerList markers, IContext context, IValue instance, Name name, IArguments arguments)
	{
		List<MethodMatch> list = new ArrayList();
		context.getMethodMatches(list, instance, name, arguments);
		
		int size = list.size();
		switch (size)
		{
		case 0:
			return null;
		case 1:
			return list.get(0).method;
		default:
			MethodMatch bestMatch = list.get(0);
			for (int i = 1; i < size; i++)
			{
				MethodMatch m = list.get(i);
				if (m.match > bestMatch.match)
				{
					bestMatch = m;
				}
			}
			return bestMatch.method;
		}
	}
	
	public static IConstructor resolveConstructor(MarkerList markers, IContext context, IArguments arguments)
	{
		List<ConstructorMatch> list = new ArrayList();
		context.getConstructorMatches(list, arguments);
		
		int size = list.size();
		switch (size)
		{
		case 0:
			return null;
		case 1:
			return list.get(0).constructor;
		default:
			ConstructorMatch bestMatch = list.get(0);
			for (int i = 1; i < size; i++)
			{
				ConstructorMatch m = list.get(i);
				if (m.match > bestMatch.match)
				{
					bestMatch = m;
				}
			}
			return bestMatch.constructor;
		}
	}
}
