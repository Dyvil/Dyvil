package dyvil.tools.compiler.ast.classes;

import java.util.List;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.transform.CaseClasses;

public final class CaseClassMetadata extends ClassMetadata
{
	protected IMethod	applyMethod;
	
	public CaseClassMetadata(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
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
	public void write(ClassWriter writer, IValue instanceFields)
	{
		super.write(writer, instanceFields);
		
		MethodWriter mw = new MethodWriterImpl(writer,
				writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "equals", "(Ljava/lang/Object;)Z", null, null));
		mw.setInstanceMethod();
		mw.registerParameter(0, "obj", Types.OBJECT);
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
			this.constructor.writeInvoke(mw);
			mw.writeInsn(Opcodes.ARETURN);
			mw.end(this.theClass.getType());
		}
	}
}
