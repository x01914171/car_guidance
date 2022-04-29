package com.example.myapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.mypro.AllPoints
import com.example.mypro.Line
import com.example.mypro.Point
import com.example.mypro.Polygon
import kotlin.math.sqrt

class MyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    //原始点线数组
    var points: ArrayList<Point> = ArrayList()
    var lines: ArrayList<Line> = ArrayList()
    var pathr:ArrayList<Point> = ArrayList()
    var allPointsList: ArrayList<AllPoints> = ArrayList()
    var polygons:ArrayList<Polygon> = ArrayList()
    //目标点
    var aimList:Array<Point> = arrayOf(Point("0",0.0,0.0), Point("0",0.0,0.0))
    //设置转换参数
    var minx=0.0
    var maxx=0.0
    var miny=0.0
    var maxy=0.0
    var x0 = 0.0
    var y0 = 0.0
    var x1 = 0.0
    var y1 = 0.0
    //设置标志变量
    var flagAimPoint = false
    var flag = false
    //定义画笔
    private val paint = Paint()
    //设置变换参数
    var oldEventX = 0f
    var oldEventY = 0f
    var newEventX = 0f
    var newEventY = 0f
    var oldDist = 0f
    var newDist = 0f
    var operate = 0
    var index = 0

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //初始化地图
        paint.setAntiAlias(true)


        for (i in polygons){
            val path = Path()
            for (j in 0..i.pois.size-1){
                x0 = i.pois[j].x
                y0 = i.pois[j].y
                val p0 = xy2WH(x0, y0)
                if(j==0){
                    path.moveTo(p0[0].toFloat(), p0[1].toFloat())
                }else{
                    path.lineTo(p0[0].toFloat(), p0[1].toFloat())
                }
            }
            paint.setStyle(Paint.Style.FILL);
            path.close()
            paint.strokeWidth = 5f
            paint.color = Color.GREEN
            canvas?.drawPath(path, paint);
        }

        paint.strokeWidth = 5f
        paint.color = Color.BLACK
        for (index in 1..allPointsList.size -1) {
            if (allPointsList[index].lineid == allPointsList[index-1].lineid){
                x0 = allPointsList[index].x
                y0 = allPointsList[index].y
                val p0 = xy2WH(x0, y0)
                x1 = allPointsList[index-1].x
                y1 = allPointsList[index-1].y
                val p1 = xy2WH(x1, y1)
                when {
                    allPointsList[index].width<=5 -> {
                        paint.setARGB(255,200,200,200)
                    }
                    allPointsList[index].width<=10 -> {
                        paint.setARGB(255,100,100,100)
                    }
                    else -> {
                        paint.setARGB(255,255,215,0)
                    }
                }
                canvas?.drawLine(p0[0].toFloat(), p0[1].toFloat(), p1[0].toFloat(), p1[1].toFloat(), paint)
            }
            else{
                continue
            }
        }

        //显示初始点
        if (flagAimPoint){
            x0 = aimList[0].x
            y0 = aimList[0].y
            x1 = aimList[1].x
            y1 = aimList[1].y
            val p0 = xy2WH(x0,y0)
            val p1 = xy2WH(x1,y1)
            paint.color=Color.RED
            canvas?.drawCircle(p0[0].toFloat(),p0[1].toFloat(),10f,paint)
            canvas?.drawCircle(p1[0].toFloat(),p1[1].toFloat(),10f,paint)
        }
        //画最短路径
        if (pathr.size!=0 ){
            paint.setColor(Color.RED)
            paint.strokeWidth=5f
            for (index in 1..pathr.size-1){
                x0 = pathr[index].x
                y0 = pathr[index].y
                val p0 = xy2WH(x0, y0)
                x1 = pathr[index-1].x
                y1 = pathr[index-1].y
                val p1 = xy2WH(x1, y1)
                canvas?.drawLine(p0[0].toFloat(), p0[1].toFloat(), p1[0].toFloat(), p1[1].toFloat(),paint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (flag) return true
        if (event?.pointerCount!! >=1){
            when(event.action.and(event.actionMasked)){
                //手指按下，记录按下坐标
                MotionEvent.ACTION_DOWN -> {
                    Log.i("test","按下")
                    oldEventX = event.x
                    oldEventY = event.y
                }
                //手指抬起，记录抬起坐标
                MotionEvent.ACTION_UP -> {
                    Log.i("test","抬起")
                    newEventX = event.x
                    newEventY = event.y
                    //判断是否进行放缩
                    if (oldEventX != -1f){
                        if(distance(oldEventX,oldEventY,newEventX,newEventY)<3f) {
                            var min = Float.MAX_VALUE
                            val loc = WH2xy(oldEventX.toDouble(), oldEventY.toDouble())
                            for (i in 0..points.size - 1) {
                                val dis = (points[i].x - loc[0]) * (points[i].x - loc[0]) +
                                        (points[i].y - loc[1]) * (points[i].y - loc[1])
                                if (dis < min) {
                                    min = dis.toFloat()
                                    index = i
                                }
                            }
                            if (operate == 0) {
                                aimList[0] = points[index]
                                operate += 1
                            } else if (operate == 1) {
                                aimList[1] = points[index]
                                operate -= 1

                            }
                            invalidate()
                        }
                        else{
                            //进行平移
                            translat(oldEventX,oldEventY,newEventX,newEventY)
                            invalidate()
                        }
                    }
                }
                //屏幕上有手指时，按下屏幕
                MotionEvent.ACTION_POINTER_DOWN -> {
                    Log.i("test","再次按下")
                    oldEventX = -1f
                    //记录起始两点距离
                    oldDist = spacing(event)
                }
                //屏幕上有手指时，抬起手指
                MotionEvent.ACTION_POINTER_UP -> {
                    Log.i("test","抬起一下")
                    //记录手指离开时两点距离
                    newDist = spacing(event)
                    //判断放缩
                    if (newDist > oldDist + 1){
                        zoom(newDist/oldDist,event)
                        invalidate()
                    }
                    if (newDist < oldDist - 1){
                        zoom(newDist/oldDist,event)
                        invalidate()
                    }
                }
            }
        }
        return true
    }
    //放缩
    fun zoom(scale:Float,event: MotionEvent){
        //计算放缩中心点
        val ox = (event.getX(0) + event.getX(1))/2
        val oy = (event.getY(0) + event.getY(1))/2
        //将中心点转换到原点 按比例放缩 换回坐标
        for (pt in allPointsList){
            pt.x = ((pt.x-WH2xy(ox.toDouble(),oy.toDouble())[0])*scale+WH2xy(ox.toDouble(),oy.toDouble())[0])
            pt.y = ((pt.y-WH2xy(ox.toDouble(),oy.toDouble())[1])*scale+WH2xy(ox.toDouble(),oy.toDouble())[1])
        }
        for (pt in points){
            pt.x = ((pt.x-WH2xy(ox.toDouble(),oy.toDouble())[0])*scale+WH2xy(ox.toDouble(),oy.toDouble())[0])
            pt.y = ((pt.y-WH2xy(ox.toDouble(),oy.toDouble())[1])*scale+WH2xy(ox.toDouble(),oy.toDouble())[1])
        }
    }
    //平移
    fun translat(x0: Float,y0: Float,x1: Float,y1: Float){
        val p1 = WH2xy(x0.toDouble(),y0.toDouble())
        val p2 = WH2xy(x1.toDouble(),y1.toDouble())
        for (pt in allPointsList){
            pt.x = pt.x+p2[0]-p1[0]
            pt.y = pt.y+p2[1]-p1[1]
        }
        for (pt in points){
            pt.x = pt.x+p2[0]-p1[0]
            pt.y = pt.y+p2[1]-p1[1]
        }
    }
    //计算屏幕上两接触点之间的距离
    fun spacing(event: MotionEvent):Float{
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x*x+y*y)
    }
    //计算两点之间的距离
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }
    //坐标转换
    fun xy2WH(x: Double, y: Double): List<Double> {
        val X = (x - minx) / (maxx - minx) * (this.width-100)+50f
        val Y = (this.width-100) * (1 - (y - miny) / (maxy - miny))+50f
        return listOf(X, Y)
    }

    fun WH2xy(x: Double,y: Double):List<Double>{
        val X = (x-50f)/(this.width-100)*(maxx-minx)+minx
        val Y = -((y-50f)/(this.width-100)*(maxy-miny)-maxy)
        return listOf(X,Y)
    }
}