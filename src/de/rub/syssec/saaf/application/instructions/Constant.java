/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.application.instructions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.CodeLine;
import de.rub.syssec.saaf.application.Field;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;
import de.rub.syssec.saaf.model.application.InstructionType;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.SyntaxException;

/**
 * This class describes a constant in SMALI bytecode. It is parsed from the instructions itself. A constant is equal to another constant
 * if both have the same {@linkplain CodeLine}. 
 * 
 * @author Johannes Hoffmann
 *
 */
public class Constant implements ConstantInterface {
	
	private final String value;
	private final String identifier;
	private final VarType varType;
	private final CodeLineInterface cl;
	private static final boolean DEBUG=Boolean.parseBoolean(System.getProperty("debug.slicing","false"));
	private static final Logger LOGGER = Logger.getLogger(Constant.class);
	private static final String STRING_DESCRIPTION = "java/lang/String";
	
	private final VariableType variableType;
	private int fuzzyLevel;
	private final LinkedList<BasicBlockInterface> path;
	
	private final int searchId;
	

	/**
	 * Parse the constant from one CodeLine. Additional information is retrieved from nearby SMALI .local or .restart local lines
	 * if they are available.
	 * 
	 * @param cl the CodeLine to parse
	 * @param fuzzyLevel set this to >0 if this Constant was found in an inaccurate fuzzy search
	 * @param path the path the DetectionLogic took to find this constant. It is saved to a new object and the given reference may be reused
	 * @param searchId an Id which all Constants should have in common which were found during one run of the DetectionLogic for one tracked invoke
	 * @throws SyntaxException if something goes wrong
	 */
	public Constant(CodeLineInterface cl, int fuzzyLevel, LinkedList<BasicBlockInterface> path, int searchId) throws SyntaxException {
		this.cl = cl;
		this.fuzzyLevel = fuzzyLevel;
		this.path = new LinkedList<BasicBlockInterface>(path);
		this.searchId = searchId;
		/*
		 * FIELD
		 */
		if (cl.startsWith(Field.FIELD)) {
			variableType = VariableType.FIELD_CONSTANT;
			// parse name
			int colonIndex = ByteUtils.indexOf(cl.getLine(), ':');
			int spaceBeforeColonPos = ByteUtils.indexOfReverse(cl.getLine(), ' ', colonIndex);
			identifier = new String(ByteUtils.subbytes(cl.getLine(), spaceBeforeColonPos+1, colonIndex));
			
			/**
			 * Parse type and value
			 * Syntax: http://code.google.com/p/smali/wiki/TypesMethodsAndFields
			 */
			int equalSignIndex = ByteUtils.indexOf(cl.getLine(), '='); // backward/reverse search not possible b/c the value might be a String containing =
			if (equalSignIndex < 0) {
//				no value, eg: .field private name:I
				value = null;
				varType = new VarType(ByteUtils.subbytes(cl.getLine(), colonIndex+1));
			}
			else {
				// eg: .field private static final name:J = 0x1L
				varType = new VarType(ByteUtils.subbytes(cl.getLine(), colonIndex+1, equalSignIndex-1));
				value = parseConstant(varType.getType(), new String(ByteUtils.subbytes(cl.getLine(), equalSignIndex+2)));	
			}
			return;
		}
		
		/*
		 * VARIABLE OR ANONYMOUS CONSTANT INSIDE METHOD
		 */
		else if (cl.getInstruction().getType() == InstructionType.CONST) {
			/*
			 * 
			 * Parse the type from the NEXT .local .line cl
			 *  
			 * 41 const-string v7, "string" 			<-- THE ORIGINAL CL
			 * .line 43 .local v7, s:Ljava/lang/String; <-- PARSE THIS LINE
			 * .restart local v0 #name:type 			<-- OR THIS LINE
			 * 
			 * If no .local or .restart line is present, it is an anonymous constant
			 */
			LinkedList<byte[]> splittedCl = Instruction.split(cl.getLine());
			byte[] constRegister = splittedCl.get(1);
			// Get the previous codeline
			boolean isRestarted = false;
			CodeLineInterface localCl = getNextMetadataLineForConstant(cl,
					MetaDataLine.LOCAL, constRegister);
			if (localCl != null) {
				variableType = VariableType.LOCAL_VARIABLE; // parse it later
			} else if ((localCl = getNextMetadataLineForConstant(cl,
					MetaDataLine.RESTART_LOCAL, constRegister)) != null) {
				variableType = VariableType.LOCAL_VARIABLE; // parse it later
				isRestarted = true;
			} else { // localCl = null, no corresponding line found
				variableType = VariableType.LOCAL_ANONYMOUS_CONSTANT;
				/*
				 * A little cheating here: If we have a const-string/jumbo or
				 * const-string opcode we know it is a string, but we do not
				 * have the String class referenced as the type, so we just set
				 * it here and we return a VarType of type String.
				 */
				final byte[] constString = "const-string".getBytes(); // also covers const-string/jumbo
				final byte[] stringType = ("L" + STRING_DESCRIPTION).getBytes();
				if (cl.startsWith(constString)) {
					// it is a String
					varType = new VarType(stringType);
				} else {
					// it is not a String, make it UNKNOWN
					varType = new VarType(null);
					// manually set the constant description to, eg, const/4.
					varType.setTypeDescription(new String(splittedCl.getFirst()));
				}
				identifier = null; // it is an anonymous constant
				// nevertheless try to parse the value
				value = parseConstant(varType.getType(),
						new String(splittedCl.getLast()));
				return;
			}

			// .restart local v0 #name:type
			// .local v7, s:Ljava/lang/String;
			LinkedList<byte[]> splittedLocalCl = Instruction.split(localCl
					.getLine()); // split is now the splitted .local line!
			byte[] nameType = splittedLocalCl.getLast();
			int colonIndex = ByteUtils.indexOf(nameType, ':');
			/*
			 * Local Line may look like this: restart local p4 and this
			 * would result in an negative array exception.
			 */
			int offset;
			if (colonIndex >= 0) {
				 offset = 0;
				if (isRestarted) {
					offset = 1; // remove the # for a restart local line
				}
				identifier = new String(ByteUtils.subbytes(nameType, 0 + offset,
						colonIndex));
			}
			else { // no identifier found
				identifier = null;
			}
			offset = 0; // cut ; at the end if applicable
			if (nameType[nameType.length - 1] == ';')
				offset = 1; // remove the ; from a non primitive type
			varType = new VarType(ByteUtils.subbytes(nameType, colonIndex + 1,
					nameType.length - offset));
			value = parseConstant(varType.getType(),
					new String(splittedCl.getLast()));
			return;
		}
			
		
		/*
		 * MATH2C
		 */
		else if (cl.getInstruction().getType() == InstructionType.MATH_2C) {
			variableType = VariableType.MATH_OPCODE_CONSTANT;
			LinkedList<byte[]> split = Instruction.split(cl.getLine());
			identifier = null;
			varType = new VarType();
			value = parseConstant(varType.getType(),
					new String(split.getLast()));
			return;
		}
		
		/*
		 * NEW ARRAY (FILL_ARRAY_DATA)
		 */
		else if (cl.getInstruction().getType() == InstructionType.FILL_ARRAY_DATA) {
			variableType = VariableType.ARRAY;
			/*
			 * Code looks like this:
			 * 
			 * new-array v4, v9, [I			<-- OR eventually this (else)
			 * fill-array-data v4, :array_0	<-- CURRENT INSTRUCTION
			 * .line 55
			 * .local v4, iiirrr:[I			<-- PARSE THIS LINE! (if)
			 * 
			 * First, find the line. Ignore empty lines etc. Use the register number to identify it correctly
			 *  
			 */
			LinkedList<byte[]> split = Instruction.split(cl.getLine());
			byte[] arrayRegister = split.get(1);
			CodeLineInterface localLine = getNextMetadataLineForConstant(cl, MetaDataLine.LOCAL, arrayRegister);
			if (localLine != null) {
				LinkedList<byte[]> split2 = Instruction.split(localLine.getLine());
				byte[] nameType = split2.getLast();
				int colonIndex = ByteUtils.indexOf(nameType, ':');
				identifier = new String(ByteUtils.subbytes(nameType, 0, colonIndex));
				varType = new VarType(ByteUtils.subbytes(nameType, colonIndex+1));

				byte[] arrayLabel = cl.getInstruction().getLabel();
				value = parseFillArrayDataOpCode(arrayLabel, cl.getMethod());
				
				return;
			}
			else {
				if (DEBUG) LOGGER.debug("LocalLine was null, searching for previous new-array line.");
				/*
				 * Get the previous new-array line and parse the type from it
				 */
				int localLineNr = cl.getLineNr()-1;	// current line index
				while (--localLineNr >= 0) {		// get the previous line
					localLine = cl.getSmaliClass().getAllCodeLines().get(localLineNr);
					if (!localLine.isCode()) continue;
					else if (localLine.getInstruction().getType() == InstructionType.NEW_ARRAY // opcode ok
						&& Arrays.equals(localLine.getInstruction().getResultRegister(), arrayRegister)) { // registers match
						// we found the corresponding new-array line
						split = Instruction.split(localLine.getLine()); // do not use CL
						varType = new VarType(split.getLast());
						identifier = null; // TODO: This could be parsed from a field, if it is moved to any
						byte[] arrayLabel = cl.getInstruction().getLabel();
						value = parseFillArrayDataOpCode(arrayLabel, cl.getMethod());
						return;
					}
					else {
						// search missed :(
						break;
					}
					
				}
				// If we reach this we were unable to parse anything correctly
				throw new SyntaxException("Could not parse array constant!");
			}
		}
		
		/*
		 * NEW ARRAY
		 */
		else if (cl.getInstruction().getType() == InstructionType.NEW_ARRAY) {
			/*	It might be something like this:
			 *  const/4 v0, 0x3									<-- need to find, the dimension
			 *  new-array v0, v0, [I							<-- found
			 *  sput-object v0, Ltest/android/Testcase5;->a1:[I	<-- started
			 *  Java code: private static final int[] a1 = { 0, 0, 0 };
			 */
			LinkedList<byte[]> split = Instruction.split(cl.getLine());
			value = new String (cl.toString()); // TODO: Parse the dimension (register needs to be tracked)!
			varType = new VarType(split.getLast());
			variableType = VariableType.ARRAY;
			identifier = null;
			return;
		}
		
		else if (cl.getInstruction().getType() == InstructionType.INVOKE || cl.getInstruction().getType() == InstructionType.INVOKE_STATIC) {
			/*
			 * invoke-virtual {v7}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
			 * 
			 * We found an INVOKE and result was moved to the backtracked register. We therefore assume that the method returns
			 * something of interest, eg, android.telephony.TelephonyManager.getDeviceId(). A constant will only be created if
			 * the search is not fuzzy and only if the method is unknown, eg, an API method. Otherwise, the return values of the
			 * method would normally be backtracked. 
			 * 
			 * The line above returns nothing, or, void. This should never happen as we never saw a move-result.
			 */
			int cpIndex = ByteUtils.indexOf(cl.getLine(), ')');
			byte[] returnType = ByteUtils.subbytes(cl.getLine(), cpIndex+1);
			if (returnType.length == 1 && returnType[0] == 'V') {
//				LOGGER.error( "Found a method which should be treated as a constant which returns void!");
				throw new SyntaxException("Found a method which should be treated as a constant but it returns void!"); // TODO: Should be a detection error?
			}
			variableType = VariableType.EXTERNAL_METHOD;
			varType = new VarType(returnType);
			byte[][] cmp = cl.getInstruction().getCalledClassAndMethodWithParameter();
			identifier = null;
			value = new String(cmp[0])+"->"+new String(cmp[1])+"("+new String(cmp[2])+")"; // the class->method is our supposed value
			return;
		}
		
		else if (cl.getInstruction().getType() == InstructionType.INTERNAL_SMALI_OPCODE) {
			// This should only occur in rare situations. The opcode has no relevant infos for us, so just let everything unset.
			variableType = VariableType.INTERNAL_BYTECODE_OP;
			varType = new VarType(null);
			identifier = null;
			value = null;
			return;
		}

		// Something went wrong...
		throw new SyntaxException("Could not parse Constant, unknown type!");
	}
	
	/**
	 * Create a new constant. This constructor will not parse the CodeLine but will set the provided type and
	 * value. This way, one can create arbitrary "constant-types" which do not really relate to some CodeLine
	 * or where one CodeLine is not enough to predict a correct value. 
	 * 
	 * @param cl the CodeLine which is not parsed FIXME: must not be null, but this should be fixed such that the complete cl parameter is dropped
	 * @param fuzzyLevel set this to >0 if this Constant was found in an inaccurate fuzzy search
 	 * @param path the path the DetectionLogic took to find this constant. It is saved to a new object and the given reference may be reused
	 * @param searchId an Id which all Constants should have in common which were found during one run of the DetectionLogic for one tracked invoke
	 * @param type the type of the constant
	 * @param value the freely chosen value
	 */
	public Constant(CodeLineInterface cl, int fuzzyLevel, LinkedList<BasicBlockInterface> path, int searchId, VariableType variableType, String value) {
		this.variableType = variableType;
		varType = new VarType(null);
		this.searchId = searchId;
		identifier = null;
		this.value = value;
		this.path = new LinkedList<BasicBlockInterface>(path);
		this.cl = cl;
	}
	
	private static class VarType {
		private Type type;
		private String typeDescription; // may be null
		private final int arrayDimension;
		
		/**
		 * Use this constructor from a MATH_2 opcode
		 * FIXME: This is badly hacked...
		 * @param isArray
		 */
		private VarType() {
			type = Type.MATH_OP;
			typeDescription = null;
			arrayDimension = 0;
		}
		
		
		/**
		 * Use this constructor to parse a value like "[[[Ljava/lang/String;".
		 * The L is crucial, the ; is optional.
		 * @param code
		 */
		private VarType(byte[] code) {
			// sanity check
			if (code == null || code.length == 0) {
				type = Type.UNKNOWN;
				typeDescription = null;
				arrayDimension = 0;
				return;
			}
			
			// get the array dimension
			int ad = 0;
			for (byte bb : code) {
				if (bb == '[') ad++;
			}
			arrayDimension = ad;
			
			// parse the type
			byte b = code[arrayDimension]; // for each dimension a [ is prefixed
			switch (b) {
			case 'Z':
				type = Type.BOOLEAN;
				break;
			case 'B':
				type = Type.BYTE;
				break;
			case 'S':
				type = Type.SHORT;
				break;
			case 'C':
				type = Type.CHAR;
				break;
			case 'I':
				type = Type.INTEGER;
				break;
			case 'J':
				type = Type.LONG;
				break;
			case 'F':
				type =Type.FLOAT;
				break;
			case 'D':
				type = Type.DOUBLE;
				break;
			case 'L':
				type = Type.OTHER_CLASS;
				break;
			default:
				// we could not parse it...
				type = Type.UNKNOWN;
			}
			
			// parse the non-primitive type
			if (type == Type.OTHER_CLASS) {
				// code looks like this [[[Ljava/lang/String; We do not want the [, L and the terminating ;
				int endOffset = 0; // check if it ends w/ ; (this depends on the input whether it was already removed
				if (code[code.length-1] == ';') endOffset = 1;
				typeDescription = new String(ByteUtils.subbytes(code, arrayDimension+1, code.length-endOffset));
				if (STRING_DESCRIPTION.equals(typeDescription)) type = Type.STRING;
			}
			else if (ad > 0) {
				/*
				 * Fix the type and description for arrays.
				 * Type will be Type.Array and description
				 * will be, eg, int[] or some/Class[][]. 
				 */
				final String s = "[]";
				StringBuilder sb = new StringBuilder();
				sb.append(type);
				for (int i=0; i<arrayDimension; i++) {
					sb.append(s);
				}
				typeDescription = sb.toString();
				type = Type.ARRAY;
			}
			else typeDescription = null;
		}
		
		
//		public boolean isArray() {
//			return arrayDimension>0?true:false;
//		}
		
		public int getArrayDimension() {
			return arrayDimension;
		}
		
		private Type getType() {
			return type;
		}
		
		/**
		 * Returns the full class name or a description for primitive types
		 * @return
		 */
		private String getTypeDescription() {
			if (typeDescription != null) return typeDescription;
			else return type.toString();
		}
		
		/**
		 * Use this method to manually overwrite the type description.
		 * @param typeDescription
		 */
		private void setTypeDescription(String typeDescription) {
			this.typeDescription = typeDescription;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(type);
			for (int i=0; i<arrayDimension; i++) {
				final String s = "[]";
				sb.append(s);
			}
			return sb.toString();
		}
	}
	
	/**
	 * Pattern to match (negative) hex numbers prefixed with 0x.
	 */
	private final static Pattern HEX_PATTERN = Pattern.compile("^-?0x([0-9]+)L?");  
	
	
	/**
	 * Convert a raw constant to a readable value based on it's type. If this method somehow fails, the original value is returned.
	 * @param type the type of the constant
	 * @param originalValue the original value
	 * @return the converted value, eg, -1234 instead of -0x4d2 for integers
	 */
	public static String parseConstant(Type type, String originalValue) {
		try {
			/*
			 *  short, long and int need to have the 0x removed.
			 *  short and long also need to have the trailing type removed.
			 *  For example, an Integer looks like: .field public static final i:I = -0x4d2 which is -1234.
			 */
			switch (type) {
				case SHORT:
					return ""+Short.parseShort(originalValue.substring(0, originalValue.length()-1).replaceFirst("0x", ""), 16);
				case INTEGER:
					return ""+Integer.parseInt(originalValue.replaceFirst("0x", ""), 16);
				case FLOAT:
					return ""+Float.parseFloat(originalValue);
				case DOUBLE:
					return ""+Double.parseDouble(originalValue);
//				case STRING:
//					/*
//					 * TODO: Automatically decode UTF-8, UTF-16BE etc Strings like \u4e0d\u6b63\u306a\u6587\u5b57\u30b3\u30fc\u30c9
//					 */
//					return originalValue;
				case UNKNOWN: // same as default, mostly ints or longs
				case MATH_OP: // see above
				case LONG:
					/*
					 * This is the LONG case, but also the default case for UNKNOWN types
					 * eg: const-wide v2, 0x7b5bad595e238e38L
					 * but const-wide v2, 0x7b5 is also possible
					 * Negative: const/4 v7, -0x1
					 */
					Matcher m = HEX_PATTERN.matcher(originalValue);
					if (m.matches()) { // we have something as above
						String hexValue = m.group(1);
						boolean isNegative = false;
						if (originalValue.startsWith("-")) {
							isNegative = true;
						}
						return String.format("%s%d", isNegative?"-":"", Long.parseLong(hexValue, 16));
					}
					// else return original value (default case)
				default:
					return originalValue;
			}
		}
		catch (NumberFormatException e) {
			LOGGER.warn("Could not convert value '"+originalValue+"', type="+type+", e="+e.getMessage());
			return originalValue;
		}
	}
	
	/**
	 * The type of the constant as a String, class names will be resolved.
	 * See {@linkplain Type} for more info.
	 * @return
	 */
	public String getTypeDescription() {
		return varType.getTypeDescription();
	}

	public int getArrayDimension() {
		return varType.getArrayDimension();
	}

	/**
	 * The type of the constant. Non-primitive types are not resolved.
	 * See {@linkplain Type} for more info.
	 * @return
	 */
	public Type getType() {
		return varType.getType();
	}
	
	/**
	 * The name of the variable, may be null.
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	
	/**
	 * 
	 * Parse something like this:
	 * 
	 * fill-array-data v0, :array_0
	 * 
	 * ...
	 * ...
	 * 
	 * :array_0 // the label!
	 * .array-data 0x1
	 *  0x78t
	 *  0x79t
	 *  0x7at
	 * .end array-data
	 * 
	 * The array is filled with this instruction. This should be the first instruction for this array after
	 * it was created w/ a new-array opcode. Parse this instruction and end after it.
	 * 
	 * 1) Get the label
	 * 2) Search the label in the method, not the BB (should be at the end)
	 * 3) Read the constants
	 * 
	 * @param label the label
	 * @param method the Method where the fill-array-data opcode was found
	 * @return a string representation of the initial array content
	 * @throws SyntaxException 
	 */
	private static String parseFillArrayDataOpCode(byte[] label, MethodInterface method) throws SyntaxException {
		/*
		 * TODO: int[] iiirrr = { 12, 1234 }; yields the following bytecode, how to parse it?
		 * :array_0	
		 * .array-data 0x4
		 *  0xct 0x0t 0x0t 0x0t
		 *  0xd2t 0x4t 0x0t 0x0t
		 * .end array-data
		 */
		LinkedList<CodeLineInterface> methodCodeLines = method.getCodeLines();
		CodeLineInterface mcl;
		for (int mindex=0; mindex<methodCodeLines.size(); mindex++) {
			mcl = methodCodeLines.get(mindex);
			if (mcl.getInstruction().getType() == InstructionType.LABEL) {
				if (Arrays.equals(label, mcl.getLine())) { // is it our label?
					mindex += 2; // skip the .array-data line, we are now at the first index (0x78t)
					mcl = methodCodeLines.get(mindex);
					StringBuilder sb = new StringBuilder();
					sb.append("[ ");
					while (mcl.getInstruction().getType() != InstructionType.SMALI_DOT_COMMENT) {
						sb.append(new String(mcl.getLine()));
						sb.append(" ");
						mcl = methodCodeLines.get(++mindex);
					}
					sb.append("]");
					return sb.toString();
				}
			}
		}
		throw new SyntaxException("Could not correctly parse the fill-array-data opcode. No label '"+new String(label)+"' found!");
	}
	
	/**
	 * Return the parsed value. May be null if no value was assignet.
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return Codeline Object where the constant came from
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 */
	public CodeLineInterface getCodeLine() {
		return cl;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CONST: type=");
		sb.append(variableType);
		sb.append(", name=");
		sb.append(identifier);
		sb.append(", value=");
		sb.append(value);
		sb.append(", type=");
		sb.append(varType.getTypeDescription());
		sb.append(", arrayDim=");
		sb.append(varType.getArrayDimension());
		sb.append(", fuzzy=");
		sb.append(fuzzyLevel);
		sb.append("\n   path=");
		sb.append(getPath());
		return sb.toString();
	}
	
	/**
	 * A small helper enum. Describes what we are searching for.
	 */
	private enum MetaDataLine {
		LOCAL(".local ".getBytes()),
		RESTART_LOCAL(".restart local ".getBytes());
		
	    private final byte[] text;

	    private MetaDataLine(byte[] text) {
	    	this.text = text;
	    }
	    
	    private byte[] lineStartsWith() {
	    	return text;
	    }
	}
	
	/**
	 * Search for the next .local or .local restart line from a given CodeLine to get more information about
	 * a constant or an array. The search ends at the end of the Method the Codeline belongs to.
	 * @param currentLine the current codeline to begin the search after
	 * @param mdl describes the line which is searched, this is either a .local or a ".restart local" line
	 * @param register this is the register which must occur in the searched line
	 * @return the found codeline or null if we find an opcode before any .local/.restart line
	 * @throws SyntaxException if the next .local line has a register mismatch or the file ended
	 */
	private static CodeLineInterface getNextMetadataLineForConstant(CodeLineInterface currentLine, MetaDataLine mdl, byte[] register) throws SyntaxException {
		CodeLineInterface localLine;
		int localLineNr = currentLine.getLineNr(); // this is already the next line index, line numbers start at 1
		MethodInterface method = currentLine.getMethod();
		
		int lastLineNrInMethod;
		if (method != null ) {
			lastLineNrInMethod = currentLine.getMethod().getCodeLines().getLast().getLineNr();
		}
		else {
			throw new SyntaxException("Cannot search for Metadata outside of a method.");
		}
		while (localLineNr < lastLineNrInMethod) {
			
			localLine = currentLine.getSmaliClass().getAllCodeLines().get(localLineNr);
			if (localLine.isCode()) {
				/*
				 *  We found some other opcode and not a .local line. The constant we are searching information for
				 *  is not assigned to any variable but the compiler just put some value into some register in order
				 *  to use it for, eg, an array initialization as the array size parameter.
				 *  
				 *  This will automatically stop a BB borders, as every jmp, goto etc instructions is considered to be code!
				 */
				return null;
			}

			if (localLine.getInstruction().getType() == InstructionType.SMALI_DOT_COMMENT && localLine.startsWith(mdl.lineStartsWith())) {
				LinkedList<byte[]> split = Instruction.split(localLine.getLine());
				/*
				 * .local v0 name:type
				 * .restart local v0 #name:type
				 */
				int indexOfRegister;
				if (mdl == MetaDataLine.LOCAL) indexOfRegister = 1;
				else indexOfRegister = 2;
				byte[] localRegister = split.get(indexOfRegister);
				if (Arrays.equals(register, localRegister)) {
					return localLine;
				}
				else { // this should not happen, the next .local line should be the correct data for our array!
					// TODO: Is the above assumption always true? At least we should consider the BasicBlocks
					// and not stupidly iterate over all method codelines.
					if (DEBUG) LOGGER.debug( "Found a .local line, but registers do not match! register="+new String(register)+", cl="+localLine);
//					throw new SyntaxException("Found a .local line, but registers do not match! register="+new String(register)+", cl="+localLine);
				}
			}
			localLineNr++; // check the next line
		}
		if (DEBUG) LOGGER.debug("Unable to find a Metadata line, method ended!");
		return null;
	}
	
	/**
	 * A constant is equal to another constant
	 * if both have the same {@linkplain CodeLine}.
	 */
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Constant))
        {
            return false;
        }
        return other == cl;
    }

    /**
	 * A constant is equal to another constant
	 * if both have the same {@linkplain CodeLine}.
	 */
    @Override
    public int hashCode()
    {
        return cl.hashCode();
    }
    
    /**
     * Get the level of fuzziness.
     * @return 0 if the constant was found during a non-fuzzy search. Values higher than 0 indicate more fuzziness the higher they are.
     */
    public int getFuzzyLevel() {
    	return fuzzyLevel;
    }
    
    /**
     * Determines whether the found constant was found during
     * a fuzzy (inaccurate) search
     * @return true if the search was funny (fuzzyLevel > 0)
     */
    public boolean wasFuzzySearch() {
    	return fuzzyLevel==0?false:true;
    }
    
    /**
     * Get the path in which the this constant was found.
     * @return the path, the last entry contains the found constant
     */
    public String getPath() {
    	StringBuffer sb = new StringBuffer();
		for (BasicBlockInterface bb : path) {
			sb.append(bb.getUniqueId());
			if (bb != path.getLast()) sb.append("->");
		}
		return sb.toString();
    }
    
    /**
     * Check whether this constant was found inside an ad framework package path.
     * @return true if it is
     */
    public boolean isInAdFrameworkPackage() {
    	return cl.getSmaliClass().isInAdFrameworkPackage();
    }
    
    /**
     * Get the searchId which all Constants should have in common which were found during one run of the
     * DetectionLogic for one tracked invoke.
     * @return the searchId
     */
    public int getSearchId() {
    	return searchId;
    }
    
    /**
     * Get the type of the found variable or constant.
     * @return the type
     */
    @Override
    public VariableType getVariableType() {
    	return variableType;
    }
}
