package de.leohopper.myturtle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class ChartPoint(
    val label: String,
    val value: Float,
)

@Composable
fun MeasurementLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    emptyLabel: String,
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val minimum = points.minOf { it.value }
    val maximum = points.maxOf { it.value }
    val safeRange = (maximum - minimum).takeIf { it > 0f } ?: 1f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        val horizontalPadding = 12.dp.toPx()
        val verticalPadding = 18.dp.toPx()
        val chartWidth = size.width - (horizontalPadding * 2)
        val chartHeight = size.height - (verticalPadding * 2)

        fun x(index: Int): Float {
            val step = chartWidth / (points.lastIndex.coerceAtLeast(1))
            return horizontalPadding + (step * index)
        }

        fun y(value: Float): Float {
            val progress = (value - minimum) / safeRange
            return verticalPadding + chartHeight - (progress * chartHeight)
        }

        repeat(4) { index ->
            val fraction = index / 3f
            val gridY = verticalPadding + (chartHeight * fraction)
            drawLine(
                color = lineColor.copy(alpha = 0.15f),
                start = Offset(horizontalPadding, gridY),
                end = Offset(size.width - horizontalPadding, gridY),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            val pointOffset = Offset(x(index), y(point.value))
            if (index == 0) {
                path.moveTo(pointOffset.x, pointOffset.y)
            } else {
                path.lineTo(pointOffset.x, pointOffset.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx()),
        )

        points.forEachIndexed { index, point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x(index), y(point.value)),
            )
        }
    }
}
