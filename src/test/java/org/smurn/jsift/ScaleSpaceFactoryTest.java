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

import java.util.HashMap;
import java.util.Map;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScaleSpaceFactory}.
 */
public class ScaleSpaceFactoryTest {

    ScaleSpaceFactory target;
    DownScaler downScaler;
    UpScaler upScaler;
    LowPassFilter filter;
    OctaveFactory octaveFactory;
    Map<Image, Double> imageSigma;

    private double getSigma(Image image) {
        if (imageSigma.containsKey(image)) {
            return imageSigma.get(image);
        } else {
            return 0.5;
        }
    }

    @Before
    public void setUp() {
        target = new ScaleSpaceFactory();

        imageSigma = new HashMap<Image, Double>();

        downScaler = mock(DownScaler.class);
        when(downScaler.downScale(any(Image.class))).thenAnswer(new Answer<Image>() {

            @Override
            public Image answer(InvocationOnMock invocation) throws Throwable {
                Image image = (Image) invocation.getArguments()[0];
                Image newImage = new Image(image.getHeight() - 1, image.getWidth() - 1);
                imageSigma.put(newImage, getSigma(image) / 2.0);
                return newImage;
            }
        });

        upScaler = mock(UpScaler.class);
        when(upScaler.upScale(any(Image.class))).thenAnswer(new Answer<Image>() {

            @Override
            public Image answer(InvocationOnMock invocation) throws Throwable {
                Image image = (Image) invocation.getArguments()[0];
                Image newImage = new Image(image.getHeight() + 1, image.getWidth() + 1);
                imageSigma.put(newImage, getSigma(image) * 2.0);
                return newImage;
            }
        });

        filter = mock(LowPassFilter.class);
        when(filter.filter(any(Image.class), anyDouble())).thenAnswer(new Answer<Image>() {

            @Override
            public Image answer(InvocationOnMock invocation) throws Throwable {
                Image image = (Image) invocation.getArguments()[0];
                double sigma = (Double) invocation.getArguments()[1];
                Image newImage = new Image(image.getHeight(), image.getWidth());
                imageSigma.put(newImage, getSigma(image) + sigma);
                return newImage;
            }
        });
        when(filter.sigmaDifference(anyDouble(), anyDouble())).then(new Answer<Double>() {

            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                double from = (Double) invocation.getArguments()[0];
                double to = (Double) invocation.getArguments()[1];
                return to - from;
            }
        });

        octaveFactory = new OctaveFactoryImpl();

    }

    @Test(expected = NullPointerException.class)
    public void imageNull() {
        target.create(null, 3, 0.5, 1.7,
                mock(UpScaler.class), mock(DownScaler.class), mock(LowPassFilter.class), mock(OctaveFactory.class));
    }

    @Test(expected = NullPointerException.class)
    public void upscalerNull() {
        target.create(new Image(10, 10), 3, 0.5, 1.7,
                null, mock(DownScaler.class), mock(LowPassFilter.class), mock(OctaveFactory.class));
    }

    @Test(expected = NullPointerException.class)
    public void downscalerNull() {
        target.create(new Image(10, 10), 3, 0.5, 1.7,
                mock(UpScaler.class), null, mock(LowPassFilter.class), mock(OctaveFactory.class));
    }

    @Test(expected = NullPointerException.class)
    public void filterNull() {
        target.create(new Image(10, 10), 3, 0.5, 1.7,
                mock(UpScaler.class), mock(DownScaler.class), null, mock(OctaveFactory.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scalesPerOctaveZero() {
        target.create(new Image(10, 10), 0, 0.5, 1.7,
                mock(UpScaler.class), mock(DownScaler.class), mock(LowPassFilter.class), mock(OctaveFactory.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroOriginalBlur() {
        target.create(new Image(10, 10), 3, 0, 1.7,
                mock(UpScaler.class), mock(DownScaler.class), mock(LowPassFilter.class), mock(OctaveFactory.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toSmallInitialBlur() {
        target.create(new Image(10, 10), 3, 1.0, 1.99,
                mock(UpScaler.class), mock(DownScaler.class), mock(LowPassFilter.class), mock(OctaveFactory.class));
    }

    @Test
    public void smallImage() {
        ScaleSpace scaleSpace = target.create(new Image(10, 1), 1, 0.5, 1.7, upScaler, downScaler, filter, octaveFactory);
        assertEquals(2, scaleSpace.getOctaves().size());

        assertEquals(2, scaleSpace.getOctaves().get(0).getWidth());
        assertEquals(11, scaleSpace.getOctaves().get(0).getHeight());

        assertEquals(1, scaleSpace.getOctaves().get(1).getWidth());
        assertEquals(10, scaleSpace.getOctaves().get(1).getHeight());
    }

    @Test
    public void octave0scale0() {
        ScaleSpace scaleSpace = target.create(new Image(10, 10), 1, 0.5, 1.7, upScaler, downScaler, filter, octaveFactory);
        Image firstImage = scaleSpace.getOctaves().get(0).getScaleImages().get(0);
        assertEquals(1.7, getSigma(firstImage), 1E-6);
    }

    @Test
    public void octave1scale0() {
        ScaleSpace scaleSpace = target.create(new Image(10, 10), 1, 0.5, 1.7, upScaler, downScaler, filter, octaveFactory);
        Image firstImage = scaleSpace.getOctaves().get(1).getScaleImages().get(0);
        assertEquals(1.7, getSigma(firstImage), 1E-6);
    }

    @Test
    public void octave2scale0() {
        ScaleSpace scaleSpace = target.create(new Image(10, 10), 1, 0.5, 1.7, upScaler, downScaler, filter, octaveFactory);
        Image firstImage = scaleSpace.getOctaves().get(2).getScaleImages().get(0);
        assertEquals(1.7, getSigma(firstImage), 1E-6);
    }
}