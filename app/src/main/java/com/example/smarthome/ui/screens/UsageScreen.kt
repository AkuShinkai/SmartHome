package com.example.smarthome.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun UsageScreen(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        LineUsageChart()
        Spacer(modifier = Modifier.height(16.dp))
        BarUsageChart()
    }

}

@Composable
fun LineUsageChart(){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ){
            LineChart(
                modifier = Modifier.padding(horizontal = 22.dp),
                data = remember {
                    listOf(
                        Line(
                            label = "Windows",
                            values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
                            color = SolidColor(Color(0xFF23af92)),
                            firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                            gradientAnimationDelay = 1000,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )
                },
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
            )
        }
    }
}

@Composable
fun BarUsageChart() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .horizontalScroll(rememberScrollState()) // Scroll ke samping
            ) {
                ColumnChart(
                    modifier = Modifier
                        .width(600.dp) // Lebar minimal agar bisa di-scroll
                        .padding(horizontal = 22.dp),
                    data = remember {
                        listOf(
                            Bars(
                                label = "Jan",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Mar",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Apr",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "May",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Jun",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Jul",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Aug",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Sep",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Oct",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Nov",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Dec",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                        )
                    },
                    barProperties = BarProperties(
                        spacing = 3.dp
                    ),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsageScreenPreview() {
    UsageScreen(navController = null)
}