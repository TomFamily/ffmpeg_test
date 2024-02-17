package com.example.opengl

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.example.opengl.utils.isDebugVersion
import com.example.opengl.utils.readStringFromRaw
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AirHockeyRenderer(val context: Context, private val vertexData: FloatBuffer) : GLSurfaceView.Renderer {

    private var programId = 0

    /**
     * 缓存u_Color变量的位置
     */
    private var uColorLocation = 0
    private var aPositionLocation = 0
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 设置ClearColor
        glClearColor(0F, 0F, 0F, 0F)
        // 读取glsl代码
        val vertexShaderCode = context.readStringFromRaw(R.raw.simple_vertex_shader)
        val fragmentShaderCode = context.readStringFromRaw(R.raw.simple_fragment_shader)
        // 创建着色器，这里未验证为0的情况
        val vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode)
        val fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderCode)
        // 链接着色器，创建程序
        programId = ShaderHelper.linkProgram(vertexShader, fragmentShader)
        // 在Debug状态下验证程序
        if (context.isDebugVersion()) {
            ShaderHelper.validateProgram(programId)
        }
        // 显式声明运行该程序，并且尝试找出两个变量的位置
        glUseProgram(programId)
        uColorLocation = glGetUniformLocation(programId, U_COLOR)
        aPositionLocation = glGetAttribLocation(programId, A_POSITION)

        // 也可以调用flip来变为读取模式
        vertexData.position(0)
        // 传输顶点数组
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
            GL_FLOAT, false, 0, vertexData)
        // 启用属性
        glEnableVertexAttribArray(aPositionLocation)
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl?.glViewport(0,0,width, height)

        val vertexShaderCode = context.readStringFromRaw(R.raw.simple_vertex_shader)
        val fragmentShaderCode = context.readStringFromRaw(R.raw.simple_fragment_shader)

        val vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode)
        val fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderCode)

        programId = ShaderHelper.linkProgram(vertexShader, fragmentShader)
    }

    override fun onDrawFrame(gl: GL10?) {
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT)

        glUniform4f(uColorLocation, 1F, 1F, 1F, 1F)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        glUniform4f(uColorLocation, 1F, 0F, 0F, 1F)
        glDrawArrays(GL_LINES, 6, 2)

        glUniform4f(uColorLocation, 0F, 0F, 1F, 1F)
        glDrawArrays(GL_POINTS, 8, 1)

        glUniform4f(uColorLocation, 1F, 0F, 0F, 1F)
        glDrawArrays(GL_POINTS, 9, 1)
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val BYTES_PER_FLOAT = 4
        private const val U_COLOR = "u_Color"
        private const val A_POSITION = "a_Position"
    }
}
