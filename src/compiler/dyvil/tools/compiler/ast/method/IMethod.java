package dyvil.tools.compiler.ast.method;

import dyvil.tools.asm.Handle;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IMethod extends IClassMember, ICallableMember, ICallableSignature, ITypeParametric, IContext
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.METHOD;
	}

	float getSignatureMatch(Name name, IValue instance, IArguments arguments);

	float getImplicitMatch(IValue value, IType type);
	
	IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, GenericData genericData);
	
	void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	// Misc
	
	boolean isAbstract();

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
	boolean checkOverride(IMethod candidate, ITypeContext typeContext);

	void addOverride(IMethod method);
	
	// Generics
	
	GenericData getGenericData(GenericData data, IValue instance, IArguments arguments);
	
	boolean hasTypeVariables();
	
	// Compilation
	
	boolean isIntrinsic();
	
	int getInvokeOpcode();

	Handle toHandle();
	
	String getDescriptor();
	
	String getSignature();
	
	String[] getInternalExceptions();
	
	void writeCall(MethodWriter writer, IValue instance, IArguments arguments, ITypeContext typeContext, IType targetType, int lineNumber)
			throws BytecodeException;
	
	void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException;
	
	void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException;
	
	void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException;
}
