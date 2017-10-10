package com.jiaozhu.accelerider.model

import com.jiaozhu.accelerider.commonTools.SingerPicker

/**
 * Created by jiaozhu on 2017/10/10.
 */
class UserModel(var Name: String = "", var Uk: String = "") : SingerPicker.Description {
    override fun description(): String {
        return Name
    }
}