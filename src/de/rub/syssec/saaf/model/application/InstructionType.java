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
package de.rub.syssec.saaf.model.application;

/**
 * Denotes the type of a SMALI instruction.
 */
public enum InstructionType {
	NEW_INSTANCE,
	JMP,
	INVOKE,
	INVOKE_STATIC,
	PUT,
	APUT,
	MATH_1, // unary operations or binary operations w/ only 1 target and 1 source reg
	MATH_2, // binary operations w/ 1 target and 2 sources
	MATH_2C, // binary operations w/ 1 target, 1 source and 1 constant instead of register
	CONST,
	GET,
	AGET,
	MOVE,
	MOVE_RESULT,
	IGNORE,
	INTERNAL_SMALI_OPCODE, // opcodes which could write to a backtracked register, but in an unusual way, eg move-exception or array-length
	NOP,
	GOTO,
	SWITCH,
	UNKNOWN,
	RETURN,
	NEW_ARRAY,
	FILL_ARRAY_DATA,
	FILLED_NEW_ARRAY,
	LABEL,
	EMPTY_LINE,
	SMALI_DOT_COMMENT,
	SMALI_HASH_KEY_COMMENT, // Line starts with a '#'
	NOT_YET_PARSED; // denotes if the instruction of a codeline has not yet been parsed
}