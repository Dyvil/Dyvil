package dyvil.tools.compiler.ast.method;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IMethod extends IClassMember, ICallableMember, IMethodSignature, IContext
{
	float getSignatureMatch(Name name, IValue instance, IArguments arguments);
	
	IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	// Misc
	
	boolean isAbstract();
	
	void setParameters(IParameter[] parameters, int parameterCount);
	
	@Override
	default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new MethodParameter(Name.getQualified("par" + index), type));
	}
	
	boolean checkOverride(MarkerList markers, IClass iclass, IMethod candidate, ITypeContext typeContext);
	
	// Generics
	
	GenericData getGenericData(GenericData data, IValue instance, IArguments arguments);
	
	boolean hasTypeVariables();
	
	// Compilation
	
	boolean isIntrinsic();
	
	int getInvokeOpcode();
	
	String getDescriptor();
	
	String getSignature();
	
	String[] getExceptions();
	
	void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type, int lineNumber) throws BytecodeException;
	
	void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
}
