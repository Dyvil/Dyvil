package dyvilx.tools.compiler.ast.method;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.TypeList;

public interface IExternalCallableMember extends ICallableMember
{
	IContext getExternalContext();

	ParameterList getExternalParameterList();

	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);

	void setIntrinsicData(IntrinsicData intrinsicData);

	TypeList getExternalExceptions();
}
