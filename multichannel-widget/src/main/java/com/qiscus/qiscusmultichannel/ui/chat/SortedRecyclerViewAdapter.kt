package com.qiscus.qiscusmultichannel.ui.chat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
abstract class SortedRecyclerViewAdapter<Item, VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>() {

    protected var recyclerViewItemClickListener: CommentsAdapter.RecyclerViewItemClickListener? = null

    val data: SortedList<Item> = SortedList(itemClass, object : SortedList.Callback<Item>() {
        override fun compare(o1: Item, o2: Item): Int {
            return this@SortedRecyclerViewAdapter.compare(o1, o2)
        }

        override fun onChanged(position: Int, count: Int) {
            this@SortedRecyclerViewAdapter.onChanged(position, count)
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return this@SortedRecyclerViewAdapter.areContentsTheSame(oldItem, newItem)
        }

        override fun areItemsTheSame(item1: Item, item2: Item): Boolean {
            return this@SortedRecyclerViewAdapter.areItemsTheSame(item1, item2)
        }

        override fun onInserted(position: Int, count: Int) {
            this@SortedRecyclerViewAdapter.onInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            this@SortedRecyclerViewAdapter.onRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            this@SortedRecyclerViewAdapter.onMoved(fromPosition, toPosition)
        }
    })

    protected abstract val itemClass: Class<Item>

    fun itemCount(): Int {
        return data.size()
    }

    protected abstract fun compare(item1: Item, item2: Item): Int

    protected fun onChanged(position: Int, count: Int) {

    }

    protected fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }

    protected fun areItemsTheSame(item1: Item, item2: Item): Boolean {
        return item1 == item2
    }

    protected fun onInserted(position: Int, count: Int) {

    }

    protected fun onRemoved(position: Int, count: Int) {

    }

    protected fun onMoved(fromPosition: Int, toPosition: Int) {

    }

    fun findPosition(item: Item): Int {
        if (data == null) {
            return -1
        }

        val size = data.size() - 1
        for (i in size downTo 0) {
            if (data.get(i)!!.equals(item)) {
                return i
            }
        }

        return -1
    }

    fun add(item: Item): Int {
        val i = data.add(item)
        notifyItemInserted(i)
        return i
    }

    open fun add(items: List<Item>) {
        data.addAll(items)
        notifyDataSetChanged()
    }

    open fun addOrUpdate(item: Item) {
        val i = findPosition(item)
        if (i >= 0) {
            data.updateItemAt(i, item)
            notifyDataSetChanged()
        } else {
            add(item)
        }
    }

    open fun addOrUpdate(items: List<Item>) {
        for (item in items) {
            val i = findPosition(item)
            if (i >= 0) {
                data.updateItemAt(i, item)
            } else {
                data.add(item)
            }
        }
        notifyDataSetChanged()
    }

    open fun remove(position: Int) {
        if (position >= 0 && position < data.size()) {
            data.removeItemAt(position)
            notifyItemRemoved(position)
        }
    }

    open fun remove(item: Item) {
        val position = findPosition(item)
        remove(position)
    }

    //Set method of OnItemClickListener object
    fun setOnItemClickListener(recyclerViewItemClickListener: CommentsAdapter.RecyclerViewItemClickListener) {
        this.recyclerViewItemClickListener = recyclerViewItemClickListener
    }

    fun setOnClickListener(view: View, position: Int) {
        view.setOnClickListener { v ->
            //When item view is clicked, trigger the itemclicklistener
            recyclerViewItemClickListener?.onItemClick(v, position)
        }
        view.setOnLongClickListener { v ->
            //When item view is clicked long, trigger the itemclicklistener
            recyclerViewItemClickListener?.onItemLongClick(v, position)
            true
        }
    }

    fun clear() {
        data.clear()
    }


}