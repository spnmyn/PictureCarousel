# PictureCarousel（图片轮播展示 暂不支持导航圆点联动）
所用控件：ImageSwitcher
---------
### 控件简介：<br>
        ImageSwitcher粗略的理解就是ImageView的选择器。是Android中控制图片展示效果的一个控件。例如幻灯片效果。
### 控件原理简介：
        ImageSwitcher有两个子View，即ImageView。当左右滑动的时候，就在这两个ImageView之间来回切换并显示图片。

        既然有两个子ImageView，那么我们要创建两个ImageView给ImageSwitcher。创建ImageViewSwitcher中的ImageView是通过ViewFactory工厂来实现的。