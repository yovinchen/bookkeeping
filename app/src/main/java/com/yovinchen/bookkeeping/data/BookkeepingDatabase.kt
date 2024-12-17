package com.yovinchen.bookkeeping.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yovinchen.bookkeeping.R
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
    version = 4,
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
                        description TEXT NOT NULL DEFAULT '',
                        icon INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // 插入默认成员
                db.execSQL("""
                    INSERT INTO members (name, description, icon)
                    VALUES ('自己', '默认成员', ${R.drawable.ic_member_boy_24dp})
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
                        type TEXT NOT NULL,
                        icon INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // 复制分类数据
                db.execSQL("""
                    INSERT INTO categories_new (id, name, type, icon)
                    SELECT id, name, type, 0 FROM categories
                """)

                // 删除旧表
                db.execSQL("DROP TABLE categories")

                // 重命名新表
                db.execSQL("ALTER TABLE categories_new RENAME TO categories")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 如果需要，在这里添加数据库迁移逻辑
                // 由于这次更改可能只是schema hash的变化，我们不需要实际的数据库更改
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
                                            insertMember(Member(name = "自己", description = "默认成员", icon = R.drawable.ic_member_boy_24dp))
                                            insertMember(Member(name = "老婆", description = "默认成员", icon = R.drawable.ic_member_girl_24dp))
                                            insertMember(Member(name = "老公", description = "默认成员", icon = R.drawable.ic_member_boy_24dp))
                                            insertMember(Member(name = "家庭", description = "默认成员", icon = R.drawable.ic_member_family_24dp))
                                            insertMember(Member(name = "儿子", description = "默认成员", icon = R.drawable.ic_member_baby_boy_24dp))
                                            insertMember(Member(name = "女儿", description = "默认成员", icon = R.drawable.ic_member_baby_girl_24dp))
                                            insertMember(Member(name = "爸爸", description = "默认成员", icon = R.drawable.ic_member_father_24dp))
                                            insertMember(Member(name = "妈妈", description = "默认成员", icon = R.drawable.ic_member_mother_24dp))
                                            insertMember(Member(name = "爷爷", description = "默认成员", icon = R.drawable.ic_member_grandfather_24dp))
                                            insertMember(Member(name = "奶奶", description = "默认成员", icon = R.drawable.ic_member_grandmother_24dp))
                                            insertMember(Member(name = "外公", description = "默认成员", icon = R.drawable.ic_member_grandfather_24dp))
                                            insertMember(Member(name = "外婆", description = "默认成员", icon = R.drawable.ic_member_grandmother_24dp))
                                            insertMember(Member(name = "其他人", description = "默认成员", icon = R.drawable.ic_member_boy_24dp))
                                        }
                                    }

// 初始化默认分类
                                    database.categoryDao().apply {
                                        // 支出分类
                                        insertCategory(Category(name = "餐饮", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_food_24dp))      // "餐饮" to R.drawable.ic_category_food_24dp
                                        insertCategory(Category(name = "交通", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_taxi_24dp))      // "交通" to R.drawable.ic_category_taxi_24dp
                                        insertCategory(Category(name = "购物", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_supermarket_24dp)) // "购物" to R.drawable.ic_category_supermarket_24dp
                                        insertCategory(Category(name = "娱乐", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_bar_24dp))       // "娱乐" to R.drawable.ic_category_bar_24dp
                                        insertCategory(Category(name = "居住", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_hotel_24dp))     // "居住" to R.drawable.ic_category_hotel_24dp
                                        insertCategory(Category(name = "医疗", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_medicine_24dp))  // "医疗" to R.drawable.ic_category_medicine_24dp
                                        insertCategory(Category(name = "教育", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_training_24dp))  // "培训" to R.drawable.ic_category_training_24dp
                                        insertCategory(Category(name = "宠物", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_pet_24dp))       // "宠物" to R.drawable.ic_category_pet_24dp
                                        insertCategory(Category(name = "花卉", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_flower_24dp))    // "鲜花" to R.drawable.ic_category_flower_24dp
                                        insertCategory(Category(name = "酒吧", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_bar_24dp))       // "娱乐" to R.drawable.ic_category_bar_24dp
                                        insertCategory(Category(name = "快递", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_delivery_24dp))  // "外卖" to R.drawable.ic_category_delivery_24dp
                                        insertCategory(Category(name = "数码", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_digital_24dp))   // "数码" to R.drawable.ic_category_digital_24dp
                                        insertCategory(Category(name = "化妆品", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_cosmetics_24dp)) // "化妆品" to R.drawable.ic_category_cosmetics_24dp
                                        insertCategory(Category(name = "水果", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_fruit_24dp))     // "水果" to R.drawable.ic_category_fruit_24dp
                                        insertCategory(Category(name = "零食", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_snack_24dp))     // "零食" to R.drawable.ic_category_snack_24dp
                                        insertCategory(Category(name = "蔬菜", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_vegetable_24dp)) // "蔬菜" to R.drawable.ic_category_vegetable_24dp
                                        insertCategory(Category(name = "会员", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_membership_24dp)) // "工资" to R.drawable.ic_category_membership_24dp
                                        insertCategory(Category(name = "礼物", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_gift_24dp))      // "礼物" to R.drawable.ic_category_gift_24dp
                                        insertCategory(Category(name = "其他支出", type = TransactionType.EXPENSE, icon = R.drawable.ic_category_more_24dp))  // "其他" to R.drawable.ic_category_more_24dp

// 收入分类
                                        insertCategory(Category(name = "工资", type = TransactionType.INCOME, icon = R.drawable.ic_category_membership_24dp)) // "工资" to R.drawable.ic_category_membership_24dp
                                        insertCategory(Category(name = "奖金", type = TransactionType.INCOME, icon = R.drawable.ic_category_gift_24dp))      // "奖金" to R.drawable.ic_category_gift_24dp
                                        insertCategory(Category(name = "投资", type = TransactionType.INCOME, icon = R.drawable.ic_category_digital_24dp))   // "投资" to R.drawable.ic_category_digital_24dp
                                        insertCategory(Category(name = "其他收入", type = TransactionType.INCOME, icon = R.drawable.ic_category_more_24dp))   // "其他" to R.drawable.ic_category_more_24dp

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
