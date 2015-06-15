package dyvil.tools.compiler.ast.expression;

import dyvil.lang.List;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.pattern.IPatterned;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class CaseExpression extends ASTNode implements IValue, IValued, IPatterned, IClassCompilable, IContext
{
	public static final IClass			PARTIALFUNCTION_CLASS	= Package.dyvilFunction.resolveClass("PartialFunction");
	public static final Type			PARTIALFUNCTION			= new Type(PARTIALFUNCTION_CLASS);
	public static final ITypeVariable	PAR_TYPE				= PARTIALFUNCTION_CLASS.getTypeVariable(0);
	public static final ITypeVariable	RETURN_TYPE				= PARTIALFUNCTION_CLASS.getTypeVariable(1);
	
	protected IPattern					pattern;
	protected IValue					condition;
	protected IValue					value;
	
	protected IType						type;
	private String						internalClassName;
	
	private transient IContext			context;
	
	public CaseExpression(ICodePosition position)
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
	
	public void setMatchCase()
	{
		this.type = Types.UNKNOWN;
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			GenericType gt = new GenericType(PARTIALFUNCTION_CLASS);
			IType t1 = this.pattern.getType();
			if (t1.isPrimitive())
			{
				t1 = t1.getReferenceType();
			}
			gt.addType(t1);
			
			if (this.value != null)
			{
				t1 = this.value.getType();
				if (t1.isPrimitive())
				{
					t1 = t1.getReferenceType();
				}
				gt.addType(t1);
			}
			else
			{
				gt.addType(new Type(Types.VOID_CLASS));
			}
			return this.type = gt;
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (this.isType(type))
		{
			this.type = type;
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(PARTIALFUNCTION);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.getTheClass() == PARTIALFUNCTION_CLASS)
		{
			return 3;
		}
		if (this.isType(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	public IValue getCondition()
	{
		return this.condition;
	}
	
	@Override
	public void setPattern(IPattern pattern)
	{
		this.pattern = pattern;
	}
	
	@Override
	public IPattern getPattern()
	{
		return this.pattern;
	}
	
	// IContext
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.context.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.context.getThisClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		IField f = this.pattern.resolveField(name);
		if (f != null)
		{
			return f;
		}
		
		if (this.type == Types.UNKNOWN)
		{
			return this.context.resolveField(name);
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return this.context.handleException(type);
	}
	
	@Override
	public byte getVisibility(IMember member)
	{
		return this.context.getVisibility(member);
	}
	
	// Phases
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public CaseExpression resolve(MarkerList markers, IContext context)
	{
		this.context = context;
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, this);
		}
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
		}
		this.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.context = context;
		if (this.condition != null)
		{
			this.condition.checkTypes(markers, this);
		}
		
		if (this.type != Types.UNKNOWN)
		{
			if (this.type == null)
			{
				this.getType();
			}
			
			IContext.addCompilable(context, this);
			
			if (this.pattern != null)
			{
				this.pattern.resolve(markers, context);
				IType type1 = this.type.resolveType(PAR_TYPE);
				this.pattern = this.pattern.withType(type1);
				this.pattern.checkTypes(markers, context);
			}
			if (this.value != null)
			{
				IType type1 = this.type.resolveType(RETURN_TYPE);
				this.value = this.value.withType(type1);
			}
		}
		
		if (this.value != null)
		{
			this.value.checkTypes(markers, this);
		}
		
		this.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.context = context;
		if (this.condition != null)
		{
			this.condition.check(markers, this);
		}
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		this.context = null;
	}
	
	@Override
	public CaseExpression foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
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
		mw.writeVarInsn(Opcodes.ALOAD, 0);
		mw.writeVarInsn(Opcodes.ALOAD, 1);
		mw.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, this.internalClassName, "isDefined", definedDesc, false);
		mw.writeJumpInsn(Opcodes.IFNE, elseLabel);
		mw.writeInsn(Opcodes.ACONST_NULL);
		mw.writeInsn(Opcodes.ARETURN);
		mw.writeLabel(elseLabel);
		if (this.value != null)
		{
			this.value.writeExpression(mw);
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
		buffer.append(" : ");
		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
		}
	}
}
