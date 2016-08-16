package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;

public interface IContext extends IMemberContext
{
	byte VISIBLE   = 0;
	byte INVISIBLE = 1;
	byte INTERNAL  = 2;

	byte PASS  = -1;
	byte FALSE = 0;
	byte TRUE  = 1;

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
	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	@Override
	void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments);

	byte checkException(IType type);

	IType getReturnType();

	boolean isMember(IVariable variable);

	IDataMember capture(IVariable capture);

	IAccessible getAccessibleThis(IClass type);

	IValue getImplicit();

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
		return resolveConstructors(implicitContext, type, arguments).getBestMember();
	}

	static MatchList<IConstructor> resolveConstructors(IImplicitContext implicitContext, IMemberContext type,
		                                                  IArguments arguments)
	{
		MatchList<IConstructor> matches = new MatchList<>(implicitContext);
		type.getConstructorMatches(matches, arguments);
		return matches;
	}

	static IMethod resolveMethod(IMemberContext context, IValue receiver, Name name, IArguments arguments)
	{
		return resolveMethods(context, receiver, name, arguments).getBestMember();
	}

	static MatchList<IMethod> resolveMethods(IMemberContext context, IValue receiver, Name name, IArguments arguments)
	{
		MatchList<IMethod> matches = new MatchList<>(context);
		context.getMethodMatches(matches, receiver, name, arguments);
		return matches;
	}

	static IMethod resolveImplicit(IImplicitContext context, IValue value, IType targetType)
	{
		return resolveImplicits(context, value, targetType).getBestMember();
	}

	static MatchList<IMethod> resolveImplicits(IImplicitContext context, IValue value, IType targetType)
	{
		MatchList<IMethod> matches = new MatchList<>(null);
		context.getImplicitMatches(matches, value, targetType);
		if (!matches.isEmpty() || targetType == null)
		{
			return matches;
		}

		targetType.getImplicitMatches(matches, value, targetType);
		return matches;
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
