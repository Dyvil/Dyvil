package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.pattern.ICase;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class CaseExpression implements IValue, ICase, IClassCompilable, IDefaultContext
{
	public static final IClass			PARTIALFUNCTION_CLASS	= Package.dyvilFunction.resolveClass("PartialFunction");
	public static final ClassType		PARTIALFUNCTION			= new ClassType(PARTIALFUNCTION_CLASS);
	public static final ITypeVariable	PAR_TYPE				= PARTIALFUNCTION_CLASS.getTypeVariable(0);
	public static final ITypeVariable	RETURN_TYPE				= PARTIALFUNCTION_CLASS.getTypeVariable(1);
	
	protected ICodePosition position;
	
	protected IPattern	pattern;
	protected IValue	condition;
	protected IValue	action;
	
	// Metadata
	protected IType	type;
	private String	internalClassName;
	
	public CaseExpression(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return CASE_STATEMENT;
	}
	
	@Override
	public boolean hasSeparateFile()
	{
		return true;
	}
	
	@Override
	public void setInnerIndex(String internalName, int index)
	{
		this.internalClassName = internalName + "$" + index;
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			ClassGenericType gt = new ClassGenericType(PARTIALFUNCTION_CLASS);
			IType t1 = this.pattern.getType();
			if (t1.isPrimitive())
			{
				t1 = t1.getObjectType();
			}
			gt.addType(t1);
			
			if (this.action != null)
			{
				t1 = this.action.getType();
				if (t1.isPrimitive())
				{
					t1 = t1.getObjectType();
				}
				gt.addType(t1);
			}
			else
			{
				gt.addType(new ClassType(Types.VOID_CLASS));
			}
			return this.type = gt;
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.isType(type))
		{
			this.type = type;
			
			IType type1 = this.type.resolveType(RETURN_TYPE);
			this.action = this.action.withType(type1, typeContext, markers, context);
			return this;
		}
		return null;
	}
	
	@Override
	public IValue getAction()
	{
		return this.action;
	}
	
	@Override
	public void setAction(IValue action)
	{
		this.action = action;
	}
	
	@Override
	public IValue getCondition()
	{
		return this.condition;
	}
	
	@Override
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	@Override
	public IPattern getPattern()
	{
		return this.pattern;
	}
	
	@Override
	public void setPattern(IPattern pattern)
	{
		this.pattern = pattern;
	}
	
	// IContext
	
	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember f = this.pattern.resolveField(name);
		if (f != null)
		{
			return f;
		}
		
		return null;
	}
	
	// Phases
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.pattern != null)
		{
			this.pattern.resolve(markers, context);
			
			if (this.type == null)
			{
				this.getType();
			}
			
			IType type1 = this.type.resolveType(PAR_TYPE);
			this.pattern = this.pattern.withType(type1, markers);
			// TODO Handle error
		}
		
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context1);
		}
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context1);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition.checkTypes(markers, context1);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, context1);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition.check(markers, context1);
		}
		if (this.action != null)
		{
			this.action.check(markers, context1);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		context.getHeader().addInnerClass(this);
		
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(context1, compilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(context1, compilableList);
		}
		return this;
	}
	
	@Override
	public String getFileName()
	{
		return this.internalClassName.substring(this.internalClassName.lastIndexOf('/') + 1);
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		IType parType = this.type.resolveType(PAR_TYPE);
		IType returnType = this.type.resolveType(RETURN_TYPE);
		String parFrameType = parType.getInternalName();
		
		StringBuilder builder = new StringBuilder("Ljava/lang/Object;");
		this.type.appendSignature(builder);
		
		// Header
		String signature = builder.toString();
		writer.visit(DyvilCompiler.classVersion, 0, this.internalClassName, signature, "java/lang/Object", new String[] { "dyvil/function/PartialFunction" });
		
		// Constructor
		MethodVisitor mv = writer.visitMethod(0, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		// isDefined
		
		StringBuilder descBuilder = new StringBuilder();
		descBuilder.append('(');
		parType.appendExtendedName(descBuilder);
		String definedDesc = descBuilder.append(")Z").toString();
		
		StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append('(');
		parType.appendSignature(signatureBuilder);
		signature = signatureBuilder.append(")Z").toString();
		
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "isDefined", definedDesc, signature, null));
		Label elseLabel = new Label();
		mw.begin();
		mw.setThisType(this.internalClassName);
		mw.setLocalType(1, parFrameType);
		
		this.pattern.writeInvJump(mw, 1, elseLabel);
		if (this.condition != null)
		{
			this.condition.writeInvJump(mw, elseLabel);
		}
		
		mw.writeLDC(1);
		mw.writeInsn(Opcodes.IRETURN);
		mw.writeLabel(elseLabel);
		mw.writeLDC(0);
		mw.writeInsn(Opcodes.IRETURN);
		mw.end();
		
		// apply
		
		descBuilder.deleteCharAt(descBuilder.length() - 1);
		returnType.appendExtendedName(descBuilder);
		String applyDesc = descBuilder.toString();
		
		signatureBuilder.deleteCharAt(signatureBuilder.length() - 1);
		returnType.appendExtendedName(signatureBuilder);
		signature = signatureBuilder.toString();
		
		mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "apply", applyDesc, signature, null));
		elseLabel = new Label();
		mw.begin();
		mw.setThisType(this.internalClassName);
		mw.setLocalType(1, parFrameType);
		
		this.pattern.writeInvJump(mw, 1, elseLabel);
		if (this.condition != null)
		{
			this.condition.writeInvJump(mw, elseLabel);
		}
		
		mw.writeInsn(Opcodes.ACONST_NULL);
		mw.writeInsn(Opcodes.ARETURN);
		mw.writeLabel(elseLabel);
		if (this.action != null)
		{
			this.action.writeExpression(mw, returnType);
		}
		else
		{
			mw.writeInsn(Opcodes.ACONST_NULL);
		}
		mw.writeInsn(Opcodes.ARETURN);
		mw.end(returnType);
		
		// Bridge Methods
		
		if (!parType.classEquals(Types.OBJECT) || !returnType.classEquals(Types.OBJECT))
		{
			// isDefined bridge
			
			mv = writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE, "isDefined", "(Ljava/lang/Object;)Z", null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, parFrameType);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.internalClassName, "isDefined", definedDesc, false);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitMaxs(2, 2);
			
			// apply bridge
			
			mv = writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE, "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitTypeInsn(Opcodes.CHECKCAST, parFrameType);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.internalClassName, "apply", applyDesc, false);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(2, 2);
		}
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeTypeInsn(Opcodes.NEW, this.internalClassName);
		writer.writeInsn(Opcodes.DUP);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, this.internalClassName, "<init>", "()V", false);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("case ");
		if (this.pattern != null)
		{
			this.pattern.toString(prefix, buffer);
		}
		if (this.condition != null)
		{
			buffer.append(" if ");
			this.condition.toString(prefix, buffer);
		}
		buffer.append(" => ");
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
