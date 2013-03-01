/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.filter.binary;

import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.FastQueue;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.point.Point2D_I32;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestContourTracer {

	FastQueue<Point2D_I32> queue = new FastQueue<Point2D_I32>(Point2D_I32.class,true);
	List<Point2D_I32> found = new ArrayList<Point2D_I32>();

	@Before
	public void init() {
		queue.reset();
		found.clear();
	}


	@Test
	public void single() {
		ImageUInt8 pattern = new ImageUInt8(1,1);
		ImageMiscOps.fill(pattern,1);

		shiftContourCheck(pattern,1);
	}

	@Test
	public void two_horizontal() {
		ImageUInt8 pattern = new ImageUInt8(2,1);
		ImageMiscOps.fill(pattern,1);

		shiftContourCheck(pattern,2);
	}

	@Test
	public void two_vertical() {
		ImageUInt8 pattern = new ImageUInt8(1,2);
		ImageMiscOps.fill(pattern,1);

		shiftContourCheck(pattern,2);
	}

	@Test
	public void square() {
		ImageUInt8 pattern = new ImageUInt8(2,2);
		ImageMiscOps.fill(pattern,1);

		shiftContourCheck(pattern,4);
	}

	@Test
	public void funky1() {
		String s =
				"10\n"+
				"01\n"+
				"10\n";

		ImageUInt8 pattern = stringToImage(s);

		shiftContourCheck(pattern,4);
	}

	@Test
	public void funky2() {
		String s =
				"1000\n"+
				"0110\n"+
				"1001\n";

		ImageUInt8 pattern = stringToImage(s);

		shiftContourCheck(pattern,8);
	}

	@Test
	public void funky3() {
		String s =
				"0100\n"+
				"0110\n"+
				"1101\n";

		ImageUInt8 input = stringToImage(s);
		ImageSInt32 label = new ImageSInt32(input.width,input.height);

		ContourTracer alg = new ContourTracer();

		// process the image
		alg.setInputs(addBorder(input),label,queue);
		alg.trace(2,1+1,0+1,7,found);

		assertEquals(7,queue.size);
		assertEquals(7, found.size());
	}

	@Test
	public void interior1() {
		String s =
				"01110\n"+
				"01101\n"+
				"11110\n";

		ImageUInt8 input = stringToImage(s);
		ImageSInt32 label = new ImageSInt32(input.width,input.height);

		ContourTracer alg = new ContourTracer();

		// process the image
		alg.setInputs(addBorder(input),label,queue);
		alg.trace(2,3+1,0+1,3,found);

		assertEquals(4, queue.size);
		assertEquals(4,found.size());
	}

	/**
	 * Make sure it is marking surrounding white pixels
	 */
	@Test
	public void checkMarkWhite() {
		fail("Implement");
	}

	/**
	 * See if the contour has the expected ordering properties
	 */
	@Test
	public void checkContourOrdering() {
		fail("Implement");
	}

	/**
	 * Given a pattern that is only a contour, it sees if it has the expected results when the pattern
	 * is shifted to every possible location in the image
	 */
	public void shiftContourCheck( ImageUInt8 pattern , int expectedSize ) {
		ContourTracer alg = new ContourTracer();
		ImageUInt8 input = new ImageUInt8(4,5);
		ImageSInt32 label = new ImageSInt32(input.width,input.height);

		// exhaustively try all initial locations
		for( int y = 0; y < input.height-pattern.height+1; y++ ) {
			for( int x = 0; x < input.width-pattern.width+1; x++ ) {
				// paste the pattern in to the larger image
				ImageMiscOps.fill(input,0);
				ImageUInt8 sub = input.subimage(x,y,x+pattern.width,y+pattern.height);
				sub.setTo(pattern);

				// reset other data structures
				ImageMiscOps.fill(label,0);
				queue.reset();
				found.clear();

				// process the image
				alg.setInputs(addBorder(input),label,queue);
				alg.trace(2,x+1,y+1,7,found);

				// forward then back
				assertEquals(expectedSize,queue.size);
				assertEquals(expectedSize,found.size());

				// see if the image has been correctly labeled
				for( int yy = 0; yy < input.height; yy++ ) {
					for( int xx = 0; xx < input.width; xx++ ) {
						boolean isOne = false;
						if( pattern.isInBounds(xx-x,yy-y) ) {
							isOne = pattern.get(xx-x,yy-y) == 1;
						}
						if( isOne )
							assertEquals(2,label.get(xx,yy));
						else
							assertEquals(0,label.get(xx,yy));
					}
				}
			}
		}
	}

	private ImageUInt8 addBorder( ImageUInt8 original ) {
		ImageUInt8 border = new ImageUInt8(original.width+2,original.height+2);
		border.subimage(1,1,border.width-1,border.height-1).setTo(original);
		ImageMiscOps.fillBorder(border,0,1);
		return border;
	}

	private ImageUInt8 stringToImage( String s ) {
		int numCols = s.indexOf('\n');
		int numRows = s.length()/(numCols+1);

		ImageUInt8 out = new ImageUInt8(numCols,numRows);
		for( int y = 0; y < numRows; y++ ) {
			for( int x = 0; x < numCols; x++ ) {
				out.set(x,y, Integer.parseInt(""+s.charAt(y*(numCols+1)+x)));
			}
		}

		return out;
	}
}
