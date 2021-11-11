# line-chart [![](https://jitpack.io/v/davidmigloz/line-chart.svg)](https://jitpack.io/#davidmigloz/line-chart)

A simple Android line chart library.

**Features:**
- Auto-scaling drawing area and labels
- Grid lines
- Baseline
- Zero line
- Different filling modes
- Custom label formatters
- Scrubbing support
- Data change animations
- Fully customizable styles
- Theme-friendly

Screenshot of the library being used in a real app:

<img src="doc/real.png" width="250" />

Take a look at the [sample app](https://github.com/davidmigloz/line-chart/tree/master/sample) to see the library working.

| Default                        | Custom styles                 | Animation                        |
|--------------------------------|-------------------------------|----------------------------------|
| <img src="doc/default.png" width="220" /> | <img src="doc/custom.png" width="220" /> | <img src="doc/animation.gif" width="220" /> |

## Usage

#### Step 1

Add the JitPack repository to your `build.gradle` file:

```gradle
allprojects {
	repositories {
		//...
		maven { url "https://jitpack.io" }
	}
}
```

#### Step 2

Add the dependency:

```gradle
dependencies {
	implementation 'com.github.davidmigloz:line-chart:2.1.0'
}
```

[CHANGELOG](https://github.com/davidmigloz/line-chart/blob/master/CHANGELOG.md)

#### Step 3

Use `LineChartView` in your layout:

```xml
<com.davidmiguel.linechart.LineChartView
    android:id="@+id/lineChart"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:linechart_gridXDivisions="5"
    app:linechart_gridYDivisions="5"
    app:linechart_fillColor="@color/blue"
    app:linechart_lineColor="@color/white"
    app:linechart_scrubEnabled="false" />
```

#### XML attributes

Take a look to [`attrs.xml`](https://github.com/davidmigloz/line-chart/blob/master/linechart/src/main/res/values/attrs.xml).

#### Methods

Take a look to [`LineChart.kt`](https://github.com/davidmigloz/line-chart/blob/master/linechart/src/main/java/com/davidmiguel/linechart/LineChart.kt).

#### Listeners

If you want to listen to scrub events, you can register a `OnScrubListener`:

```kotlin
lineChart.scrubListener = OnScrubListener { value: Any? ->
    //...
}
```

## Contributing

If you find any issues or have any questions, ideas... feel free to [open an issue](https://github.com/davidmigloz/line-chart/issues/new).
Pull request are very appreciated.

## Credits

This library is inspired by the power of [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) and the simplicity of [Spark](https://github.com/robinhood/spark).

## License

Copyright (c) 2021 David Miguel Lozano

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
