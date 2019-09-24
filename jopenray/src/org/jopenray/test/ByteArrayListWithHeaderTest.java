/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jopenray.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.jopenray.util.ByteArrayListWithHeader;
import org.junit.Test;

import sun.misc.HexDumpEncoder;

public class ByteArrayListWithHeaderTest {

	@Test
	public void testByteArrayList() {
		ByteArrayListWithHeader l = new ByteArrayListWithHeader(5, 0);
		assertTrue(l.getLength() == 0);

		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(5, 2);
		assertTrue(l2.getLength() == 2);
	}

	@Test
	public void testAddInt8() {
		ByteArrayListWithHeader l = new ByteArrayListWithHeader(4, 0);
		for (int i = 0; i < 10; i++) {
			assertTrue(l.getLength() == i);
			l.addInt8(10);
		}
		assertArrayEquals(
				new byte[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 }, l
						.getBytes());

		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(5, 2);
		for (int i = 0; i < 10; i++) {
			l2.addInt8(10);
		}
		assertArrayEquals(new byte[] { 0, 0, 10, 10, 10, 0, 0, 10, 10, 10, 0,
				0, 10, 10, 10, 0, 0, 10 }, l2.getBytes());

	}

	@Test
	public void testAddInt16() {
		ByteArrayListWithHeader l = new ByteArrayListWithHeader(4, 0);
		for (int i = 0; i < 10; i++) {
			assertTrue(l.getLength() == i * 2);
			l.addInt16(0x0A14);// 0x0A (10), 0x14 (20)
		}

		assertArrayEquals(new byte[] { 10, 20, 10, 20, 10, 20, 10, 20, 10, 20,
				10, 20, 10, 20, 10, 20, 10, 20, 10, 20 }, l.getBytes());

		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(5, 2);
		for (int i = 0; i < 10; i++) {
			l2.addInt16(0x0A14);
		}

		assertArrayEquals(new byte[] { 0, 0, 10, 20, 10, 0, 0, 20, 10, 20, 0,
				0, 10, 20, 10, 0, 0, 20, 10, 20, 0, 0, 10, 20, 10, 0, 0, 20,
				10, 20, 0, 0, 10, 20 }, l2.getBytes());
	}

	@Test
	public void testAddBytes() {
		byte[] a = new byte[] { 10, 20 };
		ByteArrayListWithHeader l = new ByteArrayListWithHeader(4, 0);
		for (int i = 0; i < 10; i++) {
			assertTrue(l.getLength() == i * 2);
			l.addBytes(a);
		}

		assertArrayEquals(new byte[] { 10, 20, 10, 20, 10, 20, 10, 20, 10, 20,
				10, 20, 10, 20, 10, 20, 10, 20, 10, 20 }, l.getBytes());

		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(5, 2);
		for (int i = 0; i < 10; i++) {
			l2.addBytes(a);
		}

		assertArrayEquals(new byte[] { 0, 0, 10, 20, 10, 0, 0, 20, 10, 20, 0,
				0, 10, 20, 10, 0, 0, 20, 10, 20, 0, 0, 10, 20, 10, 0, 0, 20,
				10, 20, 0, 0, 10, 20 }, l2.getBytes());

	}

	@Test
	public void testGetLength() {
		ByteArrayListWithHeader l = new ByteArrayListWithHeader(4, 0);
		assertTrue(l.getLength() == 0);
		for (int i = 0; i < 10; i++) {
			assertTrue(l.getLength() == i);
			l.addInt8(10);
		}
		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(5, 2);
		assertTrue(l2.getLength() == 2);
		l2.addInt8(0);
		assertTrue(l2.getLength() == 3);
	}

	@Test
	public void testGetBytes() {
		byte[] a = new byte[] { 10, 20 };
		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(1, 0);
		assertArrayEquals(new byte[] {}, l2.getBytes());
		l2.addBytes(a);
		assertArrayEquals(new byte[] { 10, 20 }, l2.getBytes());

	}

	@Test
	public void testSetInt16() {
		byte[] a = new byte[] { 10, 20 };
		ByteArrayListWithHeader l2 = new ByteArrayListWithHeader(5, 2);
		for (int i = 0; i < 10; i++) {
			l2.addBytes(a);
		}
		for (int i = 0; i < 10; i++) {
			l2.setInt16(2 * i, i);
		}
		HexDumpEncoder d = new HexDumpEncoder();
		System.out.println(d.encode(l2.getInnerByteBuffer()));

		assertArrayEquals(new byte[] { 0, 0, 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0,
				6, 0, 7, 0, 8, 0, 9, 0, 0, 10, 20, 10, 0, 0, 20, 10, 20, 0, 0,
				10, 20 }, l2.getBytes());
	}

}
