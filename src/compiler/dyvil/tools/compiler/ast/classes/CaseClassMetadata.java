package dyvil.tools.compiler.ast.classes;

import java.util.List;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.*;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.transform.CaseClasses;

public class CaseClassMetadata implements IClassMetadata
{
	protected final IClass	theClass;
	
	protected IConstructor	constructor;
	protected IMethod		applyMethod;
	
	public CaseClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public IConstructor getConstructor()
	{
		if (this.constructor != null)
		{
			return this.constructor;
		}
		
		Constructor constructor = new Constructor(this.theClass);
		constructor.modifiers = Modifiers.PUBLIC | Modifiers.SYNTHETIC;
		return this.constructor = constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		Method m = new Method(this.theClass, Name.apply, this.theClass.getType());
		m.modifiers = Modifiers.PUBLIC | Modifiers.STATIC | Modifiers.SYNTHETIC;
		m.setParameters(this.theClass.getParameters(), this.theClass.parameterCount());
		this.applyMethod = m;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (name == Name.apply && this.applyMethod != null)
		{
			int match = this.applyMethod.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(this.applyMethod, match));
			}
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (this.constructor != null)
		{
			int match = this.constructor.getSignatureMatch(arguments);
			if (match > 0)
			{
				list.add(new ConstructorMatch(this.constructor, match));
			}
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if (this.constructor != null)
		{
			// TODO
		}
		
		MethodWriter mw = new MethodWriterImpl(writer,
				writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "equals", "(Ljava/lang/Object;)Z", null, null));
		mw.setInstanceMethod();
		mw.registerParameter("obj", "java/lang/Object");
		mw.begin();
		CaseClasses.writeEquals(mw, this.theClass);
		mw.end();
		
		mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "hashCode", "()I", null, null));
		mw.setInstanceMethod();
		mw.begin();
		CaseClasses.writeHashCode(mw, this.theClass);
		mw.end();
		
		mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "toString", "()Ljava/lang/String;", null, null));
		mw.setInstanceMethod();
		mw.begin();
		CaseClasses.writeToString(mw, this.theClass);
		mw.end();
		
		if (this.applyMethod != null)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(this.applyMethod.getModifiers(), "apply", this.applyMethod.getDescriptor(), null, null));
			mw.begin();
			mw.writeTypeInsn(Opcodes.NEW, this.theClass.getType().getInternalName());
			mw.writeInsn(Opcodes.DUP);
			int len = this.theClass.parameterCount();
			for (int i = 0; i < len; i++)
			{
				IParameter param = this.theClass.getParameter(i);
				param.write(mw);
				mw.writeVarInsn(param.getType().getLoadOpcode(), i);
			}
			this.constructor.writeInvoke(mw, EmptyArguments.INSTANCE);
			mw.writeInsn(Opcodes.ARETURN);
			mw.end(this.theClass.getType());
		}
	}
}
