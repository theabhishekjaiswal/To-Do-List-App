package com.example.todo.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.TodoContentBinding

class ToDoAdapter(private val list:MutableList<ToDoData>) :
RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>(){

    private var listener:ToDoAdapterClicksInterface?= null
    fun setListener (listener: ToDoAdapterClicksInterface){
        this.listener=listener
    }
    inner class ToDoViewHolder(val binding: TodoContentBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TodoContentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                binding.todoTask.text=this.task
                binding.editTask.setOnClickListener {
                   listener?.onEditTaskBtnClicked(this)
                }
                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteTaskBtnClicked(this)
                }
            }
        }
    }

    interface  ToDoAdapterClicksInterface{
        fun onDeleteTaskBtnClicked(toDoData: ToDoData)
        fun onEditTaskBtnClicked(toDoData: ToDoData)
    }
}