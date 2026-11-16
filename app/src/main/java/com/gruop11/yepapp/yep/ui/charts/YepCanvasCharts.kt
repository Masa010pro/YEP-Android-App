package com.gruop11.yepapp.yep.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gruop11.yepapp.ui.theme.*
import com.gruop11.yepapp.yep.i18n.YepStrings

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = YepSlate600,
        )
    }
}

/**
 * Chart 1: Stage Completion % Bars vs Cohort Baseline Trend Line across Stages 1-6
 */
@Composable
fun StageCompletionComboChart(
    participantStages: List<Float>,
    cohortBaseline: List<Float>,
    primaryLabel: String,
    baselineLabel: String,
    strings: YepStrings,
    modifier: Modifier = Modifier,
) {
    val stageLabels = listOf(
        strings.stageOnboard,
        strings.stageEnrolled,
        strings.stageLearning,
        strings.stageCapstone,
        strings.stageRubric,
        strings.stagePartner,
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegendItem(YepEmerald, primaryLabel)
            ChartLegendItem(YepIndigo, baselineLabel)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
        ) {
            val leftPad = 24f
            val rightPad = 12f
            val topPad = 16f
            val bottomPad = 16f
            val chartW = (size.width - leftPad - rightPad).coerceAtLeast(10f)
            val chartH = (size.height - topPad - bottomPad).coerceAtLeast(10f)

            // Horizontal percentage guide lines (0%, 50%, 100%)
            listOf(0f, 0.5f, 1f).forEach { frac ->
                val y = topPad + chartH * (1f - frac)
                drawLine(
                    color = YepSlate200,
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.5f,
                    pathEffect = if (frac == 0f) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                )
            }

            val count = 6
            val slotW = chartW / count
            val barW = (slotW * 0.48f).coerceAtLeast(12f)

            // Draw Stage Completion Bars
            for (i in 0 until count) {
                val pct = participantStages.getOrElse(i) { 0f }.coerceIn(0f, 100f) / 100f
                val barH = chartH * pct
                val x = leftPad + slotW * i + (slotW - barW) / 2f
                val y = topPad + chartH - barH
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(YepEmerald, YepEmeraldDark)),
                    topLeft = Offset(x, y),
                    size = Size(barW, barH.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(10f, 10f),
                )
            }

            // Draw Cohort Baseline Trend Line
            val linePath = Path()
            val points = mutableListOf<Offset>()
            for (i in 0 until count) {
                val pct = cohortBaseline.getOrElse(i) { 0f }.coerceIn(0f, 100f) / 100f
                val cx = leftPad + slotW * i + slotW / 2f
                val cy = topPad + chartH * (1f - pct)
                points.add(Offset(cx, cy))
                if (i == 0) linePath.moveTo(cx, cy) else linePath.lineTo(cx, cy)
            }
            drawPath(
                path = linePath,
                color = YepIndigo,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round),
            )
            points.forEach { pt ->
                drawCircle(color = YepWhite, radius = 8f, center = pt)
                drawCircle(color = YepIndigo, radius = 5.5f, center = pt)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            stageLabels.forEachIndexed { idx, label ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${participantStages.getOrElse(idx) { 0f }.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = YepEmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = YepSlate500,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Chart 2: 4-Pillar Rubric Grouped Bar Chart (Participant vs Cohort Average / 5.00)
 */
@Composable
fun RubricComparisonGroupedBarChart(
    participantScores: List<Float>,
    cohortScores: List<Float>,
    primaryLabel: String,
    cohortLabel: String,
    modifier: Modifier = Modifier,
) {
    val pillars = listOf("Execution", "Solving", "Impact", "Clarity")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegendItem(YepBlue, primaryLabel)
            ChartLegendItem(YepSlate400, cohortLabel)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            val leftPad = 16f
            val rightPad = 12f
            val topPad = 12f
            val bottomPad = 10f
            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad

            // 1.0 .. 5.0 horizontal grid lines
            listOf(0f, 0.5f, 1f).forEach { frac ->
                val y = topPad + chartH * (1f - frac)
                drawLine(
                    color = YepSlate200,
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.5f,
                )
            }

            val count = 4
            val slotW = chartW / count
            val barW = (slotW * 0.28f).coerceAtLeast(12f)
            val gap = 6f

            for (i in 0 until count) {
                val pVal = (participantScores.getOrElse(i) { 0f } / 5f).coerceIn(0f, 1f)
                val cVal = (cohortScores.getOrElse(i) { 0f } / 5f).coerceIn(0f, 1f)
                val center = leftPad + slotW * i + slotW / 2f

                val pH = chartH * pVal
                val cH = chartH * cVal

                drawRoundRect(
                    color = YepBlue,
                    topLeft = Offset(center - barW - gap / 2f, topPad + chartH - pH),
                    size = Size(barW, pH.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(8f, 8f),
                )
                drawRoundRect(
                    color = YepSlate400,
                    topLeft = Offset(center + gap / 2f, topPad + chartH - cH),
                    size = Size(barW, cH.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(8f, 8f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            pillars.forEachIndexed { idx, name ->
                val pScore = participantScores.getOrElse(idx) { 0f }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (pScore > 0f) String.format("%.1f/5", pScore) else "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = YepBlue,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = YepSlate600,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Chart 3 (Admin Impact): Youth Engagement, Enrollments & Completions Area/Line Chart
 */
@Composable
fun ImpactEngagementAreaChart(
    timeframe: String,
    themeMultiplier: Float,
    modifier: Modifier = Modifier,
) {
    val months = if (timeframe == "6M") {
        listOf("Jun", "Jul", "Aug", "Sep", "Oct", "Nov")
    } else {
        listOf("Dec", "Feb", "Apr", "Jun", "Aug", "Oct", "Nov")
    }
    val engagement = if (timeframe == "6M") {
        listOf(110f, 138f, 162f, 185f, 214f, 248f).map { it * themeMultiplier }
    } else {
        listOf(64f, 82f, 108f, 134f, 168f, 205f, 248f).map { it * themeMultiplier }
    }
    val enrollments = engagement.map { it * 0.72f }
    val completions = engagement.map { it * 0.48f }
    val maxVal = (engagement.maxOrNull() ?: 250f).coerceAtLeast(10f)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartLegendItem(YepEmerald, "Active Youth")
            ChartLegendItem(YepBlue, "Enrollments")
            ChartLegendItem(YepIndigo, "Completions")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            val leftPad = 12f
            val rightPad = 12f
            val topPad = 12f
            val bottomPad = 10f
            val w = size.width - leftPad - rightPad
            val h = size.height - topPad - bottomPad

            listOf(0f, 0.5f, 1f).forEach { frac ->
                val y = topPad + h * (1f - frac)
                drawLine(YepSlate200, Offset(leftPad, y), Offset(size.width - rightPad, y), 1.5f)
            }

            fun drawSeries(values: List<Float>, color: Color, fillArea: Boolean) {
                val path = Path()
                val fillPath = Path()
                val pts = values.mapIndexed { idx, v ->
                    val x = leftPad + (w * idx / (values.size - 1).coerceAtLeast(1))
                    val y = topPad + h * (1f - (v / maxVal).coerceIn(0f, 1f))
                    Offset(x, y)
                }
                pts.forEachIndexed { idx, pt ->
                    if (idx == 0) {
                        path.moveTo(pt.x, pt.y)
                        fillPath.moveTo(pt.x, topPad + h)
                        fillPath.lineTo(pt.x, pt.y)
                    } else {
                        path.lineTo(pt.x, pt.y)
                        fillPath.lineTo(pt.x, pt.y)
                    }
                }
                if (pts.isNotEmpty() && fillArea) {
                    fillPath.lineTo(pts.last().x, topPad + h)
                    fillPath.close()
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), Color.Transparent)),
                    )
                }
                drawPath(path, color = color, style = Stroke(width = 4f, cap = StrokeCap.Round))
                pts.forEach { pt -> drawCircle(color, 4.5f, pt) }
            }

            drawSeries(engagement, YepEmerald, fillArea = true)
            drawSeries(enrollments, YepBlue, fillArea = false)
            drawSeries(completions, YepIndigo, fillArea = false)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            months.forEach { m ->
                Text(text = m, style = MaterialTheme.typography.labelSmall, color = YepSlate500)
            }
        }
    }
}

/**
 * Chart 4 (Admin Impact): 4-Pillar Skill Competency Trajectory Multi-Line Chart (2.6–5.0)
 */
@Composable
fun CompetencyTrajectoryMultiLineChart(
    timeframe: String,
    modifier: Modifier = Modifier,
) {
    val labels = if (timeframe == "6M") {
        listOf("Jun", "Jul", "Aug", "Sep", "Oct", "Nov")
    } else {
        listOf("Dec", "Feb", "Apr", "Jun", "Aug", "Nov")
    }
    val exec = listOf(2.8f, 3.1f, 3.4f, 3.7f, 4.1f, 4.4f)
    val solving = listOf(2.7f, 3.0f, 3.3f, 3.6f, 3.9f, 4.2f)
    val impact = listOf(3.0f, 3.3f, 3.6f, 4.0f, 4.3f, 4.6f)
    val clarity = listOf(2.6f, 2.9f, 3.2f, 3.5f, 3.8f, 4.1f)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ChartLegendItem(YepEmerald, "Execution")
            ChartLegendItem(YepBlue, "Solving")
            ChartLegendItem(YepIndigo, "Impact")
            ChartLegendItem(YepAmber, "Clarity")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            val leftPad = 12f
            val rightPad = 12f
            val topPad = 12f
            val bottomPad = 10f
            val w = size.width - leftPad - rightPad
            val h = size.height - topPad - bottomPad
            val minV = 2.5f
            val maxV = 5.0f

            listOf(0f, 0.5f, 1f).forEach { frac ->
                val y = topPad + h * (1f - frac)
                drawLine(YepSlate200, Offset(leftPad, y), Offset(size.width - rightPad, y), 1.5f)
            }

            fun drawLineSeries(series: List<Float>, color: Color) {
                val path = Path()
                val pts = series.mapIndexed { idx, v ->
                    val x = leftPad + (w * idx / (series.size - 1).coerceAtLeast(1))
                    val norm = ((v - minV) / (maxV - minV)).coerceIn(0f, 1f)
                    val y = topPad + h * (1f - norm)
                    Offset(x, y)
                }
                pts.forEachIndexed { idx, pt ->
                    if (idx == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                drawPath(path, color = color, style = Stroke(width = 3.8f, cap = StrokeCap.Round))
                pts.forEach { drawCircle(color, 4f, it) }
            }

            drawLineSeries(exec, YepEmerald)
            drawLineSeries(solving, YepBlue)
            drawLineSeries(impact, YepIndigo)
            drawLineSeries(clarity, YepAmber)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = YepSlate500) }
        }
    }
}

/**
 * Chart 5 (Admin Impact): Cohort Tier Distribution Stacked Bar Chart (Beginner / Intermediate / Advanced)
 */
@Composable
fun CohortTierStackedBarChart(
    modifier: Modifier = Modifier,
) {
    val cohorts = listOf("Q1", "Q2", "Q3", "Q4")
    val beginner = listOf(60f, 48f, 34f, 22f)
    val intermediate = listOf(30f, 36f, 42f, 44f)
    val advanced = listOf(10f, 16f, 24f, 34f)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartLegendItem(YepAmber, "Beginner")
            ChartLegendItem(YepBlue, "Intermediate")
            ChartLegendItem(YepEmerald, "Advanced")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp),
        ) {
            val leftPad = 16f
            val rightPad = 16f
            val topPad = 10f
            val bottomPad = 8f
            val w = size.width - leftPad - rightPad
            val h = size.height - topPad - bottomPad
            val slotW = w / cohorts.size
            val barW = (slotW * 0.48f).coerceAtLeast(20f)

            for (i in cohorts.indices) {
                val x = leftPad + slotW * i + (slotW - barW) / 2f
                val begH = h * (beginner[i] / 100f)
                val intH = h * (intermediate[i] / 100f)
                val advH = h * (advanced[i] / 100f)

                var curY = topPad + h
                // Beginner bottom
                curY -= begH
                drawRect(color = YepAmber, topLeft = Offset(x, curY), size = Size(barW, begH))
                // Intermediate middle
                curY -= intH
                drawRect(color = YepBlue, topLeft = Offset(x, curY), size = Size(barW, intH))
                // Advanced top
                curY -= advH
                drawRoundRect(
                    color = YepEmerald,
                    topLeft = Offset(x, curY),
                    size = Size(barW, advH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            cohorts.forEachIndexed { i, q ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "$q Cohort",
                        style = MaterialTheme.typography.labelSmall,
                        color = YepSlateDark,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${advanced[i].toInt()}% Adv",
                        style = MaterialTheme.typography.labelSmall,
                        color = YepEmeraldDark,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}
