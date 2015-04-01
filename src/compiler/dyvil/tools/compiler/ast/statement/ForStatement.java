package dyvil.tools.compiler.ast.statement;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ForStatement extends ASTNode implements IStatement, IContext, ILoop
{
	public static final Name	$index		= Name.getQualified("$index");
	public static final Name	$length		= Name.getQualified("$length");
	public static final Name	$array		= Name.getQualified("$array");
	public static final Name	$forStart	= Name.getQualified("$forStart");
	public static final Name	$forUpdate	= Name.getQualified("$forCondition");
	public static final Name	$forEnd		= Name.getQualified("$forEnd");
	
	public static final int		DEFAULT		= 0;
	public static final int		ITERATOR	= 1;
	public static final int		ARRAY		= 2;
	
	private transient IContext	context;
	private IStatement			parent;
	
	public Variable				variable;
	
	public IValue				condition;
	public IValue				update;
	
	public byte					type;
	
	public IValue				then;
	
	protected Label				startLabel;
	protected Label				updateLabel;
	protected Label				endLabel;
	
	protected Variable			var1;
	protected Variable			var2;
	protected Variable			var3;
	
	public ForStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);
	}
	
	@Override
	public int getValueType()
	{
		return FOR;
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable = new Variable(type.getPosition());
		this.variable.type = type;
	}
	
	@Override
	public Label getContinueLabel()
	{
		return this.updateLabel;
	}
	
	@Override
	public Label getBreakLabel()
	{
		return this.endLabel;
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IType getThisType()
	{
		return this.context.getThisType();
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
	public FieldMatch resolveField(Name name)
	{
		if (this.variable != null && this.variable.getName() == name)
		{
			return new FieldMatch(this.variable, 1);
		}
		
		if (this.type == ARRAY)
		{
			if (name == $index)
			{
				return new FieldMatch(this.var1, 1);
			}
			if (name == $length)
			{
				return new FieldMatch(this.var2, 1);
			}
			if (name == $array)
			{
				return new FieldMatch(this.var3, 1);
			}
		}
		
		return this.context.resolveField(name);
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
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		if (name == $forStart)
		{
			return this.startLabel;
		}
		if (name == $forUpdate)
		{
			return this.updateLabel;
		}
		if (name == $forEnd)
		{
			return this.endLabel;
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.resolveTypes(markers, context);
		}
		if (this.type == 0)
		{
			if (this.condition != null)
			{
				this.condition.resolveTypes(markers, context);
			}
			if (this.update != null)
			{
				this.update.resolveTypes(markers, context);
			}
		}
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				((IStatement) this.then).setParent(this);
			}
			
			this.then.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.context = context;
		
		if (this.type != 0)
		{
			IType varType = this.variable.type;
			IValue value = this.variable.value;
			this.variable.value = value.resolve(markers, context);
			
			IType valueType = value.getType();
			int valueTypeDims = valueType.getArrayDimensions();
			if (valueTypeDims != 0)
			{
				this.type = ARRAY;
				if (!valueType.classEquals(varType) || varType.getArrayDimensions() != valueTypeDims - 1)
				{
					Marker marker = markers.create(value.getPosition(), "for.array.type");
					marker.addInfo("Array Type: " + valueType);
					marker.addInfo("Variable Type: " + varType);
					
				}
				else
				{
					Variable var = new Variable(null);
					var.type = Types.INT;
					var.name = $index;
					this.var1 = var;
					
					var = new Variable(null);
					var.type = Types.INT;
					var.name = $length;
					this.var2 = var;
					
					var = new Variable(null);
					var.type = valueType;
					var.name = $array;
					this.var3 = var;
				}
			}
		}
		else
		{
			if (this.variable != null)
			{
				this.variable.resolve(markers, context);
			}
			if (this.condition != null)
			{
				this.condition = this.condition.resolve(markers, this);
			}
			if (this.update != null)
			{
				this.update = this.update.resolve(markers, this);
			}
		}
		
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, this);
		}
		
		this.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			if (this.type == 0)
			{
				this.variable.checkTypes(markers, context);
			}
			else
			{
				this.variable.value.checkTypes(markers, context);
			}
		}
		this.context = context;
		if (this.update != null)
		{
			this.update.checkTypes(markers, this);
		}
		if (this.condition != null)
		{
			IValue condition1 = this.condition.withType(Types.BOOLEAN);
			if (condition1 == null)
			{
				Marker marker = markers.create(this.condition.getPosition(), "for.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
			}
			else
			{
				this.condition = condition1;
			}
			
			this.condition.checkTypes(markers, this);
		}
		if (this.then != null)
		{
			this.then.checkTypes(markers, this);
		}
		this.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.context = context;
		
		if (this.variable != null)
		{
			if (this.type == 0)
			{
				this.variable.check(markers, context);
			}
			else
			{
				this.variable.value.check(markers, context);
			}
		}
		if (this.update != null)
		{
			this.update.check(markers, this);
		}
		if (this.condition != null)
		{
			this.condition.check(markers, this);
		}
		if (this.then != null)
		{
			this.then.check(markers, this);
		}
		
		this.context = null;
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.writeStatement(writer);
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		org.objectweb.asm.Label startLabel = this.startLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label updateLabel = this.updateLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		
		Variable var = this.variable;
		if (this.type == DEFAULT)
		{
			int locals = writer.registerLocal();
			
			// Variable
			if (var != null)
			{
				var.value.writeExpression(writer);
				var.index = locals;
				writer.writeVarInsn(var.type.getStoreOpcode(), var.index);
			}
			
			writer.writeLabel(startLabel);
			// Condition
			if (this.condition != null)
			{
				this.condition.writeInvJump(writer, endLabel);
			}
			// Then
			if (this.then != null)
			{
				this.then.writeStatement(writer);
			}
			// Update
			writer.writeLabel(updateLabel);
			if (this.update != null)
			{
				this.update.writeStatement(writer);
			}
			// Go back to Condition
			writer.writeJumpInsn(Opcodes.GOTO, startLabel);
			
			writer.resetLocals(locals);
			writer.writeLabel(endLabel);
			
			// Variable
			if (var != null)
			{
				writer.writeLocal(var.name.qualified, var.type, startLabel, endLabel, var.index);
			}
			return;
		}
		if (this.type == ARRAY)
		{
			int locals = writer.registerLocal();
			
			Variable arrayVar = this.var3;
			Variable indexVar = this.var1;
			Variable lengthVar = this.var2;
			
			org.objectweb.asm.Label scopeLabel = new org.objectweb.asm.Label();
			writer.writeLabel(scopeLabel);
			
			// Load the array
			var.value.writeExpression(writer);
			// Local Variables
			var.index = locals + 1;
			indexVar.index = locals + 2;
			lengthVar.index = locals + 3;
			arrayVar.index = locals + 4;
			
			writer.writeInsn(Opcodes.DUP);
			arrayVar.writeSet(writer, null, null);
			// Load the length
			writer.writeInsn(Opcodes.ARRAYLENGTH);
			lengthVar.writeSet(writer, null, null);
			// Set index to 0
			writer.writeLDC(0);
			indexVar.writeSet(writer, null, null);
			
			// Jump to boundary check
			writer.writeJumpInsn(Opcodes.GOTO, updateLabel);
			writer.writeLabel(startLabel);
			
			// Load the element
			arrayVar.writeGet(writer, null);
			indexVar.writeGet(writer, null);
			writer.writeInsn(arrayVar.type.getArrayLoadOpcode());
			var.writeSet(writer, null, null);
			
			// Then
			if (this.then != null)
			{
				this.then.writeStatement(writer);
			}
			
			// Increase index
			writer.writeIINC(indexVar.index, 1);
			// Boundary Check
			writer.writeLabel(updateLabel);
			indexVar.writeGet(writer, null);
			lengthVar.writeGet(writer, null);
			writer.writeJumpInsn(Opcodes.IF_ICMPLT, startLabel);
			
			// Local Variables
			writer.resetLocals(locals);
			writer.writeLabel(endLabel);
			
			writer.writeLocal(var.name.qualified, var.type, scopeLabel, endLabel, var.index);
			writer.writeLocal("$index", "I", null, scopeLabel, endLabel, indexVar.index);
			writer.writeLocal("$length", "I", null, scopeLabel, endLabel, lengthVar.index);
			writer.writeLocal("$array", arrayVar.type, scopeLabel, endLabel, arrayVar.index);
			return;
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.forStart);
		if (this.type != 0)
		{
			this.variable.type.toString(prefix, buffer);
			buffer.append(' ').append(this.variable.name).append(Formatting.Statements.forEachSeperator);
			this.variable.value.toString(prefix, buffer);
		}
		else
		{
			if (this.variable != null)
			{
				this.variable.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.condition != null)
			{
				buffer.append(' ');
				this.condition.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.update != null)
			{
				buffer.append(' ');
				this.update.toString(prefix, buffer);
			}
		}
		buffer.append(Formatting.Statements.forEnd);
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				buffer.append('\n').append(prefix);
				this.then.toString(prefix, buffer);
			}
			else
			{
				buffer.append(' ');
				this.then.toString(prefix, buffer);
			}
		}
	}
}
