package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.field.Field;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import static dyvil.reflect.Modifiers.*;

public final class ObjectClassMetadata extends ClassMetadata
{
	protected IField instanceField;

	public ObjectClassMetadata(IClass iclass)
	{
		super(iclass);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.OBJECT;
	}

	@Override
	public IField getInstanceField()
	{
		return this.instanceField;
	}

	@Override
	public void setInstanceField(IField field)
	{
		this.instanceField = field;
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		super.resolveTypesHeader(markers, context);

		if (!this.theClass.isSubClassOf(Types.SERIALIZABLE))
		{
			this.theClass.getInterfaces().add(Types.SERIALIZABLE);
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		super.resolveTypesGenerate(markers, context);

		final ClassBody body = this.theClass.getBody();
		if (body != null && body.constructorCount() > 0)
		{
			markers.add(Markers.semanticError(this.theClass.getPosition(), "class.object.constructor",
			                                  this.theClass.getName()));
		}

		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.constructor.setAttributes(AttributeList.of(PRIVATE));
		}

		if ((this.members & INSTANCE_FIELD) == 0)
		{
			final Field field = this.createInstanceField();
			this.instanceField = field;
			this.theClass.createBody().addDataMember(field);
		}
		else
		{
			markers.add(
				Markers.semanticError(this.instanceField.getPosition(), "class.object.field", this.theClass.getName()));
		}
	}

	private Field createInstanceField()
	{
		final int flags = PUBLIC | CONST | (this.theClass.isImplicit() ? IMPLICIT : 0);

		final IType classType = this.theClass.getClassType();
		final Field field = new Field(this.theClass, Names.instance, classType, AttributeList.of(flags));
		final ConstructorCall call = new ConstructorCall(null, classType, ArgumentList.EMPTY);
		field.setValue(call);
		return field;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		super.write(writer);

		String internalName = this.theClass.getInternalName();
		if ((this.members & TOSTRING) == 0)
		{
			// Generate a toString() method that simply returns the name of this
			// object type.
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(PUBLIC, "toString",
			                                                                      "()Ljava/lang/String;", null, null));
			mw.visitCode();
			mw.setThisType(internalName);
			mw.visitLdcInsn(this.theClass.getName().unqualified);
			mw.visitInsn(Opcodes.ARETURN);
			mw.visitEnd(Types.STRING);
		}

		if ((this.members & EQUALS) == 0)
		{
			// Generate an equals(Object) method that compares the objects for
			// identity
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(PUBLIC, "equals",
			                                                                      "(Ljava/lang/Object;)Z", null, null));
			mw.visitCode();
			mw.setThisType(internalName);
			mw.visitParameter(1, "obj", Types.ANY, 0);
			mw.visitVarInsn(Opcodes.ALOAD, 0);
			mw.visitVarInsn(Opcodes.ALOAD, 1);
			Label label = new Label();
			mw.visitJumpInsn(Opcodes.IF_ACMPNE, label);
			mw.visitLdcInsn(1);
			mw.visitInsn(Opcodes.IRETURN);
			mw.visitLabel(label);
			mw.visitLdcInsn(0);
			mw.visitInsn(Opcodes.IRETURN);
			mw.visitEnd();
		}

		if ((this.members & HASHCODE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer,
			                                           writer.visitMethod(PUBLIC, "hashCode", "()I", null, null));
			mw.visitCode();
			mw.setThisType(internalName);
			mw.visitLdcInsn(internalName.hashCode());
			mw.visitInsn(Opcodes.IRETURN);
			mw.visitEnd();
		}

		if ((this.members & READ_RESOLVE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(PRIVATE | SYNTHETIC, "readResolve",
			                                                                      "()Ljava/lang/Object;", null, null));
			mw.setThisType(internalName);
			writeGetInstance(mw, internalName);
		}

		if ((this.members & WRITE_REPLACE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(PRIVATE | SYNTHETIC, "writeReplace",
			                                                                      "()Ljava/lang/Object;", null, null));
			mw.setThisType(internalName);
			writeGetInstance(mw, internalName);
		}
	}

	private static void writeGetInstance(MethodWriter mw, String internal) throws BytecodeException
	{
		mw.visitCode();
		mw.visitFieldInsn(Opcodes.GETSTATIC, internal, "instance", 'L' + internal + ';');
		mw.visitInsn(Opcodes.ARETURN);
		mw.visitEnd();
	}

	@Override
	public void writeStaticInit(MethodWriter mw) throws BytecodeException
	{
	}
}
