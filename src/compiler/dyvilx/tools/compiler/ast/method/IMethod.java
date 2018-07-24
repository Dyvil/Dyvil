package dyvilx.tools.compiler.ast.method;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParametricMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.parsing.marker.MarkerList;

import static dyvil.reflect.Modifiers.ABSTRACT;
import static dyvil.reflect.Modifiers.FINAL;

public interface IMethod extends ICallableMember, ITypeParametricMember, IContext
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.METHOD;
	}

	// ------------------------------ Attributable Implementation ------------------------------

	@Override
	default int getJavaFlags()
	{
		int javaFlags = ICallableMember.super.getJavaFlags();
		if (this.hasModifier(Modifiers.STATIC | Modifiers.ABSTRACT))
		{
			// for static abstract methods, move the abstract modifier

			javaFlags &= ~ABSTRACT;
		}
		if ((javaFlags & Modifiers.FINAL) != 0 && this.getEnclosingClass().hasModifier(Modifiers.INTERFACE))
		{
			// for final interface methods, move the final modifier

			javaFlags &= ~FINAL;
		}
		return javaFlags;
	}

	@Override
	default long getDyvilFlags()
	{
		long dyvilFlags = ICallableMember.super.getDyvilFlags();
		if (this.hasModifier(Modifiers.STATIC | Modifiers.ABSTRACT))
		{
			// for static abstract methods, move the abstract modifier

			dyvilFlags |= ABSTRACT;
		}
		if (this.hasModifier(Modifiers.FINAL) && this.getEnclosingClass().hasModifier(Modifiers.INTERFACE))
		{
			// for final interface methods, move the final modifier

			dyvilFlags |= FINAL;
		}
		return dyvilFlags;
	}

	// --------------- Matching ---------------

	void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	void checkImplicitMatch(MatchList<IMethod> list, IValue value, IType type);

	IValue checkArguments(MarkerList markers, SourcePosition position, IContext context, IValue receiver, ArgumentList arguments, GenericData genericData);

	void checkCall(MarkerList markers, SourcePosition position, IContext context, IValue instance, ArgumentList arguments, ITypeContext typeContext);

	// Misc

	boolean isImplicitConversion();

	boolean isFunctional();

	boolean isObjectMethod();

	default boolean isNested()
	{
		return false;
	}

	/**
	 * Checks if this method overrides the given {@code candidate} method.
	 *
	 * @param candidate
	 * 		the potential super-method
	 * @param typeContext
	 * 		the type context for type specialization
	 *
	 * @return {@code true}, if this method overrides the given candidate
	 */
	boolean overrides(IMethod candidate, ITypeContext typeContext);

	void addOverride(IMethod method);

	// Generics

	IType getReceiverType();

	GenericData getGenericData(GenericData data, IValue instance, ArgumentList arguments);

	boolean hasTypeVariables();

	// Compilation

	boolean isIntrinsic();

	IntrinsicData getIntrinsicData();

	int getInvokeOpcode();

	Handle toHandle();

	void writeCall(MethodWriter writer, IValue receiver, ArgumentList arguments, ITypeContext typeContext, IType targetType, int lineNumber)
			throws BytecodeException;

	void writeInvoke(MethodWriter writer, IValue receiver, ArgumentList arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException;

	void writeJump(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException;

	void writeInvJump(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException;
}
