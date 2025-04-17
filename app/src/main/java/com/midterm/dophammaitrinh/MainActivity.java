package com.midterm.dophammaitrinh;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TaskAdapter adapter;
    private TaskDatabase database;
    private LiveData<List<Task>> taskList;
    private static final String EXTRA_TASK = "TASK_TO_EDIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        database = TaskDatabase.getInstance(this);

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        // Setup search functionality
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTasks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTasks(newText);
                return true;
            }
        });

        // Setup add button
        CardView addTaskCard = findViewById(R.id.addTaskCard);
        addTaskCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Setup click listeners
        adapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onEditClick(Task task) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra(EXTRA_TASK, (Parcelable) task);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Task task) {
                new Thread(() -> {
                    database.taskDao().delete(task);
                }).start();
            }
        });

        // Load all tasks
        loadTasks();
    }

    private void loadTasks() {
        if (taskList != null) {
            taskList.removeObservers(this);
        }
        taskList = database.taskDao().getAllTasks();
        taskList.observe(this, tasks -> adapter.setTasks(tasks));
    }

    private void searchTasks(String query) {
        if (taskList != null) {
            taskList.removeObservers(this);
        }
        taskList = database.taskDao().searchTasks(query);
        taskList.observe(this, tasks -> adapter.setTasks(tasks));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }
}