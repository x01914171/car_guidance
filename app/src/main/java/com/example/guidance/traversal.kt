package com.example.guidance

// 图遍历相关的工具函数
class GraphTraversal {
    companion object {
        fun <T> dfs(graph: Graph<T, *>, start: T, visited: MutableSet<T> = mutableSetOf()): List<T> {
            if (start in visited) return emptyList()
            
            visited.add(start)
            val result = mutableListOf(start)
            
            graph.adjacentVertices(start).forEach { neighbor ->
                result.addAll(dfs(graph, neighbor, visited))
            }
            
            return result
        }
        
        fun <T> bfs(graph: Graph<T, *>, start: T): List<T> {
            val visited = mutableSetOf<T>()
            val queue = mutableListOf(start)
            val result = mutableListOf<T>()
            
            while (queue.isNotEmpty()) {
                val current = queue.removeAt(0)
                if (current !in visited) {
                    visited.add(current)
                    result.add(current)
                    queue.addAll(graph.adjacentVertices(current) - visited)
                }
            }
            
            return result
        }
    }
}