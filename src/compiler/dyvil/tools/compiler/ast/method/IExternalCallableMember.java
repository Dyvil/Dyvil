package dyvil.tools.compiler.ast.method;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.parameter.ParameterList;

public interface IExternalCallableMember extends ICallableMember
{
	IContext getExternalContext();

	ParameterList getExternalParameterList();
	
	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);

	void setIntrinsicData(IntrinsicData intrinsicData);
}
