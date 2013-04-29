/* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package de.rub.syssec.saaf.misc;

 
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.http.util.ByteArrayBuffer;


/**
 * Byte utilities.
 * 
 * Some methods copied from http://www.java2s.com/Code/Java/Collections-Data-Structure/
 * Doesthisbytearraybeginwithmatcharraycontent.htm
 * 
 */
public class ByteUtils {

	/**
	 * Does this byte array begin with match array content?
	 * 
	 * @param source
	 *            Byte array to examine
	 * @param match
	 *            Byte array to locate in <code>source</code>
	 * @return true If the starting bytes are equal
	 */
	public static boolean startsWith(byte[] source, byte[] match) {
		return startsWith(source, 0, match);
	}
	

	/**
	 * Does this byte array begin with match array content? The check is NOT case sensitive!
	 * 
	 * @param source
	 *            Byte array to examine
	 * @param offset
	 *            An offset into the <code>source</code> array
	 * @param match
	 *            Byte array to locate in <code>source</code>
	 * @return true If the starting bytes are equal
	 */
	public static boolean startsWith(byte[] source, int offset, byte[] match) {

		if (match.length > (source.length - offset)) {
			return false;
		}

		for (int i = 0; i < match.length; i++) {
			if (source[offset + i] != match[i]) {
				// ignore cases
				if (source[offset + i] >= 65 && source[offset + i] <= 90
						&& source[offset + i] + 32 == match[i]) {
					continue;
				} else if (source[offset + i] >= 97
						&& source[offset + i] <= 122
						&& source[offset + i] - 32 == match[i]) {
					continue;
				} else
					return false;
			}
		}
		return true;
	}
	
	
	public static boolean endsWith(byte[] source, char c) {
		return endsWith(source, new byte[] { (byte) c });
	}
	

	/**
	 * Does this byte array end with match array content? The check is NOT case sensitive!
	 */
	public static boolean endsWith(byte[] source, byte[] match) {
		if (match.length > (source.length)) {
			return false;
		}

		for (int i = 0; i < match.length; i++) {
			if (match[i] != source[source.length - match.length + i]) {
				if (match[i] >= 65
						&& match[i] <= 90
						&& match[i] + 32 == source[source.length - match.length
								+ i]) {
					continue;
				} else if (match[i] >= 97
						&& match[i] <= 122
						&& match[i] - 32 == source[source.length - match.length
								+ i]) {
					continue;
				}
				return false;
			}
		}
		return true;
	}

	
	/**
	 * Does the source array equal the match array?
	 * 
	 * @param source
	 *            Byte array to examine
	 * @param offset
	 *            An offset into the <code>source</code> array
	 * @param match
	 *            Byte array to locate in <code>source</code>
	 * @return true If the two arrays are equal
	 */
	public static boolean equals(byte[] source, byte[] match) {

		if (match.length != source.length) {
			return false;
		}
		return startsWith(source, 0, match);
	}
	

	/**
	 * Copies bytes from the source byte array to the destination array
	 * 
	 * @param source
	 *            The source array
	 * @param srcBegin
	 *            Index of the first source byte to copy
	 * @param srcEnd
	 *            Index after the last source byte to copy
	 * @param destination
	 *            The destination array
	 * @param dstBegin
	 *            The starting offset in the destination array
	 */
	public static void getBytes(byte[] source, int srcBegin, int srcEnd,
			byte[] destination, int dstBegin) {
		System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd
				- srcBegin);
	}

	
	/**
	 * Return a new byte array containing a sub-portion of the source array
	 * 
	 * @param srcBegin
	 *            The beginning index (inclusive)
	 * @param srcEnd
	 *            The ending index (exclusive)
	 * @return The new, populated byte array
	 */
	public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
		byte destination[];

		destination = new byte[srcEnd - srcBegin];
		getBytes(source, srcBegin, srcEnd, destination, 0);

		return destination;
	}

	
	/**
	 * Return a new byte array containing a sub-portion of the source array
	 * 
	 * @param srcBegin
	 *            The beginning index (inclusive)
	 * @return The new, populated byte array
	 */
	public static byte[] subbytes(byte[] source, int srcBegin) {
		return subbytes(source, srcBegin, source.length);
	}


	/**
	 * Return the array as a printable string.
	 * 
	 * @param buffer
	 * @param breakLines Insert \n in returned String if CRLF was found
	 * @return
	 */
	public static String dumpArray(byte[] buffer, boolean breakLines) {
		StringBuilder sb = new StringBuilder();
		boolean lastOneIsR = false;
		for (byte b : buffer) {
			if (b >= 32 && b <= 126)
				sb.append(new String(new byte[] { b }));
			else if (b == 10) {
				sb.append("\\n");
				if (lastOneIsR) {
					if (breakLines)
						sb.append("\n");
					lastOneIsR = false;
				}
			} else if (b == 13) {
				sb.append("\\r");
				lastOneIsR = true;
			} else if (b == 0)
				sb.append("\\0");
			else
				sb.append("?");
		}
		return sb.toString();
	}
	
	
	/**
	 * Read a line from an inputstream into a byte buffer. A line might end with a LF or an CRLF. CR's are accepted inside a line and
	 * are not understood as a beginning new line. This should work therefore on Mac OS X, Unix, Linux and Windows.
	 * 
	 * See http://en.wikipedia.org/wiki/Newline for more.
	 *  
	 * @param in the inputstream
	 * @param maxSize the maximum amount of bytes to read until a CRLF or LF is reached, a value of zero or smaller disables a limit (use w/ care!)
	 * @return the buffer where read bytes are appended, this buffer will not contain any CR's or or CRLF's at the end of the array. Null is
	 * returned if EOF is reached.
	 * @throws IOException if something is wrong w/ the stream or the maxSize is reached
	 */
	public static byte[] parseLine(BufferedInputStream in, int maxSize) throws IOException {
		ByteArrayBuffer bab = new ByteArrayBuffer(512);
		int b;
		while (true) {
			if (!(maxSize <= 0 || bab.length() <= maxSize)) {
				throw new IOException("Maximal bytearraybuffer size of "+maxSize+" exceeded!");
			}
			b = in.read();
			if (b == -1) {
				if (bab.isEmpty()) {
					// we have nothing read yet and could nothing read, we will therefore return 'null' as this
					// indicates EOF.
					return null;
				}
				else {
					// return what we got so far
					return bab.toByteArray();
				}
			}
			// CRLF case
			if (b == '\r') { // check if we find a \n
				int next = in.read();
				if (b == -1) {
					// EOF; return what we got
					return bab.toByteArray();
				}
				else if (next == '\n') { // we did
					in.mark(-1); // rest mark
					return bab.toByteArray(); // return the line without CRLF
				} else {
					// found no CRLF but only a CR and some other byte, so we need to add both to the buffer and proceed
					bab.append('\r');
					bab.append(b);
				}
			}
			// LF case
			else if (b == '\n') { // we found a LF and therefore the end of a line
				return bab.toByteArray();
			}
			else { // we just found a byte which is happily appended
				bab.append(b);
			}
		}
	}
	
	
	
	/**
	 * http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in
	 * -java
	 * 
	 * @param <T>
	 * @param first
	 * @param rest
	 * @return
	 */
	public static byte[] concatAll(byte[] first, byte[]... rest) {
		int totalLength = first.length;
		for (byte[] array : rest) {
			totalLength += array.length;
		}
		byte[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (byte[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	
	
	/**
	 * Checks if pattern is contained in source. This is just a wrapped KMP indexOf().
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static boolean contains(byte[] source, byte[] pattern) {
		return KMP.indexOf(source, pattern) < 0 ? false : true; 
	}
	
	/**
	 * Checks if pattern is contained in source. This is just a wrapped KMP indexOf().
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static boolean contains(byte[] source, char c) {
		return KMP.indexOf(source, new byte[] {(byte)c}) < 0 ? false : true; 
	}
	
	public static int indexOf(byte[] source, char c) {
		return indexOf(source, c, 0);
	}
	
	
	public static int indexOf(byte[] source, char c, int offset) {
		for (int i = offset; i < source.length; i++) {
			if (source[i] == c) return i; 
		}
		return -1;
	}
	
	/**
	 * Searches forwards
	 * @param source
	 * @param c
	 * @return
	 */
	public static int indexOfReverse(byte[] source, char c) {
		return indexOfReverse(source, c, source.length-1);
	}
	
	/**
	 * Searches backwards
	 */
	public static int indexOfReverse(byte[] source, char c, int offset) {
		for (int i = offset; i >= 0; i--) {
			if (source[i] == c) return i; 
		}
		return -1;
	}
	
	
	/**
	 * Get the first part of the array until a chosen char is found.
	 * @param source
	 * @param c the char to split at
	 * @param offset the offest of source
	 * @return
	 */
	public static byte[] splitAtFirstOccurence(byte[] source, char c, int offset) {
		int index = indexOf(source, c, offset);
		if (index < 0) return source;
		else {
			return subbytes(source, 0, index);
		}
	}
	
	/**
	 * Get the last part of the array until a chosen char is found, the char is searched from the end to the beginning of source.
	 * @param source
	 * @param c
	 * @param offset the offest of source, from the beginning, NOT the end.
	 * @return
	 */
	public static byte[] splitAtLastOccurence(byte[] source, char c, int offset) {
		int index = indexOfReverse(source, c, offset);
		if (index < 0) return source;
		else {
			return subbytes(source, index, source.length);
		}
	}
}