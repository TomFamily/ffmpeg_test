package com.example.opengl

import android.opengl.GLES20.*
import android.util.Log
import com.example.opengl.utils.LogU

object ShaderHelper {
    private const val tag = "ShaderHelper"
    fun compileVertexShader(shaderCode: String) = compileShader(GL_VERTEX_SHADER, shaderCode)

    fun compileFragmentShader(shaderCode: String) = compileShader(GL_FRAGMENT_SHADER, shaderCode)

    fun compileShader(type: Int, shaderCode: String): Int {
        // 创建着色器
        val shaderObjectId = glCreateShader(type)
        if (type == 0) {
            LogU.d(tag = tag, message = "Could not create new shader")
            return 0
        }
        // 传输并编译代码
        glShaderSource(shaderObjectId, shaderCode)
        glCompileShader(shaderObjectId)

        // 获得代码编译结果
        val compileStatus = IntArray(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)
        LogU.v(tag = tag, message ="Results of compiling source:" +
                "\n$shaderCode\n:${glGetShaderInfoLog(shaderObjectId)}")
        if (compileStatus[0] == 0) {
            // 编译失败，删除着色器
            glDeleteShader(shaderObjectId)
            LogU.w(tag, "Compilation of shader failed.")
            return 0
        }
        return shaderObjectId
    }

    /**
     * 将顶点着色器和片元着色器一起链接到 OpenGL 程序中
     */
    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programId = glCreateProgram()
        // 将顶点和片元着色器附加到程序中
        glAttachShader(programId, vertexShaderId)
        glAttachShader(programId, fragmentShaderId)
        // 链接着色器
        glLinkProgram(programId)
        val status = IntArray(1)
        glGetProgramiv(programId, GL_LINK_STATUS, status, 0)
        if (status[0] != GL_TRUE) {
            glDeleteProgram(programId)
            Log.d("program", "Results of link program: \n ${glGetProgramInfoLog(programId)}")
        }

        // 释放着色器
        glDeleteShader(vertexShaderId)
        glDeleteShader(fragmentShaderId)

        return programId
    }

    fun validateProgram(programObjectId: Int): Boolean {
        glValidateProgram(programObjectId)
        val validateStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
        LogU.v(tag, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId))
        return validateStatus[0] != 0
    }

}
