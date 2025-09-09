# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个大型车辆道路导航Android应用，主要功能包括：
- 基于Dijkstra算法的路径规划
- 考虑车辆尺寸（宽度、长度）的路径优化
- 转弯处碰撞检测
- 可视化地图显示和路径绘制
- 用户登录和注册功能

## 构建和运行命令

### 基本构建
```bash
# 构建项目
./gradlew build

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease

# 清理构建
./gradlew clean
```

### 运行和测试
```bash
# 运行单元测试
./gradlew test

# 运行Android仪器测试
./gradlew connectedAndroidTest

# 安装到设备
./gradlew installDebug
```

## 代码架构

### 核心组件
1. **MainActivity**: 主Activity，处理用户交互和路径查询逻辑
2. **LoginActivity**: 用户登录界面
3. **RegisterActivity**: 用户注册界面
4. **AnimationActivity**: 启动动画页面
5. **Graph系统**: 基于接口的图数据结构实现
   - `Graph<T, E>`: 图接口定义
   - `GraphImpl<T, E>`: 具体图实现
   - `dijkstra.kt`: Dijkstra最短路径算法实现
6. **数据模型** (cla.kt):
   - `Point`: 地图点坐标
   - `Line`: 道路线段（包含宽度信息）
   - `Corner`: 转弯处数据
   - `Polygon`: 多边形区域
7. **视图组件**:
   - `MyView`: 自定义地图渲染视图
   - `CornerJudge`: 转弯碰撞检测算法

### 数据加载
应用从assets文件夹读取地图数据：
- `points.txt`: 端点坐标
- `lines.txt`: 道路线段信息
- `allpoints.txt`: 所有节点信息
- `corner.txt`: 转弯处数据
- `polygon_pois.txt`: 多边形POI数据
- `roadname.txt`: 道路名称

### 路径规划算法
1. **标准模式**: 基于道路长度的最短路径
2. **车辆约束模式**: 考虑车辆尺寸，对宽度不足的道路设置无穷大权重

## 技术栈
- **语言**: Kotlin + Java
- **UI框架**: Android View System + Jetpack Compose
- **图形**: 自定义Canvas绘制
- **动画**: Lottie
- **架构**: MVC模式

## 开发注意事项

### 坐标系统
- 使用Double精度坐标系统
- 地图边界通过minx, maxx, miny, maxy动态计算

### 权重计算
车辆约束模式下的边权重公式：
```
权重 = (车辆宽度/道路宽度) * 500 + 道路长度
```

### 关键配置
- `minSdk = 30`
- `targetSdk = 35` 
- `compileSdk = 35`
- Java版本: 11

## 常见开发任务

修改路径规划算法请查看：
- `dijkstra.kt`: 核心算法实现
- `MainActivity.kt:119-162`: 路径查询逻辑

修改地图渲染请查看：
- `MyView.kt`: 地图绘制逻辑

修改数据模型请查看：
- `cla.kt`: 所有数据类定义