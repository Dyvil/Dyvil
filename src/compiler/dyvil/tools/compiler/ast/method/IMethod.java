package dyvil.tools.compiler.ast.method;

import dyvil.tools.asm.Handle;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParametricMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IMethod extends ICallableMember, ITypeParametricMember, IContext
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.METHOD;
	}

	void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	void checkImplicitMatch(MatchList<IMethod> list, IValue value, IType type);
	
	IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue receiver, ArgumentList arguments, GenericData genericData);
	
	void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance, ArgumentList arguments, ITypeContext typeContext);
	
	// Misc
	
	boolean isAbstract();

	boolean isImplicitConversion();

	boolean isObjectMethod();

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
