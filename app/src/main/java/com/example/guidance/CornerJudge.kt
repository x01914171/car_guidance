package com.example.guidance

import android.os.Debug
import android.util.Log
import org.jetbrains.annotations.TestOnly
import java.math.BigDecimal
import java.math.RoundingMode

class CornerJudge {

    companion object{
        /**
         * 主要工作方法
         * @param road1 ArrayList<Point> 道路线段1，作为移动基准
         * @param road2 ArrayList<Point> 道路线段2，作为判断条件
         * @param width Float   车辆宽度
         * @param length Float  车辆长度
         * @param dir Boolean   折点是否在判断点Y大于0的方向
         */
        fun work(road1: ArrayList<Point>,road2: ArrayList<Point>,width: Float,length: Float):Boolean {
            for (i in road1.indices) {
                var l1 = BL2xy84(doubleArrayOf(BigDecimal(road1[i].x.toString()).toDouble(), BigDecimal(road1[i].y.toString()).toDouble()))
                var p1 = Point(road1[i].id,l1[0],l1[1])
                road1.set(i, Point("-1",p1.y,p1.x))
            }
            for (i in road2.indices) {
                var l2 = BL2xy84(doubleArrayOf(BigDecimal(road2[i].x.toString()).toDouble(), BigDecimal(road2[i].y.toString()).toDouble()))
                var p2 = Point(road2[i].id,l2[0],l2[1])
                road2.set(i,Point("-1",p2.y,p2.x))
            }

            var init = road1[1]     // 基准线拐点
            var cross = road2[1]        // 判断线凸点，凸点在车内就是不通过

//            Log.i("距离",Math.sqrt(Math.pow((init.x-cross.x).toDouble(),2.0)+Math.pow((init.y-cross.y).toDouble(),2.0)).toString())

            // 保证线段从左到右
            if(road1[2].x<=road1[0].x && road1[2].y>=road1[0].y) {
                var a = road1[0];
                road1[0]= road1[2]
                road1[2]=a
            }


            // 计算两条道路斜率
            var k1 = ((road1[1].y-road1[0].y)/(road1[1].x-road1[0].x))
            var k2 = ((road1[2].y-road1[1].y)/(road1[2].x-road1[1].x))

            // 计算车辆起始点坐标(出发点)
            var init1:Point
            var init2:Point
            init1 = getLocation(road1[0],init,k1,length)
            // 计算车辆终止点坐标
            init2 = getLocation(road1[2], init, k2, length)
            // plt.scatter(initX1, initY1)

            var head = init
            var footer = init1
            var cunrrentLength=0.1
            var dir = cross.y>=init.y //折点是否在判断点Y大于0的方向
            // 开始遍历
            while(cunrrentLength<=length) {
                footer = getLocation(road1[0], init, k1, (length - cunrrentLength).toFloat())
                head = getLocation(road1[2], init, k2, cunrrentLength.toFloat())
                var pois2: Pair<Point, Point> = constructPolygon(footer, head, width, dir)
                var head2 = pois2.first
                var footer2 = pois2.second
                var pts = arrayListOf(head, head2, footer2, footer)
                if (isPoiWithinPoly(cross, arrayListOf(pts))) {
                    Log.i("isPoiWithinPoly 超出道路了", "不可通行")
                    return false
                }
                // return ;
                // 判断多边形是与线段否相交（逐个线判断相交）
                for (i in 0..3) {
                    var j = i + 1
                    var k = i
                    if (i == 3) {
                        k = 0
                        j = 1
                    }
                    if (Intersect(
                            Line("-1",0f, Point("-1",pts[k].x,pts[k].y), Point("-1",pts[j].x,pts[j].y),0f),
                            Line("-1",0f, Point("-1",road2[0].x,road2[0].y), Point("-1",road2[1].x,road2[1].y),0f),
                        )) {

                        Log.i("Intersect1超出道路了", "不可通行")
                        return false
                    }
                    if (Intersect(
                            Line("-1",0f, Point("-1",pts[k].x,pts[k].y), Point("-1",pts[j].x,pts[j].y),0f),
                            Line("-1",0f, Point("-1",road2[2].x,road2[2].y), Point("-1",road2[1].x,road2[1].y),0f),
                        )) {
                        Log.i("Intersect2超出道路了", "不可通行")
                        return false
                    }

                }
                cunrrentLength += 0.1
            }

            return true
        }

        /**
         * 构建车辆矩形
         * @param footer Point
         * @param head Point
         * @param width Float
         * @param dir Boolean
         * @return Pair<Point, Point>
         */
        private fun constructPolygon(footer: Point,head: Point,width: Float,dir: Boolean): Pair<Point, Point> {
            var dis: Pair<Double, Double>
            var dis2: Pair<Double, Double>
            var k = (head.y - footer.y) / (head.x - footer.x)
//     向着y>0方向增加
            if (dir) {
                if (-1 / k >= 0) {
                    dis = Pair(head.x + 2, head.y + 2 * (-1 / k))
                    dis2 = Pair(footer.x + 2, footer.y + 2 * (-1 / k))
                } else {
                    dis = Pair(head.x - 2, head.y - 2 * (-1 / k))
                    dis2 = Pair(footer.x - 2, footer.y - 2 * (-1 / k))
                }
            } else {
//        向着y < 0 方向增加
                if (-1 / k >= 0) {
                    dis = Pair(head.x - 2, head.y - 2 * (-1 / k))
                    dis2 = Pair(footer.x - 2, footer.y - 2 * (-1 / k))
                } else {
                    dis = Pair(head.x + 2, head.y + 2 * (-1 / k))
                    dis2 = Pair(footer.x + 2, footer.y + 2 * (-1 / k))
                }
            }
            var head2 = getLocation(Point("-1", dis.first, dis.second), head, -1 / k, width)
            var footer2 = getLocation(Point("-1", dis2.first, dis.second), footer, -1 / k, width)

            return Pair(head2, footer2)
        }

        /**
         * 判断一条线段上相隔距离length的点
         * @param point Point 道路外点
         * @param init Point 道路起点
         * @param k Float 所判断的道路斜率
         * @param length Float截取长度
         * @return Point 坐标点
         */
        private fun getLocation(point: Point, init: Point, k: Double, length: Float): Point {
            var initY1: Double
            var initX1: Double
            if (point.y >= init.y) {
                if (k.equals(0.0)) {
                    initY1 = init.y
                    if (point.x >= init.x)
                        initX1 = init.x + length
                    else
                        initX1 = init.x - length
                } else {
                    initY1 = (length / Math.sqrt((1 / (k * k) + 1).toDouble()) + init.y)
                    initX1 = (initY1 - init.y) / k + init.x
                }
            } else {
                initY1 = (-length / Math.sqrt((1 / (k * k) + 1).toDouble()) + init.y)
                initX1 = (initY1 - init.y) / k + init.x
            }
            return Point("-1", initX1, initY1)
        }


        /***
         * 经纬度转换成平面坐标
         * @param BL DoubleArray
         * @param L0 Double
         * @return DoubleArray
         */
        private fun BL2xy84(BL: DoubleArray): DoubleArray //经纬度转换成平面坐标
        {
            var L0 = 121.464467
            val xy = DoubleArray(2)
            val X: Double
            val B: Double
            val L: Double
            val a: Double
            val b: Double
            val e: Double
            val e1: Double
            val V: Double
            val c: Double
            val M: Double
            val N: Double
            val t: Double
            val n: Double
            val l: Double
            val x: Double
            val y: Double
            X = B2S(0.0, BL[0])
            B = BL[0] / 180 * Math.PI //纬度
            L = BL[1] / 180 * Math.PI //精度
            L0 = L0 / 180 * Math.PI
            a = 6378137.0 //WGS_84参考椭球参数
            // f=1/298.257223563;//椭球扁率
            b = 6356752.3142 //短轴
            e = Math.sqrt(a * a - b * b) / a
            e1 = Math.sqrt(a * a - b * b) / b
            V = Math.sqrt(1 + e1 * e1 * Math.cos(B) * Math.cos(B))
            c = a * a / b
            M = c / (V * V * V)
            N = c / V
            t = Math.tan(B)
            n = Math.sqrt(e1 * e1 * Math.cos(B) * Math.cos(B))
            l = L - L0
            x = X +
                    N * t * Math.cos(B) * Math.cos(B) * l * l *
                    (0.5 + (1.0 / 24) * (5 - t * t + 9 * n * n + 4 * n * n * n * n) * Math.cos(B) * Math.cos(B) * l * l + 1.0 / 720 * (61 - 58 * t * t + t * t * t * t) * Math.pow((Math.cos(B)), 4.0) * l * l * l * l);
            y =N * Math.cos(B) * l * (1 + 1.0 / 6 * (1 - t * t + n * n) * Math.cos(B) * Math.cos(B) * l * l + 1.0 / 120 * (5 - 18 * t * t + t * t * t * t + 14 * n * n - 58 * t * t * n * n) * Math.pow(Math.cos(B),4.0) * l * l * l * l)
            return doubleArrayOf(x, y)
        }

        //    // https://blog.csdn.net/pulian1508/article/details/120834185
        private fun B2S(B1: Double, B2: Double): Double //BL2xy84函数的内部一个函数
        {
            var B1 = B1
            var B2 = B2
            val a: Double
            val b: Double
            val e2: Double
            val A: Double
            val B: Double
            val C: Double
            val D: Double
            val E: Double
            val F: Double
            val G: Double
            val S: Double
            a = 6378137.0 //WGS_84参考椭球参数
            //f=1/298.257223563;%椭球扁率
            b = 6356752.3142 // 短轴
            e2 = (a * a - b * b) / (a * a)
            A = 1 + 3 * e2 / 4 + 45 * e2 * e2 / 64 + 175 * e2 * e2 * e2 / 256 + 11025 * Math.pow(
                e2,
                4.0
            ) / 16384 + 43659 * Math.pow(e2, 5.0) / 65536 + 693693 * Math.pow(e2, 6.0) / 1048576
            B = A - 1
            C = 15 * e2 * e2 * e2 / 32 + 175 * e2 * e2 * e2 / 384 + 3675 * Math.pow(
                e2,
                4.0
            ) / 8192 + 14553 * Math.pow(e2, 5.0) / 32768 + 231231 * Math.pow(e2, 6.0) / 524288
            D = 35 * e2 * e2 * e2 / 96 + 735 * Math.pow(e2, 4.0) / 2048 + 14553 * Math.pow(
                e2,
                5.0
            ) / 40960 + 231231 * Math.pow(e2, 6.0) / 655360
            E = Math.pow(e2, 4.0) * 315 / 1024 + Math.pow(e2, 5.0) * 6237 / 20480 + Math.pow(
                e2,
                6.0
            ) * 99099 / 327680
            F = 693 * Math.pow(e2, 5.0) / 2560 + 11011 * Math.pow(e2, 6.0) / 40960
            G = 1001 * Math.pow(e2, 6.0) / 4096
            B1 = B1 / 180 * Math.PI
            B2 = B2 / 180 * Math.PI
            S =
                a * (1 - e2) * (A * (B2 - B1) - B * (Math.sin(B2) * Math.cos(B2) - Math.sin(B1) * Math.cos(
                    B1
                )) - C * (Math.pow(
                    Math.sin(B2), 3.0
                ) * Math.cos(B2) - Math.pow(Math.sin(B1), 3.0) * Math.cos(B1)) - D * (Math.pow(
                    Math.sin(B2), 5.0
                ) * Math.cos(B2) - Math.pow(Math.sin(B1), 5.0) * Math.cos(B1)) - E * (Math.pow(
                    Math.sin(B2), 7.0
                ) * Math.cos(B2) - Math.pow(Math.sin(B1), 7.0) * Math.cos(B1)) - F * (Math.pow(
                    Math.sin(B2), 9.0
                ) * Math.cos(B2) - Math.pow(Math.sin(B1), 9.0) * Math.cos(B1)) - G * (Math.pow(
                    Math.sin(B2), 11.0
                ) * Math.cos(B2) - Math.pow(Math.sin(B1), 11.0) * Math.cos(B1)))
            return S
        }

        /**
         * 判断点在多边形内
         * @param poi Point 目标点
         * @param poly List<ArrayList<Point>>   多边形（可以为有孔多边形）
         * @return Boolean
         */
        private fun isPoiWithinPoly(poi: Point, poly: ArrayList<ArrayList<Point>>): Boolean {
            //输入：点，多边形三维数组
            //poly=[[[x1,y1],[x2,y2],……,[xn,yn],[x1,y1]],[[w1,t1],……[wk,tk]]] 三维数组
            var sinsc = 0 //交点个数
            var s_poi: Point
            var e_poi: Point
            for (epoly in poly) { //循环每条边的曲线->each polygon 是二维数组[[x1,y1],…[xn,yn]]
                for (i in IntRange(0, epoly.size-2)) { //[0,len-1]
                    s_poi = epoly[i]
                    e_poi = epoly[i + 1]
                    if (isRayIntersectsSegment(poi, s_poi, e_poi)) {
                        sinsc += 1 //有交点就加1}
                    }
                }
            }
            return if (sinsc % 2 == 1) true else false
        }

        private fun isRayIntersectsSegment(
            poi: Point,
            s_poi: Point,
            e_poi: Point
        ): Boolean { //[x,y] [lng,lat]{
            //输入：判断点，边起点，边终点，都是[lng,lat]格式数组
            if (s_poi.y == e_poi.y) { //排除与射线平行、重合，线段首尾端点重合的情况
                return false
            }
            if (s_poi.y > poi.y && e_poi.y > poi.y) { //线段在射线上边
                return false
            }
            if (s_poi.y < poi.y && e_poi.y < poi.y) { //线段在射线下边
                return false
            }
            if (s_poi.y == poi.y && e_poi.y > poi.y) { //交点为下端点，对应spoint
                return false
            }
            if (e_poi.y == poi.y && s_poi.y > poi.y) { //交点为下端点，对应epoint
                return false
            }
            if (s_poi.y < poi.y && e_poi.y < poi.y) { //线段在射线左边
                return false
            }
            var xseg = e_poi.y - (e_poi.y - s_poi.y) * (e_poi.y - poi.y) / (e_poi.y - s_poi.y) //求交
            if (xseg < poi.y) //交点在射线起点的左侧
                return false
            return true  //排除上述情况之后
        }

        /**
         * 判断线段相交
         * @param l1 Line
         * @param l2 Line
         * @return Boolean
         */
        private fun Intersect(l1: Line, l2: Line): Boolean {
//         l1 [xa, ya, xb, yb]   l2 [xa, ya, xb, yb]
            var v1 = Pair(l1.p1.x - l2.p1.x, l1.p1.y - l2.p1.y)
            var v2 = Pair(l1.p1.x - l2.p2.x, l1.p1.y - l2.p2.y)
            var v0 = Pair(l1.p1.x - l1.p2.x, l1.p1.y - l1.p2.y)
            var a = v0.first * v1.second - v0.second * v1.first
            var b = v0.first * v2.second - v0.second * v2.first

            var temp = l1
            var l1 = l2
            var l2 = temp
            v1 = Pair(l1.p1.x - l2.p1.x, l1.p1.y - l2.p1.y)
            v2 = Pair(l1.p1.x - l2.p2.x, l1.p1.y - l2.p2.y)
            v0 = Pair(l1.p1.x - l1.p2.x, l1.p1.y - l1.p2.y)
            var c = v0.first * v1.second - v0.second * v1.first
            var d = v0.first * v2.second - v0.second * v2.first

            return a * b < 0 && c * d < 0
        }

    }


}