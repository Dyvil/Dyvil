package dyvil.tools.asm;

public interface TypeAnnotatableVisitor
{
	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
}
