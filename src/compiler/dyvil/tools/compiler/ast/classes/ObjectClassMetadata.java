package dyvil.tools.compiler.ast.classes;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ObjectClassMetadata extends ClassMetadata
{
	protected IField	instanceField;
	
	public ObjectClassMetadata(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public IField getInstanceField()
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
			if (body.constructorCount() > 0)
			{
				markers.add(this.theClass.getPosition(), "class.object.constructor", this.theClass.getName().qualified);
			}
			
			IField f = body.getField(Name.instance);
			if (f != null)
			{
				return;
			}
		}
		
		Field f = new Field(this.theClass, Name.instance, this.theClass.getType());
		f.modifiers = Modifiers.PUBLIC | Modifiers.CONST | Modifiers.SYNTHETIC;
		this.instanceField = f;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (this.instanceField != null && name == Name.instance)
		{
			return this.instanceField;
		}
		return null;
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields)
	{
		if (this.instanceField != null)
		{
			this.instanceField.write(writer);
		}
		
		super.write(writer, instanceFields);
	}
}
