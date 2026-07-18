package com.wallosapp.android

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Room Database Entity
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val price: Double,
    val currency: String,
    val cycle: String,
    val category: String,
    val date: String
)

// 2. Data Access Object (DAO) interface
@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subscriptions: List<SubscriptionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}

// 3. Room Database Singleton Class
@Database(entities = [SubscriptionEntity::class], version = 1, exportSchema = false)
abstract class WallosDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: WallosDatabase? = null

        fun getDatabase(context: Context): WallosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WallosDatabase::class.java,
                    "wallos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
