package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ObjectClassMetadata extends ClassMetadata
{
	protected IField	instanceField;
	private boolean		hasToString;
	
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
		f.modifiers = Modifiers.PUBLIC | Modifiers.CONST | Modifiers.SYNTHETIC;
		this.instanceField = f;
		
		if (this.theClass.getMethod(Name.toString, null, 0, null) != null)
		{
			this.hasToString = true;
		}
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
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
		if (this.instanceField != null)
		{
			this.instanceField.write(writer);
		}
		
		super.write(writer, instanceFields);
		
		if (!this.hasToString)
		{
			// Generate a toString() method that simply returns the name of this
			// object type.
			MethodWriterImpl mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "toString", "()Ljava/lang/String;", null, null));
			mw.begin();
			mw.setThisType(this.theClass.getInternalName());
			mw.writeLDC(this.theClass.getName().unqualified);
			mw.writeInsn(Opcodes.ARETURN);
			mw.end(Types.STRING);
		}
	}
}
