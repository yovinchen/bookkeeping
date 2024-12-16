package com.yovinchen.bookkeeping.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.yovinchen.bookkeeping.R

object IconManager {
    // 类别图标映射
    private val categoryIcons = mapOf(
        "食品" to R.drawable.ic_category_food_24dp,
        "交通" to R.drawable.ic_category_taxi_24dp,
        "娱乐" to R.drawable.ic_category_bar_24dp,
        "购物" to R.drawable.ic_category_supermarket_24dp,
        "工资" to R.drawable.ic_category_membership_24dp,
        "服装" to R.drawable.ic_category_clothes_24dp,
        "数码" to R.drawable.ic_category_digital_24dp,
        "饮料" to R.drawable.ic_category_drink_24dp,
        "医疗" to R.drawable.ic_category_medicine_24dp,
        "旅行" to R.drawable.ic_category_travel_24dp,
        "便利店" to R.drawable.ic_category_convenience_24dp,
        "化妆品" to R.drawable.ic_category_cosmetics_24dp,
        "外卖" to R.drawable.ic_category_delivery_24dp,
        "鲜花" to R.drawable.ic_category_flower_24dp,
        "水果" to R.drawable.ic_category_fruit_24dp,
        "礼物" to R.drawable.ic_category_gift_24dp,
        "住宿" to R.drawable.ic_category_hotel_24dp,
        "宠物" to R.drawable.ic_category_pet_24dp,
        "景点" to R.drawable.ic_category_scenic_24dp,
        "零食" to R.drawable.ic_category_snack_24dp,
        "培训" to R.drawable.ic_category_training_24dp,
        "蔬菜" to R.drawable.ic_category_vegetable_24dp,
        "婴儿" to R.drawable.ic_category_baby_24dp,
        "餐饮" to R.drawable.ic_category_food_24dp,  // 添加餐饮分类
        "居住" to R.drawable.ic_category_hotel_24dp,  // 添加居住分类
        "其他" to R.drawable.ic_category_more_24dp
    )

    // 成员图标映射
    private val memberIcons = mapOf(
        "自己" to R.drawable.ic_member_boy_24dp,
        "家庭" to R.drawable.ic_member_family_24dp,
        "父亲" to R.drawable.ic_member_father_24dp,
        "母亲" to R.drawable.ic_member_mother_24dp,
        "男宝" to R.drawable.ic_member_baby_boy_24dp,
        "女宝" to R.drawable.ic_member_baby_girl_24dp,
        "新娘" to R.drawable.ic_member_bride_24dp,
        "新郎" to R.drawable.ic_member_groom_24dp,
        "爷爷" to R.drawable.ic_member_grandfather_24dp,
        "奶奶" to R.drawable.ic_member_grandmother_24dp,
        "男生" to R.drawable.ic_member_boy_24dp,
        "女生" to R.drawable.ic_member_girl_24dp,
        "其他" to R.drawable.ic_member_girl_24dp
    )

    @Composable
    fun getCategoryIconVector(name: String): ImageVector? {
        return categoryIcons[name]?.let { ImageVector.vectorResource(id = it) }
    }

    @Composable
    fun getMemberIconVector(name: String): ImageVector? {
        return memberIcons[name]?.let { ImageVector.vectorResource(id = it) }
    }

    @DrawableRes
    fun getCategoryIcon(name: String): Int? {
        return categoryIcons[name]
    }

    @DrawableRes
    fun getMemberIcon(name: String): Int? {
        return memberIcons[name]
    }

    fun getAllCategoryIcons(): List<Int> {
        return categoryIcons.values.toList()
    }

    fun getAllMemberIcons(): List<Int> {
        return memberIcons.values.toList()
    }
}
