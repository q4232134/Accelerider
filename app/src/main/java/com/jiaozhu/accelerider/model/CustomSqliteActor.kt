package com.jiaozhu.accelerider.model

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import com.alibaba.fastjson.JSON
import zlc.season.rxdownload3.core.Mission
import zlc.season.rxdownload3.core.RealMission
import zlc.season.rxdownload3.database.SQLiteActor


class CustomSqliteActor(context: Context) : SQLiteActor(context) {
    private val MODEL = "file_model"

    override fun provideCreateSql(): String {
        return """
            CREATE TABLE $TABLE_NAME (
                $TAG TEXT PRIMARY KEY NOT NULL,
                $URL TEXT NOT NULL,
                $SAVE_NAME TEXT,
                $SAVE_PATH TEXT,
                $RANGE_FLAG INTEGER,
                $CURRENT_SIZE TEXT,
                $TOTAL_SIZE TEXT,
                $STATUS_FLAG INTEGER,
                $MODEL TEXT)
            """
    }

//    override fun delete(mission: RealMission) {
//        val actual = mission.actual
//        val writableDatabase = sqLiteOpenHelper.writableDatabase
//        writableDatabase.delete(TABLE_NAME, "$TAG=?", arrayOf(actual.tag))
//    }

    override fun onCreate(mission: RealMission): ContentValues {
        val cv = super.onCreate(mission)
        if (mission.actual is Task) {
            val actualMission = mission.actual as Task
            cv.put(MODEL, JSON.toJSONString(actualMission.model))
        }
        return cv
    }

    override fun onGetAllMission(cursor: Cursor): Mission {
        val mission = super.onGetAllMission(cursor)
        var model = cursor.getString(cursor.getColumnIndexOrThrow(MODEL))?.let { JSON.parseObject(it, FileModel::class.java) }
        return Task(model!!, mission)
    }
}