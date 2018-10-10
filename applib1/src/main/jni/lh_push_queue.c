//
// Created by lh on 2018/9/30.
//
#include <stdio.h>
#include <malloc.h>
#include "lh_push_queue.h"

typedef struct queue_node{
    struct queue_node* prev;
    struct queue_node* next;
    void* p;//节点的值
}node;

static node *phead = NULL;
static int count = 0;

//创建的节点是static的，其实没必要吧，容器咋能只有一个呢，这里
static node* create_node(void* pval){
    node* pnode = NULL;
    pnode = (node*)malloc(sizeof(node));//这里强转和不强转 有区别？？？？？？？？？？？？？
    if(pnode){
        //成功分配内存空间，则头节点的上下节点都是 自个
        pnode->prev = pnode->next = pnode;
        pnode->p = pval;
    }
    return pnode;
}
//新建"双向链表"。成功返回0，否则，返回-1。
extern int create_queue(){
    //创建头节点--创建一个节点（节点存储上下节点的指针，value的指针）
    phead = create_node(NULL);
    if(phead){
        //设置节点个数为0
        count = 0;
        return 0;
    }
    return -1;
}

//销毁"双向链表"。成功返回0，否则返回-1
extern int destroy_queue(){
    if(!phead){
        return -1;
    }
    node *pnode = phead->next;
    node *ptmp = NULL;
    if(pnode!=phead){
        ptmp = pnode;
        pnode = pnode->next;
        free(ptmp);
    }
    free(phead);
    phead = NULL;
    count = 0;
    return 0;
}

//"双向量表是否为空"，为空的话返回1，否则返回0
extern int queue_is_empty(){
    return count == 0;
}
//"双向链表"的大小
extern int queue_size(){
    return count;
}

static node* get_node(int index){
    //思路：二分查找:1.如果index<count/2 则从头节点开始next一次找，否则，从头节点开始prev依次找
    if(index<0 || index>=count){
        return NULL;
    }
    if(index < count/2){
        int i=0;
        node *pnode = phead->next;
        while((i++)<index){
            pnode = phead->next;
        }
        return pnode;// 当i == index时返回
    }
    int j = 0;
    int rindex = count - 1 - index;//倒着数第多少个
    node *rnode = phead->prev;
    while((j++)<rindex){
        rnode = phead->prev;
    }
    return rnode;
}

//获取"双向链表中第index位置的元素"。成功返回节点指针，失败返回NULL
extern void* queue_get_value(int index){
    node *pindex = get_node(index);
    if(!pindex){
        return NULL;
    }
    return pindex->p;
}
//获取"双向链表中第一个元素"。成功返回节点指针，失败返回NULL
extern void* queue_get_first(){
    return get_node(0)->p;
}
//获取"双向链表中最后一个元素"。成功返回节点指针，失败返回NULL。
extern void* queue_get_last(){
    return get_node(count -1);
}
//将"value"插入到表头位置。成功返回0，失败返回-1
extern int queue_insert_first(void* pval){
    //这里应该要判断头节点是否空
    //创建一个节点，然后节点p指向pval. 接着处理 该节点和头尾两节点的上下指针指向
    node *pnode = create_node(pval);
    if(!pnode){
        return -1;
    }
    //当前节点的上下指向。 下节点的上指向，上节点的下指向
    pnode->prev = phead;
    pnode->next = phead->next;
    phead->next->prev = pnode;
    phead->next = pnode;
    count++;
    return 0;
}
//将"value"插入到末尾位置。成功返回0，失败返回-1
extern int queue_insert_last(void* pval){
    //处理类似 插入的表头
    node *pnode = create_node(pval);
    if(!pnode){
        return -1;
    }
    pnode->prev = phead->prev;
    pnode->next = phead;
    phead->prev->next = pnode;
    phead->prev = pnode;
    count++;
    return 0;
}
//将"value"插入到index位置，成功返回0，失败返回-1
extern int queue_insert(int index,void *pval){
    //思路：1.若在0的位置，则调用插入表头位置函数。否则，找到该位置的节点，创建节点，然后做上下指针指向处理
    //插入表头
    if(index == 0){
        return queue_insert_first(pval); 
    }
    node *pindex = get_node(index);
    if(!pindex){
        return -1;
    }
    node *pnode = create_node(pval);
    if(!pnode){
        return -1;
    }
    pnode->prev= pindex->prev;
    pnode->next = pindex;
    pindex->prev->next = pnode;
    pindex->prev = pnode;
    count++;
    return 0;
}

//删除"双向链表中index位置的节点"。成功返回0，失败返回-1
extern int queue_delete(int index){
    //思路：找到index对应的元素，调整起上下指针指向，然后释放内存，数量减一
    node *pnode = get_node(index);
    if(!pnode){
        return -1;
    }
    pnode->prev->next = pnode->next;
    pnode->next->prev = pnode->prev;
    free(pnode);
    count--;
    return 0;
}
//删除第一个节点。成功返回0，失败返回-1
extern int queue_delete_first(){
    return queue_delete(0);
}
//删除组后一个节点。成功返回0，失败返回-1
extern int queue_delete_last(){
    return queue_delete(count -1);
}
