/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.segmentation.slic;

import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSegmentSlic {

	@Test
	public void initializeClusters() {

		DummySlic alg = new DummySlic(4,1,10,1) {
			@Override
			protected void perturbCenter(Cluster c, int x, int y) {
				c.x = x; c.y = y;
			}
		};

		alg.input = new ImageUInt8(100,122);
		alg.gridInterval = 10;
		alg.clusters.resize(120);

		alg.initializeClusters();

		assertEquals(2+3,alg.clusters.get(0).x,1e-4);
		assertEquals(2+4,alg.clusters.get(0).y,1e-4);

		assertEquals(2+13,alg.clusters.get(1).x,1e-4);
		assertEquals(2+4,alg.clusters.get(1).y,1e-4);

		assertEquals(95,alg.clusters.get(9).x,1e-4);
		assertEquals(2+4,alg.clusters.get(9).y,1e-4);

		assertEquals(2+3,alg.clusters.get(10).x,1e-4);
		assertEquals(2+14,alg.clusters.get(10).y,1e-4);
	}

	/**
	 * Makes sure bounds checking is properly done.  places a cluster at each corner and sees if the pixels around
	 * the corner has that specific cluster
	 */
	@Test
	public void computeClusterDistance() {
		DummySlic alg = new DummySlic(4,1,10,1);

		ImageUInt8 input = new ImageUInt8(7,9);
		alg.initalize(input);

		SegmentSlic.Cluster c0 = alg.clusters.grow();
		SegmentSlic.Cluster c1 = alg.clusters.grow();
		SegmentSlic.Cluster c2 = alg.clusters.grow();
		SegmentSlic.Cluster c3 = alg.clusters.grow();

		c0.x = 0; c0.y = 0;
		c1.x = 6; c1.y = 0;
		c2.x = 6; c2.y = 8;
		c3.x = 0; c3.y = 8;

		alg.gridInterval = 2;
		alg.computeClusterDistance();

		checkPixelContains(0,2,0,2,c0,alg);
		checkPixelContains(5,7,0,2,c1,alg);
		checkPixelContains(5,7,7,9,c2,alg);
		checkPixelContains(0,2,7,9,c3,alg);
	}

	private void checkPixelContains( int x0 , int x1 , int y0 , int y1 ,
									 SegmentSlic.Cluster c ,
									 DummySlic alg ) {

		for( int y = y0; y < y1; y++ ) {
			for( int x = x0; x < x1; x++ ) {
				int index = y*alg.input.width + x;
				SegmentSlic.Pixel p = alg.pixels.get(index);

				boolean contains = false;
				for( int i = 0; i < p.total; i++ ) {
					if( p.clusters[i].cluster == c )
						contains = true;
				}
				assertTrue(contains);
			}
		}
	}

	@Test
	public void updateClusters() {
		DummySlic alg = new DummySlic(4,1,10,1);

		SegmentSlic.Cluster c0 = alg.clusters.grow();
		SegmentSlic.Cluster c1 = alg.clusters.grow();
		SegmentSlic.Cluster c2 = alg.clusters.grow();

		alg.pixels.resize(6);
		alg.pixels.get(0).add(c0,2); // 0.666666
		alg.pixels.get(0).add(c1,4); // 0.333333

		alg.pixels.get(1).add(c1,1); // 0.75
		alg.pixels.get(1).add(c0,3); // 0.25

		for( int i = 2; i < 6; i++ ) {
			alg.pixels.get(i).add(c2,0.2f);
		}

		alg.input = new ImageUInt8(2,3);
		alg.updateClusters();

		double w = (2.0/3.0) + 0.25;
		assertEquals((0*0.666 + 1*0.25)/w,c0.x,1e-4);
		assertEquals((0*0.666 + 0*0.25)/w,c0.y,1e-4);

		w = (1.0/3.0) + 0.75;
		assertEquals((0*0.333 + 1*0.75)/w,c1.x,1e-4);
		assertEquals((0*0.333 + 0*0.75)/w,c1.y,1e-4);

		assertEquals(0.5,c2.x,1e-4);
		assertEquals(1.5,c2.y,1e-4);
	}

	@Test
	public void assignLabelsToPixels() {
		DummySlic alg = new DummySlic(4,1,10,1);

		SegmentSlic.Cluster c0 = alg.clusters.grow();
		SegmentSlic.Cluster c1 = alg.clusters.grow();
		SegmentSlic.Cluster c2 = alg.clusters.grow();
		c0.id = 0; c1.id = 1; c2.id = 2;

		alg.pixels.resize(6);
		alg.pixels.get(0).add(c0,2);
		alg.pixels.get(0).add(c1,4);
		alg.pixels.get(0).add(c2,0.1f);

		alg.pixels.get(1).add(c1,1);
		alg.pixels.get(1).add(c0,2);

		for( int i = 2; i < 6; i++ ) {
			alg.pixels.get(i).add(c1,0);
			alg.pixels.get(i).add(c2,0.2f);
		}

		ImageSInt32 image = new ImageSInt32(2,3);
		alg.assignLabelsToPixels(image);

		assertEquals(2,image.get(0,0));
		assertEquals(1,image.get(1,0));
		for( int i = 2; i < 6; i++ ) {
			assertEquals(1,image.data[i]);
		}

	}

	@Test
	public void Pixel_add()
	{
		SegmentSlic.Cluster c0 = new SegmentSlic.Cluster();
		SegmentSlic.Cluster c1 = new SegmentSlic.Cluster();

		SegmentSlic.Pixel p = new SegmentSlic.Pixel();

		assertEquals(0,p.total);

		p.add(c0, 2.2f);
		assertEquals(1, p.total);
		assertEquals(2.2f, p.clusters[0].distance, 1e-4f);
		assertTrue(c0 == p.clusters[0].cluster);

		p.add(c1, 1.2f);
		assertEquals(2, p.total);
		assertEquals(1.2f, p.clusters[1].distance, 1e-4f);
		assertTrue(c1 == p.clusters[1].cluster);

		// should get capped and not go forever
		for( int i = 0; i < 50; i++ ) {
			p.add(c0,0.5f);
		}
		assertEquals(10, p.total);
	}


	@Test
	public void Pixel_computeWeights() {
		SegmentSlic.Pixel p = new SegmentSlic.Pixel();

		p.total = 2;
		p.clusters[0].distance = 2;
		p.clusters[1].distance = 0.3f;

		p.computeWeights();

		assertEquals(1.0f - 2f/2.3f,p.clusters[0].distance,1e-4f);
		assertEquals(1.0f - 0.3f/2.3f,p.clusters[1].distance,1e-4f);

		// check special case of 1 item.  The weight will be 1 since it is the only one
		p.total = 1;
		p.clusters[0].distance = 2;

		p.computeWeights();

		assertEquals(1.0f,p.clusters[0].distance,1e-4f);
	}

	@Test
	public void Cluster_update() {
		SegmentSlic.Cluster c = new SegmentSlic.Cluster();

		float x = 6.1f;
		float y = 7.5f;

		c.totalWeight = 10.05f;
		c.x = x*c.totalWeight;
		c.y = y*c.totalWeight;

		c.color = new float[]{1.2f,56.8f};
		c.color[0] *= c.totalWeight;
		c.color[1] *= c.totalWeight;

		c.update();

		assertEquals(x,c.x,1e-4f);
		assertEquals(y,c.y,1e-4f);
		assertEquals(1.2f,c.color[0],1e-4f);
		assertEquals(56.8f,c.color[1],1e-4f);
	}

	public static class DummySlic extends SegmentSlic<ImageUInt8> {

		public DummySlic(int numberOfRegions, float m, int totalIterations, int numBands) {
			super(numberOfRegions, m, totalIterations, numBands);
		}

		@Override
		public void setColor(Cluster c, int x, int y) {
		}

		@Override
		public void addColor(Cluster c, int index, float weight) {
		}

		@Override
		public float colorDistance(float[] color, int index) {
			return 0;
		}

		@Override
		public float getIntensity(int x, int y) {
			return 0;
		}
	}

}
