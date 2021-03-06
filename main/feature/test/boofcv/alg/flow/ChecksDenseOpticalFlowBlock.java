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

package boofcv.alg.flow;

import boofcv.alg.misc.GImageMiscOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.ImageSingleBand;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests for implementations of {@link DenseOpticalFlowBlock}
 *
 * @author Peter Abeles
 */
public abstract class ChecksDenseOpticalFlowBlock<T extends ImageSingleBand> {

	Class<T> imageType;
	Random rand = new Random(234);
	T image;

	protected ChecksDenseOpticalFlowBlock(Class<T> imageType) {
		this.imageType = imageType;

		image = GeneralizedImageOps.createSingleBand(imageType,40,50);
	}

	public abstract DenseOpticalFlowBlock<T> createAlg( int searchRadius, int regionRadius, int maxPerPixelError);

	@Test
	public void extractTemplate() {

		int r = 2;
		DenseOpticalFlowBlock<T> alg = createAlg(1,r,10);

		GImageMiscOps.fillUniform(image,rand,0,200);
		alg.extractTemplate(3,4,image);

		for( int i = -r; i <= r; i++ ) {
			for( int j = -r; j <= r; j++ ) {
				int x = j+3, y = i+4;

				double expected = GeneralizedImageOps.get(image,x,y);
				double found = GeneralizedImageOps.get(alg.template,j+r,i+r);

				assertEquals(expected,found,1e-8);
			}
		}
	}

	@Test
	public void computeError() {
		int r = 2;
		int w = r*2+1;
		DenseOpticalFlowBlock<T> alg = createAlg(1,r,10);

		GImageMiscOps.fillUniform(image,rand,0,200);
		GImageMiscOps.fillUniform(alg.template,rand,0,200);

		float found = alg.computeError(5,6,image);

		float expected = 0;
		for( int i = -r; i <= r; i++ ) {
			for( int j = -r; j <= r; j++ ) {
				int x = j+5, y = i+6;

				double v0 = GeneralizedImageOps.get(image,x,y);
				double v1 = GeneralizedImageOps.get(alg.template,j+r,i+r);

				expected += Math.abs(v0-v1);
			}
		}

		assertEquals(expected,found,1e-5);
	}
}
