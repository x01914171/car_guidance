package com.example.mypro

data class Point(val id:String, var x: Double, var y: Double)
data class Line(val id :String, val length:Float,val p1:Point,val p2:Point,val width: Float)
data class AllPoints(val id:String, val lineid:String, var x: Double, var y: Double, val width:Float)
data class Station(val pID :String,val lID:String,val length: Float,val width: Float)

data class Polygon(val id:String,val pois: ArrayList<Point>)
data class Corner(val ID :String,val lID:String,val pois1: ArrayList<Point>,val pois2: ArrayList<Point>)
