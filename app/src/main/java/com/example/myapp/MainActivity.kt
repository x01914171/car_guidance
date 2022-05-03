package com.example.myapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.mypro.*
import shortestPath
import java.io.*
import GraphImpl
import android.util.Log


//allpoints.txt :点ID，线ID，宽度，X,Y (道路中心线)
//lines.txt  端点ID1，端点ID2，宽度
//points.txt 端点ID，X,Y
//polygon_pois.txt 点id 多边形id X,Y (道路多边形)
//
@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {
    //定义控件
    lateinit var button:Button
    lateinit var myView:MyView
    lateinit var textView:TextView
    lateinit var carw:TextView
    lateinit var carl:TextView
    lateinit var switch: Switch
    //目标点id
    var firstPoint:Point= Point("0",0.0,0.0)
    var lastPoint: Point=Point("0",0.0,0.0)

    var minx = Double.MAX_VALUE
    var maxx = Double.MIN_VALUE
    var miny = Double.MAX_VALUE
    var maxy = Double.MIN_VALUE

    private var stations:ArrayList<Station> = ArrayList()
    private var point:ArrayList<String> = ArrayList() //端点
    private var points:ArrayList<Point> = ArrayList()
    private var line:ArrayList<String> = ArrayList() //线
    private var lines:ArrayList<Line> = ArrayList()
    private var allpoint:ArrayList<String> = ArrayList()//结点
    private var allpoints:ArrayList<AllPoints> = ArrayList()
    private var polygon:ArrayList<String> = ArrayList();
    private var polygons:ArrayList<Polygon> = ArrayList();
    private var corner:ArrayList<String> = ArrayList()//转弯处
    private var corners:ArrayList<Corner> = ArrayList()


    private var carwidth =0.0f
    private var carlength =0.0f
    var graph:GraphImpl<Point,Float> = GraphImpl(false,5.0f) //图
    var graph2:GraphImpl<Point,Float> = GraphImpl(false,5.0f) //图
    var path = shortestPath(graph,from = Point("0",0.0,0.0),destination = Point("0",0.0,0.0))

    //初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        //绑定控件
        button = findViewById(R.id.button)
        myView = findViewById(R.id.view)
        textView = findViewById(R.id.textView)
        carw = findViewById(R.id.editTextNumberSigned)
        carl = findViewById(R.id.editTextNumberSigned2)
        switch = findViewById(R.id.switch1)
        switch.isChecked = true

    }
    //加载数据，地图
    override fun onStart() {
        super.onStart()
        initMap()
    }
    //用户交互
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        button.setOnClickListener {
            when(button.text){
                "确定" -> {
                    myView.visibility = View.VISIBLE
                    myView.flagAimPoint = true
                    button.text = "查询"
                    switch.isVisible=true
                }
                "查询" -> {
                    if(carw.text.isEmpty()){
                        carwidth=1f
                        carw.text="1"
                    }else{
                        carwidth = carw.text.toString().toFloat()
                    }
                    if(carl.text.isEmpty()){
                        carlength=1f
                        carl.text="1"
                    }else{
                        carlength = carl.text.toString().toFloat()
                    }

                    val aimList = myView.aimList
                    if (aimList.count()<2){
                        Toast.makeText(this, "请选择两个点", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        //获取起始点、末尾点号
                        firstPoint = aimList[0]
                        lastPoint = aimList[1]


                        if (!switch.isChecked){
                            //图
                            path = shortestPath(graph,from = firstPoint,destination = lastPoint)
                            if(path.first.size!=0 && path.second<Float.MAX_VALUE){
                                myView.pathr = path.first as ArrayList<Point>
                                myView.invalidate()
                                textView.text= "出发点ID：${firstPoint.id}\n目的点ID：${lastPoint.id}\n"
                                Toast.makeText(this, "查询成功", Toast.LENGTH_LONG).show()
                            }else{
                                textView.text= "不连通"
                                Toast.makeText(this,"不连通",Toast.LENGTH_LONG).show()
                            }
                        } else {
                            graph2= GraphImpl(false,5.0f) //图
                            for(i in points){
                                graph2.addVertex(i)
                            }
                            for (i in lines){
                                var width =carwidth/i.width*500+i.length    //权重计算
                                var corner_i = findcorner(i.id) //判断是否为拐点
                                if(corner_i!=-1){
                                    if(cornerCollesion(corners[corner_i])){
                                        width=Float.MAX_VALUE;
                                    }
                                }
                                if (carwidth<i.width){
                                    graph2.addArc(Pair(i.p1,i.p2),width)
                                }else{
                                    graph2.addArc(Pair(i.p1,i.p2),Float.MAX_VALUE)
                                }
                            }
                            path = shortestPath(graph2,from = firstPoint,destination = lastPoint)
                            if(path.first.size!=0 && path.second<Float.MAX_VALUE){
                                myView.pathr = path.first as ArrayList<Point>
                                myView.invalidate()
                                textView.text="出发点ID：${firstPoint.id}\n目的点ID：${lastPoint.id}\n"
                                Toast.makeText(this, "查询成功", Toast.LENGTH_LONG).show()
                            }else{
                                textView.text= "不连通"
                                Toast.makeText(this,"不连通",Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 初始化地图
     */
    fun initMap(){
        point = readfile("points.txt")
        line =  readfile("lines.txt")
        allpoint = readfile("allpoints.txt")
        corner = readfile("corner.txt")
        polygon = readfile("polygon_pois.txt")

        var text = readfile("roadname.txt")

        for (index in 0..allpoint.size-6 step 6){
            allpoints.add(AllPoints(allpoint[index],allpoint[index+1],allpoint[index+2].toDouble(),
                allpoint[index+3].replace("\r","").toDouble(),allpoint[index +5].toFloat()))
        }
        for (index in 0..point.size-3 step 3){
            points.add(Point(point[index],point[index+1].toDouble(),point[index+2].toDouble()))
        }
        for (index in 0..line.size-4 step 4){
            stations.add(Station(line[index],line[index+1],line[index+2].toFloat(),line[index+3].toFloat()))
        }
        for (index in 1..stations.size-1){
            if (stations[index].lID==stations[index-1].lID){
                lines.add(Line(stations[index].lID,stations[index].length,
                    points[findpoint(stations[index].pID,points)],
                    points[findpoint(stations[index-1].pID,points)],
                    stations[index].width))
            }
        }

        var polygon1:ArrayList<Point> = ArrayList()
        var pid = polygon[1]
        for (index in 0..polygon.size-4 step 4){
            if(polygon[index+1]==pid){
                polygon1.add(Point(polygon[index],polygon[index+2].toDouble(),polygon[index+3].toDouble()))
            }else
            {
                polygons.add(Polygon(pid,polygon1))
                polygon1= ArrayList()
                polygon1.add(Point(polygon[index],polygon[index+2].toDouble(),polygon[index+3].toDouble()))
                pid = polygon[index+1];
            }
        }

//寻找拐角
        var lid =corner[1]
        var pois1 = ArrayList<Point>();
        var pois2 = ArrayList<Point>();
        for (index in 0..corner.size-5 step 5){
            if(corner[index+1]==lid){
                if (corner[index+2]=="1" ){
                    pois1.add(Point("-1",corner[index+3].toDouble(),corner[index+4].toDouble()))
                }else{
                    pois2.add(Point("-1",corner[index+3].toDouble(),corner[index+4].toDouble()))
                }
            }else{
                corners.add(Corner(corner[index],lid,pois1,pois2))
                lid=corner[index+1]
                pois1 = ArrayList();
                pois2 = ArrayList();
            }
        }

        //图的构建
        for(i in points){
            graph.addVertex(i)
        }
        for (i in lines){
            graph.addArc(Pair(i.p1,i.p2),i.length)
        }
        //获取地图范围
        for (pt in allpoints) {
            val x0 = pt.x
            val y0 = pt.y
            minx = if (x0>minx) minx else x0
            miny = if (y0>miny) miny else y0
            maxx = if (x0<maxx) maxx else x0
            maxy = if (y0<maxy) maxy else y0
        }


        //传输数据
        myView.text=text
        myView.minx = minx
        myView.maxx = maxx
        myView.miny = miny
        myView.maxy = maxy
        myView.points=points
        myView.lines = lines
        myView.allPointsList = allpoints
        myView.polygons=polygons
//        view不可见
        myView.visibility = View.INVISIBLE
        myView.flagAimPoint = true
        myView.invalidate()
    }

    /**
     * 判断是否过拐角
     * @param corner Corner
     * @return Boolean
     */
    fun cornerCollesion(corner: Corner):Boolean{
        var flag:Boolean=true   //可以通行
        for( i in 1..corner.pois1.size-2){
            for( j in 1..corner.pois2.size - 2){
                flag = CornerJudge.work(arrayListOf(corner.pois1[i - 1], corner.pois1[i], corner.pois1[i + 1]),
                    arrayListOf(corner.pois2[j - 1], corner.pois2[j], corner.pois2[j + 1]), carwidth, carlength)
                println(flag)
                if(!flag){
                    return false
                }
            }
        }
        return true
    }

    /***
     * 读取数据文件
     * @param filename String
     * @return ArrayList<String>
     */
    fun readfile(filename:String): ArrayList<String> {
        var string= ""
        try {
            val inputStream: InputStream = assets.open(filename)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            string = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return java.util.ArrayList(string.split("\n",",") )
    }

    fun findpoint(id:String,p:ArrayList<Point>):Int{
        for(i in 0..p.size-1){
            if (p[i].id == id ) return i
        }
        return -1
    }
    fun findcorner(id:String):Int{
        for(i in 0..corners.size-1){
            if (corners[i].lID == id ) return i
        }
        return -1
    }
}