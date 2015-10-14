package dyvil.tools.compiler.ast.classes;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.parsing.marker.MarkerList;

public final class CaseClassMetadata extends ClassMetadata
{
	protected IMethod applyMethod;
	
	public CaseClassMetadata(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		this.checkMethods();
		
		if ((this.methods & APPLY) == 0)
		{
			Method m = new Method(this.theClass, Name.apply, this.theClass.getType(), Modifiers.PUBLIC | Modifiers.STATIC);
			IParameter[] parameters = this.theClass.getParameters();
			int parameterCount = this.theClass.parameterCount();
			
			m.setParameters(parameters, parameterCount);
			m.setTypeVariables(this.theClass.getTypeVariables(), this.theClass.genericCount());
			
			if (parameterCount > 0 && parameters[parameterCount - 1].isVarargs())
			{
				m.setVarargs();
			}
			this.applyMethod = m;
		}
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (name == Name.apply && this.applyMethod != null)
		{
			float match = this.applyMethod.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(this.applyMethod, match));
			}
		}
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
		super.write(writer, instanceFields);
		MethodWriter mw;
		
		if ((this.methods & APPLY) == 0)
		{
			mw = new MethodWriterImpl(writer,
					writer.visitMethod(this.applyMethod.getModifiers(), "apply", this.applyMethod.getDescriptor(), this.applyMethod.getSignature(), null));
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
			this.constructor.writeInvoke(mw, 0);
			mw.writeInsn(Opcodes.ARETURN);
			mw.end(this.theClass.getType());
		}
		
		String internal = this.theClass.getInternalName();
		if ((this.methods & EQUALS) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "equals", "(Ljava/lang/Object;)Z", null, null));
			mw.setThisType(internal);
			mw.registerParameter(1, "obj", Types.OBJECT, 0);
			mw.begin();
			CaseClasses.writeEquals(mw, this.theClass);
			mw.end();
		}
		
		if ((this.methods & HASHCODE) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "hashCode", "()I", null, null));
			mw.setThisType(internal);
			mw.begin();
			CaseClasses.writeHashCode(mw, this.theClass);
			mw.end();
		}
		
		if ((this.methods & TOSTRING) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "toString", "()Ljava/lang/String;", null, null));
			mw.setThisType(internal);
			mw.begin();
			CaseClasses.writeToString(mw, this.theClass);
			mw.end();
		}
	}
}
