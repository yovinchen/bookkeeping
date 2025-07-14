package com.yovinchen.bookkeeping.utils

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

object DateUtils {
    
    /**
     * 根据月度开始日期计算给定日期所属的记账月份
     * @param date 要判断的日期
     * @param monthStartDay 月度开始日期（1-28）
     * @return 该日期所属的记账月份
     */
    fun getAccountingMonth(date: Date, monthStartDay: Int): YearMonth {
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return getAccountingMonth(localDate, monthStartDay)
    }
    
    /**
     * 根据月度开始日期计算给定日期所属的记账月份
     * @param date 要判断的日期
     * @param monthStartDay 月度开始日期（1-28）
     * @return 该日期所属的记账月份
     */
    fun getAccountingMonth(date: LocalDate, monthStartDay: Int): YearMonth {
        val dayOfMonth = date.dayOfMonth
        
        return if (dayOfMonth >= monthStartDay) {
            // 当前日期大于等于开始日期，属于当前月
            YearMonth.from(date)
        } else {
            // 当前日期小于开始日期，属于上个月
            YearMonth.from(date.minusMonths(1))
        }
    }
    
    /**
     * 获取记账月份的开始日期
     * @param yearMonth 记账月份
     * @param monthStartDay 月度开始日期（1-28）
     * @return 该记账月份的开始日期
     */
    fun getMonthStartDate(yearMonth: YearMonth, monthStartDay: Int): LocalDate {
        return yearMonth.atDay(monthStartDay)
    }
    
    /**
     * 获取记账月份的结束日期
     * @param yearMonth 记账月份
     * @param monthStartDay 月度开始日期（1-28）
     * @return 该记账月份的结束日期
     */
    fun getMonthEndDate(yearMonth: YearMonth, monthStartDay: Int): LocalDate {
        val nextMonth = yearMonth.plusMonths(1)
        return nextMonth.atDay(monthStartDay).minusDays(1)
    }
    
    /**
     * 检查日期是否在指定的记账月份内
     * @param date 要检查的日期
     * @param yearMonth 记账月份
     * @param monthStartDay 月度开始日期（1-28）
     * @return 是否在该记账月份内
     */
    fun isInAccountingMonth(date: Date, yearMonth: YearMonth, monthStartDay: Int): Boolean {
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return isInAccountingMonth(localDate, yearMonth, monthStartDay)
    }
    
    /**
     * 检查日期是否在指定的记账月份内
     * @param date 要检查的日期
     * @param yearMonth 记账月份
     * @param monthStartDay 月度开始日期（1-28）
     * @return 是否在该记账月份内
     */
    fun isInAccountingMonth(date: LocalDate, yearMonth: YearMonth, monthStartDay: Int): Boolean {
        val startDate = getMonthStartDate(yearMonth, monthStartDay)
        val endDate = getMonthEndDate(yearMonth, monthStartDay)
        
        return date >= startDate && date <= endDate
    }
}