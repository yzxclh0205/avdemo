//
// Created by lh on 2018/9/18.
//
#include "queue.h"

struct _Queue{
    int size;//队列长度
    //push或者pop元素时 需要按照先后顺序，一次进行
    int next_to_write;
    int next_to_read;
    int *ready;
    //任意类型的数组指针，这里每一个元素都是AVPacket的指针，总共有size个
    void** tab;//AVPacket** packets;
};

/*
 * 初始化队列
 */
Queue * queue_init(int size, queue_fill_func fill_func){
    Queue* queue = (Queue*)malloc(sizeof(Queue));//动态分配内存空间
    queue->size = size;
    queue->next_to_read = 0;
    queue->next_to_write = 0;
    queue->tab = malloc(sizeof(*queue->tab)*size);//第一个指针 的值的内存大小 * size 得到整个一维数组的内存空间大小
    for(int i =0;i<size;i++){
        queue->tab[i] = fill_func();//通过回调的方式给指针数组的 每一个队列元素 分配内存空间
    }
    return queue;
}

int queue_get_next(Queue *queue,int current){
    return (current+1)% queue->size;
}
/*
 * 2.压栈: 这里的压栈 最终的目的是赋值参数，移动下一个处理的索引（写）。返回值：队列元素指针，参数：要操作的队列的指针，互斥锁，条件变量
 */
void* queue_push(Queue *queue,pthread_mutex_t *mutex,pthread_cond_t *cond){
    int current;
    int next_to_write;
    for(;;){//while循环 控制惊群效应，只允许一个线程获得锁进行操作
        current = queue->next_to_write;//------------------这里使得每一帧数据都一次往后写
        next_to_write = queue_get_next(queue, current);
        //这里处理的方式是，下一个要读的位置等于下一个要写的位置是，等我写完，再读
        //不等于就继续；反过来就是。下一个要读的位置不等于要写的位置，就可以继续写
        LOGI("queue_push queue：%#x当前要写的：%d 下一个要读的%d",queue,current,queue->next_to_read);
        if(queue->next_to_read !=next_to_write){
            break;
        }
        //挂起，阻塞
        pthread_cond_wait(cond,mutex);
    }
    queue->next_to_write = next_to_write;
    LOGI("queue_push queue:%#x, %d",queue,current);
    //唤醒其他 休眠的线程（因为条件变量阻塞的）
    pthread_cond_broadcast(cond);
    return queue->tab[current];
}
/*
 * 3.出队列: 这里的出栈 最终的目的是拿到队列的元素，移动下一个处理的索引（读）。返回值：队列元素指针，参数：要操作的队列的指针，互斥锁，条件变量
 */
void* queue_pop(Queue *queue,pthread_mutex_t *mutex,pthread_cond_t *cond){
    int current;
    for(;;){
        current = queue->next_to_read;//----------------------------这句放在这里赋值 会出现curren为空的情况？咋产生的？ 原来是 生产时 current 在while多了 current的声明，局部两个变量
        LOGI("queue_pop queue：%#x当前要读的：%d 下一个要写的%d",queue,current,queue->next_to_write);
//        next_to_read = queue_get_next(queue,current);
        //如果当前的读是下一个写，那要等写完再说。没有可读的了-------------------------------这里区别于写的逻辑，因为当前已没有可读的了
//        if(current!=queue->next_to_write){
        if(queue->next_to_read != queue->next_to_write){
            break;
        }
        pthread_cond_wait(cond,mutex);
    }
    queue->next_to_read = queue_get_next(queue,current);
    LOGI("queue_pop queue:%#x, %d",queue,current);
    //唤醒其他 休眠的线程（因为条件变量阻塞的）
    pthread_cond_broadcast(cond);
    return queue->tab[current];
}

/*
 * 4.销毁队列：释放队列内存: 队列元素的释放 交给自定义
 */
void queue_free(Queue *queue,queue_free_func free_func){
    for(int i=0;i<queue->size;i++){
        //销毁队列中的元素，通过使用回调函数
        free_func((void*)queue->tab[i]);
    }
    free(queue->tab);
    free(queue);
}
