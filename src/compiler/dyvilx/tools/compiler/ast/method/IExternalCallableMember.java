package dyvilx.tools.compiler.ast.method;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.parameter.ParameterList;

public interface IExternalCallableMember extends ICallableMember
{
	IContext getExternalContext();

	ParameterList getExternalParameterList();
	
	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);

	void setIntrinsicData(IntrinsicData intrinsicData);
}
