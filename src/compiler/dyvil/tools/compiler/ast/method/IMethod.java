package dyvil.tools.compiler.ast.method;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IMethod extends IClassMember, ICallableMember, IMethodSignature, IContext
{
	public float getSignatureMatch(Name name, IValue instance, IArguments arguments);
	
	public IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	// Misc
	
	public void setParameters(IParameter[] parameters, int parameterCount);
	
	@Override
	public default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new MethodParameter(Name.getQualified("par" + index), type));
	}
	
	public boolean checkOverride(MarkerList markers, IClass iclass, IMethod candidate, ITypeContext typeContext);
	
	// Generics
	
	public GenericData getGenericData(GenericData data, IValue instance, IArguments arguments);
	
	public boolean hasTypeVariables();
	
	// Compilation
	
	public boolean isIntrinsic();
	
	public int getInvokeOpcode();
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type, int lineNumber) throws BytecodeException;
	
	public void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
}
