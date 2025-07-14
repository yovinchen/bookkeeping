package com.yovinchen.bookkeeping.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.yovinchen.bookkeeping.R

/**
 * 图标管理器
 * 集中管理应用中使用的各类图标资源
 * 
 * 主要功能：
 * 1. 管理分类图标和成员图标的映射关系
 * 2. 提供根据名称获取对应图标的方法
 * 3. 提供获取所有可用图标的方法
 */
object IconManager {
    /**
     * 类别图标映射
     * 将分类名称映射到对应的图标资源ID
     */
    private val categoryIcons = mapOf(
        "餐饮" to R.drawable.ic_category_food_24dp,        // 餐饮类别对应食物图标
        "交通" to R.drawable.ic_category_taxi_24dp,        // 交通类别对应出租车图标
        "购物" to R.drawable.ic_category_supermarket_24dp, // 购物类别对应超市图标
        "娱乐" to R.drawable.ic_category_bar_24dp,         // 娱乐类别对应酒吧图标
        "居住" to R.drawable.ic_category_hotel_24dp,       // 居住类别对应酒店图标
        "医疗" to R.drawable.ic_category_medicine_24dp,    // 医疗类别对应药品图标
        "教育" to R.drawable.ic_category_training_24dp,    // 教育类别对应培训图标
        "宠物" to R.drawable.ic_category_pet_24dp,         // 宠物类别对应宠物图标
        "鲜花" to R.drawable.ic_category_flower_24dp,      // 鲜花类别对应花图标
        "外卖" to R.drawable.ic_category_delivery_24dp,    // 外卖类别对应外卖图标
        "数码" to R.drawable.ic_category_digital_24dp,     // 数码类别对应数码产品图标
        "化妆品" to R.drawable.ic_category_cosmetics_24dp, // 化妆品类别对应化妆品图标
        "水果" to R.drawable.ic_category_fruit_24dp,       // 水果类别对应水果图标
        "零食" to R.drawable.ic_category_snack_24dp,       // 零食类别对应零食图标
        "蔬菜" to R.drawable.ic_category_vegetable_24dp,   // 蔬菜类别对应蔬菜图标
        "工资" to R.drawable.ic_category_membership_24dp,  // 工资类别对应会员图标
        "礼物" to R.drawable.ic_category_gift_24dp,        // 礼物类别对应礼物图标
        "其他" to R.drawable.ic_category_more_24dp,        // 其他类别对应更多图标
        "会员" to R.drawable.ic_category_membership_24dp,  // 会员类别对应会员图标
        "奖金" to R.drawable.ic_category_gift_24dp,        // 奖金类别对应礼物图标
        "投资" to R.drawable.ic_category_digital_24dp      // 投资类别对应数码图标
    )

    /**
     * 成员图标映射
     * 将成员角色名称映射到对应的图标资源ID
     */
    private val memberIcons = mapOf(
        "自己" to R.drawable.ic_member_boy_24dp,           // 自己对应男孩图标
        "老婆" to R.drawable.ic_member_bride_24dp,         // 老婆对应新娘图标
        "老公" to R.drawable.ic_member_groom_24dp,         // 老公对应新郎图标
        "家庭" to R.drawable.ic_member_family_24dp,        // 家庭对应家庭图标
        "儿子" to R.drawable.ic_member_baby_boy_24dp,      // 儿子对应男婴图标
        "女儿" to R.drawable.ic_member_baby_girl_24dp,     // 女儿对应女婴图标
        "爸爸" to R.drawable.ic_member_father_24dp,        // 爸爸对应父亲图标
        "妈妈" to R.drawable.ic_member_mother_24dp,        // 妈妈对应母亲图标
        "爷爷" to R.drawable.ic_member_grandfather_24dp,   // 爷爷对应祖父图标
        "奶奶" to R.drawable.ic_member_grandmother_24dp,   // 奶奶对应祖母图标
        "男生" to R.drawable.ic_member_boy_24dp,           // 男生对应男孩图标
        "女生" to R.drawable.ic_member_girl_24dp,          // 女生对应女孩图标
        "外公" to R.drawable.ic_member_grandfather_24dp,   // 外公对应祖父图标
        "外婆" to R.drawable.ic_member_grandmother_24dp,   // 外婆对应祖母图标
        "其他" to R.drawable.ic_member_girl_24dp           // 其他成员使用女孩图标作为默认值
    )

    /**
     * 获取分类对应的图标向量
     * 用于在Compose UI中直接使用
     * 
     * @param name 分类名称
     * @return 对应的图标向量，如果未找到则返回null
     */
    @Composable
    fun getCategoryIconVector(name: String): ImageVector? {
        return categoryIcons[name]?.let { ImageVector.vectorResource(id = it) }
    }

    /**
     * 获取成员对应的图标向量
     * 用于在Compose UI中直接使用
     * 
     * @param name 成员名称
     * @return 对应的图标向量，如果未找到则返回null
     */
    @Composable
    fun getMemberIconVector(name: String): ImageVector? {
        return memberIcons[name]?.let { ImageVector.vectorResource(id = it) }
    }

    /**
     * 获取分类对应的图标资源ID
     * 
     * @param name 分类名称
     * @return 对应的图标资源ID，如果未找到则返回null
     */
    @DrawableRes
    fun getCategoryIcon(name: String): Int? {
        return categoryIcons[name]
    }

    /**
     * 获取成员对应的图标资源ID
     * 
     * @param name 成员名称
     * @return 对应的图标资源ID，如果未找到则返回null
     */
    @DrawableRes
    fun getMemberIcon(name: String): Int? {
        return memberIcons[name]
    }

    /**
     * 获取所有可用的分类图标资源ID列表
     * 
     * @return 所有分类图标的资源ID列表
     */
    fun getAllCategoryIcons(): List<Int> {
        return categoryIcons.values.toList()
    }

    /**
     * 获取所有可用的成员图标资源ID列表
     * 
     * @return 所有成员图标的资源ID列表
     */
    fun getAllMemberIcons(): List<Int> {
        return memberIcons.values.toList()
    }
}
