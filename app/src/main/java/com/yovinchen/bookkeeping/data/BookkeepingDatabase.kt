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
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BookkeepingRecord::class, Category::class, Member::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BookkeepingDatabase : RoomDatabase() {
    abstract fun bookkeepingDao(): BookkeepingDao
    abstract fun categoryDao(): CategoryDao
    abstract fun memberDao(): MemberDao

    companion object {
        private const val TAG = "BookkeepingDatabase"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建成员表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT ''
                    )
                """)
                
                // 插入默认成员
                db.execSQL("""
                    INSERT INTO members (name, description)
                    VALUES ('自己', '默认成员')
                """)

                // 修改记账记录表，添加成员ID字段
                db.execSQL("""
                    ALTER TABLE bookkeeping_records
                    ADD COLUMN memberId INTEGER DEFAULT NULL
                    REFERENCES members(id) ON DELETE SET NULL
                """)

                // 更新现有记录，将其关联到默认成员
                db.execSQL("""
                    UPDATE bookkeeping_records
                    SET memberId = (SELECT id FROM members WHERE name = '我自己')
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 重新创建记账记录表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bookkeeping_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        memberId INTEGER,
                        FOREIGN KEY(memberId) REFERENCES members(id) ON DELETE SET NULL
                    )
                """)

                // 复制数据
                db.execSQL("""
                    INSERT INTO bookkeeping_records_new (id, amount, type, category, description, date, memberId)
                    SELECT id, amount, type, category, description, date, memberId FROM bookkeeping_records
                """)

                // 删除旧表
                db.execSQL("DROP TABLE bookkeeping_records")

                // 重命名新表
                db.execSQL("ALTER TABLE bookkeeping_records_new RENAME TO bookkeeping_records")

                // 重新创建分类表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL
                    )
                """)

                // 复制分类数据
                db.execSQL("""
                    INSERT INTO categories_new (id, name, type)
                    SELECT id, name, type FROM categories
                """)

                // 删除旧表
                db.execSQL("DROP TABLE categories")

                // 重命名新表
                db.execSQL("ALTER TABLE categories_new RENAME TO categories")
            }
        }

        @Volatile
        private var INSTANCE: BookkeepingDatabase? = null

        fun getDatabase(context: Context): BookkeepingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookkeepingDatabase::class.java,
                    "bookkeeping_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created, initializing default data")
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val database = getDatabase(context)
                                    
                                    // 初始化默认成员
                                    database.memberDao().apply {
                                        if (getMemberCount() == 0) {
                                            insertMember(Member(name = "自己", description = "默认成员"))
                                        }
                                    }

                                    // 初始化默认分类
                                    database.categoryDao().apply {
                                        // 支出分类
                                        insertCategory(Category(name = "餐饮", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "交通", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "购物", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "娱乐", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "居住", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "医疗", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "教育", type = TransactionType.EXPENSE))
                                        insertCategory(Category(name = "其他支出", type = TransactionType.EXPENSE))

                                        // 收入分类
                                        insertCategory(Category(name = "工资", type = TransactionType.INCOME))
                                        insertCategory(Category(name = "奖金", type = TransactionType.INCOME))
                                        insertCategory(Category(name = "投资", type = TransactionType.INCOME))
                                        insertCategory(Category(name = "其他收入", type = TransactionType.INCOME))
                                    }
                                    
                                    Log.d(TAG, "Default data initialized successfully")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error initializing default data", e)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
