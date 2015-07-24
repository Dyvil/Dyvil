package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

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
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		this.constructor.setModifiers(Modifiers.PRIVATE);
		
		this.checkMethods();
		
		IClassBody body = this.theClass.getBody();
		if (body != null)
		{
			if (markers != null && body.constructorCount() > 0)
			{
				markers.add(this.theClass.getPosition(), "class.object.constructor", this.theClass.getName().qualified);
			}
			
			IField f = body.getField(Name.instance);
			if (f != null)
			{
				this.instanceField = f;
				return;
			}
		}
		
		Field f = new Field(this.theClass, Name.instance, this.theClass.getType());
		f.modifiers = Modifiers.PUBLIC | Modifiers.CONST;
		this.instanceField = f;
		
		ConstructorCall call = new ConstructorCall(null);
		call.type = this.theClass.getType();
		call.constructor = this.constructor;
		
		f.setValue(call);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.instanceField != null && name == Name.instance)
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
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
		if (this.instanceField != null)
		{
			this.instanceField.write(writer);
		}
		
		super.write(writer, instanceFields);
		
		String internalName = this.theClass.getInternalName();
		if ((this.methods & TOSTRING) == 0)
		{
			// Generate a toString() method that simply returns the name of this
			// object type.
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "toString", "()Ljava/lang/String;", null, null));
			mw.begin();
			mw.setThisType(internalName);
			mw.writeLDC(this.theClass.getName().unqualified);
			mw.writeInsn(Opcodes.ARETURN);
			mw.end(Types.STRING);
		}
		
		if ((this.methods & EQUALS) == 0)
		{
			// Generate an equals(Object) method that compares the objects for
			// identity
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null));
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
		
		if ((this.methods & HASHCODE) == 0)
		{
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "hashCode", "()I", null, null));
			mw.begin();
			mw.setThisType(internalName);
			mw.writeLDC(internalName.hashCode());
			mw.writeInsn(Opcodes.IRETURN);
			mw.end();
		}
	}
}
