package dyvil.tools.asm;

public interface TypeAnnotatableVisitor
{
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
}
