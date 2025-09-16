package com.example.todolist

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class TodoItem(
	val id: Long,
	val title: String,
	val isDone: Boolean = false
)

class TodoViewModel : ViewModel() {
	private val _items = mutableStateListOf<TodoItem>()
	val items: List<TodoItem> get() = _items

	fun addItem(title: String) {
		if (title.isBlank()) return
		_items.add(
			TodoItem(
				id = System.currentTimeMillis(),
				title = title.trim(),
				isDone = false
			)
		)
	}

	fun toggleDone(id: Long) {
		val index = _items.indexOfFirst { it.id == id }
		if (index != -1) {
			val current = _items[index]
			_items[index] = current.copy(isDone = !current.isDone)
		}
	}

	fun removeItem(id: Long) {
		_items.removeAll { it.id == id }
	}
} 