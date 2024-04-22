package com.example.pc_client.hilt

import javax.inject.Inject

class Person @Inject constructor() {

    var name: String = "张三"
    var age: Int = 20

    override fun toString(): String {
        return "姓名:$name  年龄:$age"
    }
}