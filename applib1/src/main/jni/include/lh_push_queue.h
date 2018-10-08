//
// Created by lh on 2018/9/30.
//
#ifndef _QUEUE_H
#define _QUEUE_H
//新建"双向链表"。成功返回0，否则，返回-1。
extern int create_queue();
//销毁"双向链表"。成功返回0，否则返回-1
extern int destroy_queue();
//"双向量表是否为空"，为空的话返回1，否则返回0
extern int queue_is_empty();
//"双向链表"的大小
extern int queue_size();
//获取"双向链表中第index位置的元素"。成功返回节点指针，失败返回NULL
extern void* queue_get(int index);
//获取"双向链表中第一个元素"。成功返回节点指针，失败返回NULL
extern void* queue_get_first();
//获取"双向链表中最后一个元素"。成功返回节点指针，失败返回NULL。
extern void* queue_get_last();
//将"value"插入到index位置，成功返回0，失败返回-1
extern int queue_insert(int index,void *pval);
//将"value"插入到表头位置。成功返回0，失败返回-1
extern int queue_insert_first(void* pval);
//将"value"插入到末尾位置。成功返回0，失败返回-1
extern int queue_insert_last(void* pval);
//删除"双向链表中index位置的节点"。成功返回0，失败返回-1
extern int queue_delete(int index);
//删除第一个节点。成功返回0，失败返回-1
extern int queue_delete_first();
//删除组后一个节点。成功返回0，失败返回-1
extern int queue_delete_last();
#endif//_QUEUE_H
