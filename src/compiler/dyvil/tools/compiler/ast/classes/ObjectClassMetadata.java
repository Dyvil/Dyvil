package dyvil.tools.compiler.ast.classes;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ObjectClassMetadata implements IClassMetadata
{
	protected final IClass	theClass;
	
	private Constructor		constructor;
	protected IField		instanceField;
	
	public ObjectClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
		
		this.constructor = new Constructor(iclass);
	}
	
	@Override
	public IField getInstanceField()
	{
		return this.instanceField;
	}
	
	@Override
	public IConstructor getConstructor()
	{
		return this.constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.constructor.type = this.theClass.getType();
		
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
				this.instanceField = f;
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
		if (name == Name.instance)
		{
			return this.instanceField;
		}
		return null;
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields)
	{
	}
}
