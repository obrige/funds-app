package com.fundhelper.app.data.db

import androidx.room.*
import com.fundhelper.app.data.model.FundEntity
import com.fundhelper.app.data.model.GroupEntity
import com.fundhelper.app.data.model.IndexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDao {
    @Query("SELECT * FROM funds ORDER BY sortOrder ASC")
    fun getAllFunds(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE `group` = :group ORDER BY sortOrder ASC")
    fun getFundsByGroup(group: String): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE code = :code")
    suspend fun getFundByCode(code: String): FundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFunds(funds: List<FundEntity>)

    @Update
    suspend fun updateFund(fund: FundEntity)

    @Delete
    suspend fun deleteFund(fund: FundEntity)

    @Query("DELETE FROM funds WHERE code = :code")
    suspend fun deleteFundByCode(code: String)

    @Query("UPDATE funds SET sortOrder = :order WHERE code = :code")
    suspend fun updateOrder(code: String, order: Int)

    @Query("UPDATE funds SET shares = :shares WHERE code = :code")
    suspend fun updateShares(code: String, shares: Double)

    @Query("UPDATE funds SET costPrice = :cost WHERE code = :code")
    suspend fun updateCostPrice(code: String, cost: Double)

    @Query("UPDATE funds SET isFavorite = :fav WHERE code = :code")
    suspend fun updateFavorite(code: String, fav: Boolean)

    @Query("SELECT * FROM funds WHERE isFavorite = 1 LIMIT 1")
    suspend fun getFavoriteFund(): FundEntity?

    @Query("SELECT COUNT(*) FROM funds")
    suspend fun getFundCount(): Int
}

@Dao
interface IndexDao {
    @Query("SELECT * FROM indices ORDER BY sortOrder ASC")
    fun getAllIndices(): Flow<List<IndexEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndex(index: IndexEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndices(indices: List<IndexEntity>)

    @Delete
    suspend fun deleteIndex(index: IndexEntity)

    @Query("DELETE FROM indices WHERE secId = :secId")
    suspend fun deleteBySecId(secId: String)

    @Query("SELECT COUNT(*) FROM indices")
    suspend fun getCount(): Int
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM fund_groups ORDER BY sortOrder ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)
}
