package com.yovinchen.bookkeeping.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.Converters
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [BookkeepingRecord::class, Category::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BookkeepingDatabase : RoomDatabase() {
    abstract fun bookkeepingDao(): BookkeepingDao

    companion object {
        private const val TAG = "BookkeepingDatabase"
        
        @Volatile
        private var Instance: BookkeepingDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.d(TAG, "Starting migration from version 1 to 2")
                    
                    // 检查表是否存在
                    val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='categories'")
                    val tableExists = cursor.moveToFirst()
                    cursor.close()
                    
                    if (tableExists) {
                        // 如果表存在，执行迁移
                        Log.d(TAG, "Categories table exists, performing migration")
                        database.execSQL("ALTER TABLE categories RENAME TO categories_old")
                        
                        database.execSQL("""
                            CREATE TABLE categories (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                name TEXT NOT NULL,
                                type TEXT NOT NULL
                            )
                        """)
                        
                        database.execSQL("""
                            INSERT INTO categories (name, type)
                            SELECT name, type FROM categories_old
                        """)
                        
                        database.execSQL("DROP TABLE categories_old")
                    } else {
                        // 如果表不存在，直接创建新表
                        Log.d(TAG, "Categories table does not exist, creating new table")
                        database.execSQL("""
                            CREATE TABLE IF NOT EXISTS categories (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                name TEXT NOT NULL,
                                type TEXT NOT NULL
                            )
                        """)
                    }
                    
                    // 确保 bookkeeping_records 表存在
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS bookkeeping_records (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL,
                            amount REAL NOT NULL,
                            category TEXT NOT NULL,
                            description TEXT NOT NULL,
                            date INTEGER NOT NULL
                        )
                    """)
                    
                    Log.d(TAG, "Migration completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during migration", e)
                    throw e
                }
            }
        }

        private suspend fun populateDefaultCategories(dao: BookkeepingDao) {
            try {
                Log.d(TAG, "Starting to populate default categories")
                // 支出类别
                listOf(
                    "餐饮",
                    "交通",
                    "购物",
                    "娱乐",
                    "医疗",
                    "住房",
                    "其他支出"
                ).forEach { name ->
                    try {
                        dao.insertCategory(Category(name = name, type = TransactionType.EXPENSE))
                        Log.d(TAG, "Added expense category: $name")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding expense category: $name", e)
                    }
                }
                
                // 收入类别
                listOf(
                    "工资",
                    "奖金",
                    "投资",
                    "其他收入"
                ).forEach { name ->
                    try {
                        dao.insertCategory(Category(name = name, type = TransactionType.INCOME))
                        Log.d(TAG, "Added income category: $name")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding income category: $name", e)
                    }
                }
                Log.d(TAG, "Finished populating default categories")
            } catch (e: Exception) {
                Log.e(TAG, "Error during category population", e)
            }
        }

        fun getDatabase(context: Context): BookkeepingDatabase {
            return Instance ?: synchronized(this) {
                try {
                    Log.d(TAG, "Creating new database instance")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        BookkeepingDatabase::class.java,
                        "bookkeeping_database"
                    )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created, initializing default categories")
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    Instance?.let { database ->
                                        populateDefaultCategories(database.bookkeepingDao())
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in onCreate callback", e)
                                }
                            }
                        }
                    })
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()  // 如果迁移失败，允许重建数据库
                    .build()
                    
                    Instance = instance
                    Log.d(TAG, "Database instance created successfully")
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating database", e)
                    throw e
                }
            }
        }
    }
}
