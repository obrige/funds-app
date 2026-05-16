package com.fundhelper.app.widget

import android.content.Context
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
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
                        .background(GlanceTheme.colors.surface)
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
                                    Text(
                                        fund.code,
                                        style = TextStyle(fontSize = 11.sp)
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
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FundWidget().update(context, glanceId)
    }
}

class FundWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FundWidget()
}
