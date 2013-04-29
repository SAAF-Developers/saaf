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
package de.rub.syssec.saaf.misc;

/**
 * The Knuth-Morris-Pratt Pattern Matching Algorithm for byte arrays.
 * Adapted from http://helpdesk.objects.com.au/java/search-a-byte-array-for-a-byte-sequence
 */
public class KMP {

	/**
	 * Search the data byte array for the first occurrence of the byte array
	 * pattern.
	 */
	public static int indexOf(byte[] data, byte[] pattern) {
		return indexOf(data, 0, pattern);
	}

	/**
	 * Search the data byte array for the first occurrence of the byte array
	 * pattern beginning at offset. Use '*' as a wildcard for any amount of
	 * arbitrary bytes. A trailing wildcard will be ignored.
	 */
	public static int indexOf(byte[] data, int offset, byte[] pattern,
			boolean ignoreCase, boolean wildcard) {
		if (offset > data.length)
			return -1;
		int[] failure = computeFailure(pattern);

		int j = 0;

		for (int i = 0 + offset; i < data.length; i++) {
			if (wildcard && pattern[j] == '*') {
				/* Skip the wildcard */
				j++;

				/*
				 * If the wildcard was at the end of the pattern, data and
				 * pattern match and * can be ignored
				 */
				if (j == pattern.length)
					return i - pattern.length;

				/*
				 * Go through the data and skip everything which is not
				 * pattern[j]
				 */
				while (i < data.length
						&& (!ignoreCase && pattern[j] != data[i] || ignoreCase
								&& pattern[j] != data[i]
								&& pattern[j] >= 'A'
								&& pattern[j] <= 'Z'
								&& pattern[j] + 32 != data[i]))
					i++;

				/*
				 * The pattern[j] character wasn't found anywhere in the data so
				 * there is no match
				 */
				if (i == data.length)
					return -1;
			}

			while (j > 0
					&& (!ignoreCase && pattern[j] != data[i] || ignoreCase
							&& pattern[j] != data[i] && pattern[j] >= 'A'
							&& pattern[j] <= 'Z' && pattern[j] + 32 != data[i])) {
				j = failure[j - 1];
			}

			if (pattern[j] == data[i]
					|| (ignoreCase && pattern[j] >= 'A' && pattern[j] <= 'Z' && pattern[j] + 32 == data[i])) {
				j++;
			}

			if (j == pattern.length) {
				return i - pattern.length + 1;
			}
		}
		return -1;
	}

	public static int indexOf(byte[] data, int offset, byte[] pattern) {
		return indexOf(data, offset, pattern, false, false);
	}

	/**
	 * Computes the failure function using a boot-strapping process, where the
	 * pattern is matched against itself.
	 */
	private static int[] computeFailure(byte[] pattern) {
		int[] failure = new int[pattern.length];

		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			while (j > 0 && pattern[j] != pattern[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}

		return failure;
	}
}