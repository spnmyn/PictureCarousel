# PictureCarousel
图片自动轮播展示，没有导航圆点。
=========
所用控件：ImageSwitcher
---------
### 控件简介：<br>
        ImageSwitcher粗略的理解就是ImageView的选择器。是Android中控制图片展示效果的一个控件。例如幻灯片效果。
### 控件原理简介：
        ImageSwitcher有两个子View，即ImageView。当左右滑动的时候，就在这两个ImageView之间来回切换并显示图片。

        既然有两个子ImageView，那么我们要创建两个ImageView给ImageSwitcher。创建ImageViewSwitcher中的ImageView是通过ViewFactory工厂来实现的。
### 问题说明：
使用华为V9手机，安卓7.0系统。运行过程中，有两张轮播图发生互跳现象。问题尚未解决。
