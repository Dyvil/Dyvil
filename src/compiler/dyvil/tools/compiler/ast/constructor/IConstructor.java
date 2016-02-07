package dyvil.tools.compiler.ast.constructor;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IConstructor extends IClassMember, ICallableMember, ITypeList, IContext
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.CONSTRUCTOR;
	}

	float getSignatureMatch(IArguments arguments);
	
	IType checkGenericType(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments);
	
	void checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments);
	
	void checkCall(MarkerList markers, ICodePosition position, IContext context, IArguments arguments);
	
	// Misc
	
	void setParameters(IParameter[] parameters, int parameterCount);
	
	@Override
	default int typeCount()
	{
		return 0;
	}
	
	@Override
	default void setType(int index, IType type)
	{
	}
	
	@Override
	default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new MethodParameter(Name.getQualified("par" + index), type));
	}
	
	@Override
	default IType getType(int index)
	{
		return null;
	}
	
	@Override
	void setType(IType type);
	
	// Compilation
	
	String getDescriptor();
	
	String getSignature();
	
	String[] getExceptions();
	
	void writeCall(MethodWriter writer, IArguments arguments, IType type, int lineNumber) throws BytecodeException;
	
	void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException;
	
	void writeArguments(MethodWriter writer, IArguments arguments) throws BytecodeException;
}
