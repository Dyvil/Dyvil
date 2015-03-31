package dyvil.tools.compiler.ast.classes;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ObjectClassMetadata implements IClassMetadata
{
	protected final IClass	theClass;
	
	private IConstructor	constructor;
	protected IField		instanceField;
	
	public ObjectClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
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
		IClassBody body = this.theClass.getBody();
		if (body != null && body.getField(Name.instance) != null)
		{
			markers.add(this.theClass.getPosition(), "class.object.constructor");
		}
		
		Field f = new Field(this.theClass, Name.instance, this.theClass.getType());
		f.modifiers = Modifiers.PUBLIC | Modifiers.CONST | Modifiers.SYNTHETIC;
		this.instanceField = f;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
}
