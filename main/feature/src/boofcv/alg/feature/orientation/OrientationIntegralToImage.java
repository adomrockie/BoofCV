/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.feature.orientation;

import boofcv.alg.transform.ii.GIntegralImageOps;
import boofcv.struct.image.ImageSingleBand;

/**
 * Converts an {@link OrientationIntegral} into {@link OrientationImage}.
 *
 * @author Peter Abeles
 */
public class OrientationIntegralToImage<T extends ImageSingleBand, II extends ImageSingleBand>
	implements OrientationImage<T>
{
	private Class<T> imageType;
	private OrientationIntegral<II> alg;

	// converted integral image of input image
	private II ii;

	public OrientationIntegralToImage(OrientationIntegral<II> alg, Class<T> imageType) {
		this.alg = alg;
		this.imageType = imageType;
	}

	@Override
	public void setImage(T image) {
		if( ii != null ) {
			ii.reshape(image.width,image.height);
		}

		// compute integral image
		ii = GIntegralImageOps.transform(image, ii);
		alg.setImage(ii);
	}

	@Override
	public Class<T> getImageType() {
		return imageType;
	}

	@Override
	public void setScale(double scale) {
		alg.setScale(scale);
	}

	@Override
	public double compute(double c_x, double c_y) {
		return alg.compute(c_x,c_y);
	}
}
