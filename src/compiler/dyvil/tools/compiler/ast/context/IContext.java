package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IImportContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.ast.type.raw.PackageType;
import dyvil.tools.compiler.ast.type.typevar.TypeVarType;
import dyvil.tools.parsing.Name;

public interface IContext extends IMemberContext, IImportContext
{
	byte VISIBLE   = 0;
	byte INVISIBLE = 1;
	byte INTERNAL  = 2;

	byte TRUE  = 0;
	byte FALSE = 1;
	byte PASS  = 2;

	default boolean isStatic()
	{
		return this.checkStatic() != FALSE;
	}

	byte checkStatic();

	default IContext push(IContext context)
	{
		return new CombiningContext(context, this);
	}

	default IContext pop()
	{
		return null;
	}

	default DyvilCompiler getCompilationContext()
	{
		return this.getHeader().getCompilationContext();
	}

	IDyvilHeader getHeader();

	IClass getThisClass();

	IType getThisType();

	@Override
	Package resolvePackage(Name name);

	@Override
	IClass resolveClass(Name name);

	ITypeAlias resolveTypeAlias(Name name, int arity);

	@Override
	ITypeParameter resolveTypeParameter(Name name);

	IOperator resolveOperator(Name name, int type);

	@Override
	IDataMember resolveField(Name name);

	@Override
	void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments);

	@Override
	void getImplicitMatches(MethodMatchList list, IValue value, IType targetType);

	@Override
	void getConstructorMatches(ConstructorMatchList list, IArguments arguments);

	byte checkException(IType type);

	IType getReturnType();

	boolean isMember(IVariable variable);

	IDataMember capture(IVariable capture);

	IAccessible getAccessibleThis(IClass type);

	IValue getImplicit();

	static IClass resolveClass(IMemberContext context, Name name)
	{
		final IClass theClass = context.resolveClass(name);
		if (theClass != null)
		{
			return theClass;
		}

		return Types.LANG_HEADER.resolveClass(name);
	}

	static IType resolveType(IContext context, Name name)
	{
		final IClass theClass = context.resolveClass(name);
		if (theClass != null)
		{
			return new ClassType(theClass);
		}

		final ITypeParameter typeParameter = context.resolveTypeParameter(name);
		if (typeParameter != null)
		{
			return new TypeVarType(typeParameter);
		}

		final Package thePackage = Package.rootPackage.resolvePackage(name);
		if (thePackage != null)
		{
			return new PackageType(thePackage);
		}

		final ITypeAlias type = context.resolveTypeAlias(name, 0);
		if (type != null)
		{
			return type.getType();
		}

		if (context != Types.LANG_HEADER)
		{
			return resolveType(Types.LANG_HEADER, name);
		}
		return null;
	}

	static IOperator resolveOperator(IContext context, Name name, int type)
	{
		final IOperator operator = context.resolveOperator(name, type);
		if (operator == null || operator.getType() != type)
		{
			return Types.LANG_HEADER.resolveOperator(name, type);
		}
		return operator;
	}

	static IConstructor resolveConstructor(IImplicitContext implicitContext, IMemberContext type, IArguments arguments)
	{
		ConstructorMatchList matches = new ConstructorMatchList(implicitContext);
		type.getConstructorMatches(matches, arguments);
		return matches.getBestConstructor();
	}

	static IMethod resolveMethod(IMemberContext context, IValue receiver, Name name, IArguments arguments)
	{
		MethodMatchList matches = new MethodMatchList(context);
		context.getMethodMatches(matches, receiver, name, arguments);
		return matches.getBestMethod();
	}

	static void getConstructorMatch(ConstructorMatchList list, IArguments arguments, IConstructor constructor)
	{
		final float match = constructor.getSignatureMatch(arguments, list);
		if (match > 0)
		{
			list.add(constructor, match);
		}
	}

	static void getMethodMatch(MethodMatchList list, IValue receiver, Name name, IArguments arguments, IMethod method)
	{
		final float match = method.getSignatureMatch(name, receiver, arguments, list);
		if (match > 0)
		{
			list.add(method, match);
		}
	}

	static void getImplicitMatch(MethodMatchList list, IValue value, IType targetType, IMethod method)
	{
		final float match = method.getImplicitMatch(value, targetType);
		if (match > 0)
		{
			list.add(method, match);
		}
	}

	static byte getVisibility(IContext context, IClassMember member)
	{
		IClass thisClass = context.getThisClass();
		if (thisClass != null)
		{
			return thisClass.getVisibility(member);
		}

		return context.getHeader().getVisibility(member);
	}

	static boolean isUnhandled(IContext context, IType exceptionType)
	{
		return Types.isSuperType(Types.EXCEPTION, exceptionType) // Exception type is sub-type of java.lang.Exception
			       && !Types.isSuperType(Types.RUNTIME_EXCEPTION, exceptionType) // but not java.lang.RuntimeException
			       && context.checkException(exceptionType) == FALSE;
	}
}
