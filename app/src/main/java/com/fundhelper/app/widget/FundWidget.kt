package com.fundhelper.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.layout.Button
import com.fundhelper.app.data.db.AppDatabase
import kotlinx.coroutines.flow.first

class FundWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = androidx.room.Room.databaseBuilder(
            context, AppDatabase::class.java, "fund_helper.db"
        ).build()

        val funds = try {
            db.fundDao().getAllFunds().first()
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(GlanceTheme.colors.surface),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "自选基金助手",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    if (funds.isEmpty()) {
                        Text(
                            "暂无自选基金",
                            style = TextStyle(fontSize = 12.sp)
                        )
                    } else {
                        LazyColumn {
                            items(funds) { fund ->
                                Row(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        fund.name,
                                        style = TextStyle(fontSize = 12.sp),
                                        modifier = GlanceModifier.defaultWeight()
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Button(
                        text = "刷新",
                        onClick = actionRunCallback<RefreshAction>()
                    )
                }
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        FundWidget().update(context, glanceId)
    }
}

class FundWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FundWidget()
}
