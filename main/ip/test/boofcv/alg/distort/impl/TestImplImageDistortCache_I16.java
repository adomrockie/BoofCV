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

package boofcv.alg.distort.impl;

import boofcv.alg.distort.ImageDistortCache;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.core.image.border.ImageBorder;
import boofcv.struct.image.ImageSInt16;

/**
 * @author Peter Abeles
 */
public class TestImplImageDistortCache_I16 extends CommonImageDistortCacheTests<ImageSInt16> {

	public TestImplImageDistortCache_I16() {
		super(ImageSInt16.class);
	}

	@Override
	public ImageDistortCache<ImageSInt16> create(InterpolatePixelS<ImageSInt16> interp,
												ImageBorder<ImageSInt16> border,
												Class<ImageSInt16> imageType) {
		return new ImplImageDistortCache_I16<ImageSInt16>(interp,border);
	}
}
