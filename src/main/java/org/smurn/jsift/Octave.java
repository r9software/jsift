/*
 * Copyright 2011 Stefan C. Mueller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smurn.jsift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a scale interval in the scale-space where the scale doubles.
 * <p>All images in an octave have the same width and height.</p>
 */
public final class Octave {

    private final double baseScale;
    private final List<Image> scaleImages;
    private final List<Image> doGs;

    /**
     * Creates an instance.
     * @param baseScale Scale of the scale-image with the lowest scale in this octave.
     * @param scaleImages Scale-images.
     * @param doGs Difference-of-gaussian images.
     * @throws NullPointerException if one of the parameters is {@code null}.
     * @throws IllegalArgumentException if there is not at least four scale
     * images, if the number of Difference-of-gaussian images is not one less
     * than the number of scale-images, if not all images are of equal width and height
     * or if {@code baseScale} is not strictly positive.
     */
    public Octave(final double baseScale, final List<Image> scaleImages,
            final List<Image> doGs) {
        if (scaleImages == null) {
            throw new NullPointerException("scaleImages must not be null");
        }
        if (doGs == null) {
            throw new NullPointerException("scaleImages must not be null");
        }
        if (baseScale <= 0.0) {
            throw new IllegalArgumentException(
                    "baseScale must be greater than 0");
        }
        if (scaleImages.size() < 4) {
            throw new IllegalArgumentException(
                    "Need at least three scale-images.");
        }
        if (doGs.size() != scaleImages.size() - 1) {
            throw new IllegalArgumentException(
                    "Need exactly one DoG image less than scale-images");
        }
        int width = scaleImages.get(0).getWidth();
        int height = scaleImages.get(0).getHeight();
        
        for(int i =0; i < scaleImages.size();i++){
            Image image = scaleImages.get(i);
            if (image.getWidth() != width || image.getHeight() != height){
                throw new IllegalArgumentException("scale-image " + i + 
                        " has a different size than the first scale-image");
            }
        }
        
        for(int i =0; i < doGs.size();i++){
            Image image = doGs.get(i);
            if (image.getWidth() != width || image.getHeight() != height){
                throw new IllegalArgumentException("DoG image " + i + 
                        " has a different size than the first scale-image");
            }
        }
        
        this.baseScale = baseScale;
        this.scaleImages = Collections.unmodifiableList(
                new ArrayList<Image>(scaleImages));
        this.doGs = Collections.unmodifiableList(new ArrayList<Image>(doGs));
    }

    /**
     * Scale of the scale-image with the lowest scale in this octave.
     * @return Base scale of this octave.
     */
    public double getBaseScale() {
        return baseScale;
    }

    /**
     * Gets the number of scales per octave. 
     */
    public int getScalesPerOctave() {
        return scaleImages.size() - 3;
    }

    /**
     * Gets the scale-image at the scales of this octave.
     * Each image's scale is {@code b*2^(i/s)} where
     * {@code s=getScalesPerOctave()}, {@code b=getBaseScale()}.
     * @return Immutable list with {@code getScalesPerOctave()+3} images.
     */
    public List<Image> getScaleImages() {
        return scaleImages;
    }

    /**
     * Gets the difference-of-gaussian images of this octave.
     * The image with index {@code i} is the difference from the scale-image
     * with index {@code i+1} and {@code i}.
     * Each image's scale is {@code b*2^((i+0.5)/s)} where
     * {@code s=getScalesPerOctave()}, {@code b=getBaseScale()}.
     * @return Immutable list with {@code getScalesPerOctave()+2} images.
     */
    public List<Image> getDifferenceOfGaussians() {
        return doGs;
    }
}