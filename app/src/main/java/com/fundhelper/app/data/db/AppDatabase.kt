package com.fundhelper.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fundhelper.app.data.model.FundEntity
import com.fundhelper.app.data.model.GroupEntity
import com.fundhelper.app.data.model.IndexEntity

@Database(
    entities = [FundEntity::class, IndexEntity::class, GroupEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fundDao(): FundDao
    abstract fun indexDao(): IndexDao
    abstract fun groupDao(): GroupDao
}
