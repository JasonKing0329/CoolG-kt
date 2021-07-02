package com.king.app.coolg_kt.view

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.widget.DatePicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/9/30 10:33
 */
class DateManager {
    private var nYearStart = 0
    private var nMonthStart = 0
    private var nDayStart = 0
    private var mDate: Date? = null

    var dateStr: String? = null

    var dateFormat = SimpleDateFormat("yyyy-MM-dd")

    var date: Date?
        get() = mDate
        set(date) {
            mDate = date
            dateStr = dateFormat.format(date)
        }

    fun pickDate(context: Context, listener: OnDateListener?) {
        if (nYearStart == 0) {
            val calendar = Calendar.getInstance()
            nYearStart = calendar[Calendar.YEAR]
            nMonthStart = calendar[Calendar.MONTH]
            nDayStart = calendar[Calendar.DAY_OF_MONTH]
        }
        val startDlg = DatePickerDialog(
            context,
            OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                nYearStart = year
                nMonthStart = monthOfYear //日期控件的月份是从0开始编号的
                nDayStart = dayOfMonth
                val buffer = StringBuffer()
                buffer.append(nYearStart).append("-")
                buffer.append(if (nMonthStart + 1 < 10) "0" + (nMonthStart + 1) else nMonthStart + 1)
                    .append("-")
                buffer.append(if (nDayStart < 10) "0$nDayStart" else nDayStart)
                dateStr = buffer.toString()
                try {
                    mDate = dateFormat.parse(dateStr)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                listener?.onDateSet()
            }, nYearStart, nMonthStart, nDayStart
        )
        startDlg.show()
    }

    fun reset() {
        mDate = null
        dateStr = null
        nDayStart = 0
        nMonthStart = 0
        nYearStart = 0
    }

    interface OnDateListener {
        fun onDateSet()
    }
}