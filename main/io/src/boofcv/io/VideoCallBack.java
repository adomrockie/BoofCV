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

package boofcv.io;

import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

/**
 * Simplified interface for processing frames in a video sequence.
 *
 * @author Peter Abeles
 */
public interface VideoCallBack<T extends ImageBase> {

	/**
	 * Specifies the size of the video feed.
	 *
	 * @param width Image width.
	 * @param height Image height;
	 */
	public void init( int width , int height );

	/**
	 * Passes in the next frame in the sequence.  Time in this function should be minimized to avoid causing a
	 * back log in the video image buffer.
	 *
	 * @param frame New image frame in BoofCV image format.
	 * @param sourceData Platform specific image data.
	 * @param timeStamp Time the video frame was collected.
	 */
	public <O> void nextFrame( T frame , Object sourceData , long timeStamp );

	/**
	 * Called when the video stream has stopped.
	 */
	public void stopped();

	/**
	 * Used to inform the video stream if a request has been made to stop processing the video sequence.
	 * This function is checked after each call to {@link #nextFrame(boofcv.struct.image.ImageBase,Object, long)}.
	 *
	 * @return true if a request has been made to stop the steam
	 */
	public boolean stopRequested();

	/**
	 * Specifies the image data structure that's being processed.
	 *
	 * @return
	 */
	public ImageType<T> getImageDataType();

}
