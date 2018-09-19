//
// Created by lh on 2018/9/18.
//
#include "lh-jni.h"
#include "pthread.h"

typedef struct _Queue Queue;
//队列头文件： 定义队列的几个方法
//5.定义函数：分配 队列元素内存 的函数
typedef void* (*queue_fill_func)();
//6.定义函数：释放 队列中元素内存 的函数
typedef void* (*queue_free_func)(void* elem);

//1.初始化:分配内存，初始化索引值: 返回队列指针，参数：队列长度，自定义分配队列元素内存的函数
Queue* queue_init(int size,queue_fill_func fill_func);
//2.压入队列: 这里的压栈 最终的目的是赋值参数，移动下一个处理的索引（写）。返回值：队列元素指针，参数：要操作的队列的指针，互斥锁，条件变量
void* queue_push(Queue *queue,pthread_mutex_t *mutex,pthread_cond_t *cond);
//3.出队列: 这里的出栈 最终的目的是拿到队列的元素，移动下一个处理的索引（读）。返回值：队列元素指针，参数：要操作的队列的指针，互斥锁，条件变量
void* queue_pop(Queue *queue,pthread_mutex_t *mutex,pthread_cond_t *cond);
//4.销毁队列：释放队列内存: 队列元素的释放 交给自定义
void queue_free(Queue *queue,queue_free_func free_func);

//7.定义一个获取下一个处理索引的方法
int queue_get_next(Queue *queue,int current);




