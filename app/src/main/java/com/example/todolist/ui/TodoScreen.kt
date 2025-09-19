@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.todolist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.TodoItem
import com.example.todolist.TodoViewModel
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api // Import this
import androidx.compose.material3.MaterialTheme
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color

@Composable
fun TodoScreen(
	modifier: Modifier = Modifier,
	viewModel: TodoViewModel = viewModel()
) {
	Surface(modifier = modifier.fillMaxSize()) {
		Scaffold(
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text("To-Do List") },
					colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
						containerColor = MaterialTheme.colorScheme.primaryContainer,
						titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
					)
				)
			}
		) { innerPadding ->
			Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
				AddItemRow(onAdd = { title -> viewModel.addItem(title) })
				Spacer(Modifier.height(12.dp))
				Divider()
				Spacer(Modifier.height(12.dp))
				TodoList(
					items = viewModel.items,
					onToggle = viewModel::toggleDone,
					onRemove = viewModel::removeItem,
					onMove = viewModel::moveItem
				)
			}
		}
	}
}

@Composable
private fun AddItemRow(
	onAdd: (String) -> Unit
) {
	var text by remember { mutableStateOf("") }
	Row(verticalAlignment = Alignment.CenterVertically) {
		OutlinedTextField(
			value = text,
			onValueChange = { text = it },
			modifier = Modifier.weight(1f),
			label = { Text("Add a task") },
			singleLine = true
		)
		Spacer(Modifier.width(8.dp))
		Button(
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary
			),
			onClick = {
				if (text.isNotBlank()) {
					onAdd(text)
					text = ""
				}
			}
		) { Text("Add") }
	}
}

@Composable
private fun TodoList(
	items: List<TodoItem>,
	onToggle: (Long) -> Unit,
	onRemove: (Long) -> Unit,
	onMove: (Int, Int) -> Unit
) {
	val state = rememberReorderableLazyListState(onMove = { from, to ->
		onMove(from.index, to.index)
	})

	if (items.isEmpty()) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 32.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Text(
				"No tasks yet. Add one above.",
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		return
	}

	LazyColumn(
		state = state.listState,
		modifier = Modifier
			.fillMaxSize()
			.reorderable(state)
			.detectReorderAfterLongPress(state)
	) {
		items(items, key = { it.id }) { item ->
			ReorderableItem(state, key = item.id) { isDragging ->
				TodoRow(item = item, onToggle = onToggle, onRemove = onRemove, isDragging = isDragging)
			}
			Divider()
		}
	}

}


@Composable
private fun TodoRow(
	item: TodoItem,
	onToggle: (Long) -> Unit,
	onRemove: (Long) -> Unit,
	isDragging: Boolean // <-- pass from ReorderableItem
) {
	// Animate scale and elevation while dragging
	val scale by animateFloatAsState(targetValue = if (isDragging) 1.05f else 1f, tween(150))
	val elevation by animateDpAsState(targetValue = if (isDragging) 8.dp else 0.dp, tween(150))
	val alpha by animateFloatAsState(targetValue = if (isDragging) 0.95f else 1f, tween(150))

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
			.scale(scale)
			.zIndex(if (isDragging) 1f else 0f)
			.background(
				color = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
				shape = RoundedCornerShape(8.dp)
			)
			.padding(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Checkbox(
			checked = item.isDone,
			onCheckedChange = { onToggle(item.id) }
		)
		Text(
			text = item.title,
			modifier = Modifier
				.weight(1f)
				.padding(start = 8.dp),
			textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
		)
		FilledTonalIconButton(
			onClick = { onRemove(item.id) }
		) {
			Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete task")
		}
	}
}
