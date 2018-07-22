package dyvilx.tools.compiler.ast.context;

import dyvil.lang.Name;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.field.IAccessible;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.imports.IImportContext;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.ast.type.builtin.Types;

public interface IContext extends IMemberContext, IImportContext
{
	byte VISIBLE   = 0;
	byte INVISIBLE = 1;
	byte INTERNAL  = 2;

	byte PASS  = -1;
	byte FALSE = 0;
	byte TRUE  = 1;

	boolean isThisAvailable();

	boolean isConstructor();

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

	IHeaderUnit getHeader();

	IClass getThisClass();

	IType getThisType();

	@Override
	Package resolvePackage(Name name);

	@Override
	IHeaderUnit resolveHeader(Name name);

	@Override
	IClass resolveClass(Name name);

	@Override
	void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments);

	@Override
	ITypeParameter resolveTypeParameter(Name name);

	@Override
	IOperator resolveOperator(Name name, byte type);

	@Override
	IDataMember resolveField(Name name);

	@Override
	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	@Override
	void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments);

	byte checkException(IType type);

	IType getReturnType();

	boolean isMember(IVariable variable);

	IDataMember capture(IVariable variable);

	IAccessible getAccessibleThis(IClass type);

	@Override
	IValue resolveImplicit(IType type);

	static IOperator resolveOperator(IContext context, Name name, byte type)
	{
		final IOperator operator = context.resolveOperator(name, type);
		if (operator == null || operator.getType() != type)
		{
			final IOperator base = Types.BASE_CONTEXT.resolveOperator(name, type);
			if (base != null && base.getType() == type || operator == null)
			{
				return base;
			}
		}
		return operator;
	}

	static MatchList<ITypeAlias> resolveTypeAlias(IImportContext context, IType receiver, Name name, TypeList arguments)
	{
		MatchList<ITypeAlias> matches = new MatchList<>(null);
		context.resolveTypeAlias(matches, receiver, name, arguments);
		return matches;
	}

	static IConstructor resolveConstructor(IImplicitContext implicitContext, IMemberContext type,
		ArgumentList arguments)
	{
		return resolveConstructors(implicitContext, type, arguments).getBestMember();
	}

	static MatchList<IConstructor> resolveConstructors(IImplicitContext implicitContext, IMemberContext type,
		ArgumentList arguments)
	{
		MatchList<IConstructor> matches = new MatchList<>(implicitContext);
		type.getConstructorMatches(matches, arguments);
		return matches;
	}

	static IMethod resolveMethod(IMemberContext context, IValue receiver, Name name, ArgumentList arguments)
	{
		return resolveMethods(context, receiver, name, arguments).getBestMember();
	}

	static MatchList<IMethod> resolveMethods(IMemberContext context, IValue receiver, Name name, ArgumentList arguments)
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
		final MatchList<IMethod> matches = new MatchList<>(null);

		// First, search the given type for conversion methods
		value.getType().getImplicitMatches(matches, value, targetType);
		if (matches.hasCandidate() && targetType != null)
		{
			return matches;
		}

		// The try the surrounding context
		context.getImplicitMatches(matches, value, targetType);
		if (matches.hasCandidate() || targetType == null)
		{
			return matches;
		}

		// If that doesn't yield anything either, look into the target type (given that it is not null)
		targetType.getImplicitMatches(matches, value, targetType);
		return matches;
	}

	static byte getVisibility(IContext context, ClassMember member)
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
