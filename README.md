# **OpenGLESPro**


这是一个`Android`上的`OpenGL ES` 高级进阶学习项目，我会不断地补充sample，并在关键代码处附上中英文注释。

This is a `OpenGL ES` advanced learning project for Android, and I will continue to code and commit samples and write chinese and english commets for some import code.



在我的**掘金**上，我会发布相关的**OpenGL ES 高级进阶**教学文章，包括每个例子的对应的讲解文章，以及一些其它知识，欢迎关注：https://juejin.im/user/5afabe81f265da0b7b361036/posts

I will publish some Android OpenGL ES advanced learning articles on my **juejin**, including the articles related to every samples in this project, and other knowledge.  Welcome! Link: https://juejin.im/user/5afabe81f265da0b7b361036/posts



目前工程中有6个例子:

Now, this project contains 5 samples.



- **SampleShader**

   一个演示OpenGL 3.0 shader的例子，主要演示其中的location字段的作用

    A sample demonstrates the usage of location in OpenGL 3.0 shader.

   [https://juejin.im/post/5ca9863151882543f400b745](https://juejin.im/post/5ca9863151882543f400b745)

   

- **SampleTextureArray**

  一个纹理数组的例子，通过使用sampler2DArray将一组纹理传给fragment shader

  A sample demonstrates the usage of texture array. In the fragment shader, we use sampler2DArray to hold an array of texture.

  https://juejin.im/post/5ce0aca6e51d4510a37bab4b

  

- **SampleBinaryProgram**

  一个使用二进制GL program的例子，演示将link好的GL Program以文件的方式保存，以及读取GL program文件并加载

  A sample demonstrates the usage of binary GL program. We can save the linked GL program to file and load a binary GL program from file.

  

- **SampleFenceSync**

  一个使用栅栏做GL命令同步的例子

  a sample of using fence to synchronize the GL commands.

  

- **SampleMultiRenderTarget**

  一个多渲染目标的例子，可以一次渲染到多个纹理上

  This is a sample of multiple render targets, with which we can render to multiple textures at a time.

  

- **SampleVBOAndIBO**

  VBO(Vertex Buffer Object)和IBO(Index Buffer Object)的使用方法

  This sample demonstrates the usage of VBO(Vertex Buffer Object) and IBO(Index Buffer Object)。

  https://juejin.im/post/5ce55ae95188253114078ad7


