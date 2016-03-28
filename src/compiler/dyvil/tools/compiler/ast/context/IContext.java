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
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.parsing.Name;

public interface IContext extends IMemberContext
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

	@Override
	IType resolveType(Name name);

	@Override
	ITypeParameter resolveTypeVariable(Name name);

	Operator resolveOperator(Name name);

	@Override
	IDataMember resolveField(Name name);

	@Override
	void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);

	@Override
	void getConstructorMatches(ConstructorMatchList list, IArguments arguments);

	default boolean handleException(IType type)
	{
		return this.checkException(type) != FALSE;
	}

	byte checkException(IType type);

	IType getReturnType();

	boolean isMember(IVariable variable);

	IDataMember capture(IVariable capture);

	IAccessible getAccessibleThis(IClass type);

	IValue getImplicit();

	static void addCompilable(IContext context, IClassCompilable compilable)
	{
		IClass iclass = context.getThisClass();
		if (iclass != null)
		{
			iclass.addCompilable(compilable);
			return;
		}

		IDyvilHeader header = context.getHeader();
		header.addInnerClass(compilable);
	}

	static IClass resolveClass(IMemberContext context, Name name)
	{
		final IClass theClass = context.resolveClass(name);
		if (theClass != null)
		{
			return theClass;
		}

		return Types.LANG_HEADER.resolveClass(name);
	}

	static IType resolveType(IMemberContext context, Name name)
	{
		IType itype = context.resolveType(name);
		if (itype != null)
		{
			return itype;
		}

		return Types.LANG_HEADER.resolveType(name);
	}

	static Operator resolveOperator(IContext context, Name name)
	{
		final Operator operator = context.resolveOperator(name);
		if (operator != null)
		{
			return operator;
		}

		return Types.LANG_HEADER.resolveOperator(name);
	}

	static IConstructor resolveConstructor(IMemberContext context, IArguments arguments)
	{
		ConstructorMatchList matches = new ConstructorMatchList();
		context.getConstructorMatches(matches, arguments);
		return matches.getBestConstructor();
	}

	static IMethod resolveMethod(IMemberContext context, IValue instance, Name name, IArguments arguments)
	{
		MethodMatchList matches = new MethodMatchList();
		context.getMethodMatches(matches, instance, name, arguments);
		return matches.getBestMethod();
	}

	static void getMethodMatch(MethodMatchList list, IValue receiver, Name name, IArguments arguments, IMethod method)
	{
		float match = method.getSignatureMatch(name, receiver, arguments);
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
			       && context.handleException(exceptionType);
	}
}
