package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class ObjectClassMetadata extends ClassMetadata
{
	protected IField instanceField;
	
	public ObjectClassMetadata(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public IDataMember getInstanceField()
	{
		return this.instanceField;
	}
	
	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		super.resolveTypesHeader(markers, context);
		
		if (!this.theClass.isSubTypeOf(Types.SERIALIZABLE))
		{
			this.theClass.addInterface(Types.SERIALIZABLE);
		}
	}
	
	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
		super.resolveTypesBody(markers, context);
		
		this.checkMethods();
		
		final IClassBody body = this.theClass.getBody();
		if (body != null)
		{
			if (markers != null && body.constructorCount() > 0)
			{
				markers.add(Markers.semantic(this.theClass.getPosition(), "class.object.constructor",
				                             this.theClass.getName().qualified));
			}
			
			final IField field = body.getField(Names.instance);
			if (field != null)
			{
				this.members |= INSTANCE_FIELD;
				this.instanceField = field;
			}
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		super.resolveTypesGenerate(markers, context);

		final Field field = new Field(this.theClass, Names.instance, this.theClass.getType(),
		                              new FlagModifierSet(Modifiers.PUBLIC | Modifiers.CONST));
		this.instanceField = field;
		
		this.constructor.setModifiers(new FlagModifierSet(Modifiers.PRIVATE));
		ConstructorCall call = new ConstructorCall(null, this.constructor, EmptyArguments.INSTANCE);
		field.setValue(call);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.instanceField != null && name == Names.instance)
		{
			return this.instanceField;
		}
		return null;
	}
	
	@Override
	public void writeStaticInit(MethodWriter mw) throws BytecodeException
	{
		if (this.instanceField != null)
		{
			this.instanceField.writeStaticInit(mw);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.instanceField != null)
		{
			this.instanceField.write(writer);
		}
		
		super.write(writer);
		
		String internalName = this.theClass.getInternalName();
		if ((this.members & TOSTRING) == 0)
		{
			// Generate a toString() method that simply returns the name of this
			// object type.
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "toString",
			                                                                      "()Ljava/lang/String;", null, null));
			mw.begin();
			mw.setThisType(internalName);
			mw.writeLDC(this.theClass.getName().unqualified);
			mw.writeInsn(Opcodes.ARETURN);
			mw.end(Types.STRING);
		}
		
		if ((this.members & EQUALS) == 0)
		{
			// Generate an equals(Object) method that compares the objects for
			// identity
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "equals",
			                                                                      "(Ljava/lang/Object;)Z", null, null));
			mw.begin();
			mw.setThisType(internalName);
			mw.registerParameter(1, "obj", Types.ANY, 0);
			mw.writeVarInsn(Opcodes.ALOAD, 0);
			mw.writeVarInsn(Opcodes.ALOAD, 1);
			Label label = new Label();
			mw.writeJumpInsn(Opcodes.IF_ACMPNE, label);
			mw.writeLDC(1);
			mw.writeInsn(Opcodes.IRETURN);
			mw.writeLabel(label);
			mw.writeLDC(0);
			mw.writeInsn(Opcodes.IRETURN);
			mw.end();
		}
		
		if ((this.members & HASHCODE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer,
			                                           writer.visitMethod(Modifiers.PUBLIC, "hashCode", "()I", null,
			                                                              null));
			mw.begin();
			mw.setThisType(internalName);
			mw.writeLDC(internalName.hashCode());
			mw.writeInsn(Opcodes.IRETURN);
			mw.end();
		}
		
		if ((this.members & READ_RESOLVE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer,
			                                           writer.visitMethod(Modifiers.PRIVATE | Modifiers.SYNTHETIC,
			                                                              "readResolve", "()Ljava/lang/Object;", null,
			                                                              null));
			writeResolveMethod(mw, internalName);
		}
		
		if ((this.members & WRITE_REPLACE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer,
			                                           writer.visitMethod(Modifiers.PRIVATE | Modifiers.SYNTHETIC,
			                                                              "writeReplace", "()Ljava/lang/Object;", null,
			                                                              null));
			writeResolveMethod(mw, internalName);
		}
	}
	
	private static void writeResolveMethod(MethodWriter mw, String internal) throws BytecodeException
	{
		mw.setThisType(internal);
		mw.begin();
		mw.writeFieldInsn(Opcodes.GETSTATIC, internal, "instance", 'L' + internal + ';');
		mw.writeInsn(Opcodes.ARETURN);
		mw.end();
	}
}
