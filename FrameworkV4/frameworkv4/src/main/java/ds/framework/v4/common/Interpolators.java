/*
	Copyright 2013 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package ds.framework.v4.common;

public class Interpolators {
	private static float sViscousFluidScale;
	private static float sViscousFluidNormalize;

	static {
		// This controls the viscous fluid effect (how much of it)
		sViscousFluidScale = 8.0f;
		// must be set to 1.0 (used in viscousFluid())
		sViscousFluidNormalize = 1.0f;
		sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
	}
	
	public static float viscousFluid(float x)
    {
        x *= sViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float)Math.exp(-x));
        } else {
            float start = 0.36787944117f;   // 1/e == exp(-1)
            x = 1.0f - (float)Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= sViscousFluidNormalize;
        return x;
    }
}
