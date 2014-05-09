/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex.dataStructures;

/**
 *
 * @author karthik
 */
public class Stack<T> {

    StackItem<T> top;

    public Stack() {
        top = null;
    }

    public T push(T data) {
        StackItem<T> newStackItem = new StackItem<T>(data);

        if (this.isEmpty()) {
            top = newStackItem;
        } else {
            newStackItem.setNext(top);
            top = newStackItem;
        }
        return newStackItem.data;
    }

    public boolean isEmpty() {
        return (top == null);
    }

    public T pop() {
        StackItem<T> newStackItem;

        if (this.isEmpty()) // stack is empty so top will be null
            return null;
        
        newStackItem = top;
        top = top.getNext();

        return newStackItem.data;
    }

    public T peek() {
        if(isEmpty())
            return null;
        return top.data;
    }

     class StackItem<T> {

        private StackItem<T> next;
        T data;

        StackItem(T item) {
            data = item;
            next = null;
        }

        StackItem<T> getNext() {
            return next;
        }

        StackItem<T> setNext(T item) {
            next = new StackItem<T>(item);
            return next;
        }

        StackItem<T> setNext(StackItem<T> item) {
            next = item;
            return item;
        }
    }

}
