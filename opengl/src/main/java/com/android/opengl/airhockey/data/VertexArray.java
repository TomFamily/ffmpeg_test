package com.android.opengl.airhockey.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import com.android.opengl.airhockey.Constants;

/**
 * Created by pengfu on 19/05/2017.
 */

public class VertexArray {

    private final FloatBuffer floatBuffer ;



    public VertexArray(float[] vertexData){
        floatBuffer = ByteBuffer.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData) ;
    }

    public void setVertexAttribPointer(int dataOffset ,int attributeLocation ,int componentCount ,int stride){
        floatBuffer.position(dataOffset) ;
        glVertexAttribPointer(attributeLocation ,componentCount ,GL_FLOAT ,false ,stride ,floatBuffer) ;
        glEnableVertexAttribArray(attributeLocation);
        floatBuffer.position(0) ;
    }
}
