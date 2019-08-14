# SmallUpdateDemo

#### 介绍
Android安卓增量跟新

cd E:/Demo/ZLGX/bsdiff-4.3/bsdiff-4.3
bsdiff app1_0.apk app2_0.apk old-to-new.patch

参考地址
https://blog.csdn.net/lmj623565791/article/details/52761658
https://blog.csdn.net/myatlantis/article/details/52874227
https://blog.csdn.net/chunleixiahe/article/details/55666792

1.先解决差分包的生成
 1.1下载工具 cygwin
     记住下载插件的时候一定不要忘记 make bsdiff这两个是必须要有的，否则不能差分 还有binutils gcc gcc-mingw gdb，
     地址 https://blog.csdn.net/chunleixiahe/article/details/55666792

 1.2下载bsdiff 地址 http://www.daemonology.net/bsdiff/bsdiff-4.3.tar.gz

2.差分包的合并和安装 这些需要在app里面写
 2.1CMakeList.txt文件里面主要是指明引用到哪个.c文件和文件路径
 2.2bspatch.c文件里面提供具体方法
   JNIEXPORT jint JNICALL Java_com_small_app_BsPatchJNI_patch  可以看出来方法名是根据包名路径来的

3.需要把bzip里面的解压全部拷贝到cpp/bzip2目录下

4.gradle里面写好引用CMakeList.txt


5.必须写好权限配置，我就是权限没有配置，找错误找了一天的时间
6.项目整个文件目录格式
