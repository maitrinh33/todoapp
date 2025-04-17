package com.midterm.dophammaitrinh;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    private EditText titleEditText;
    private EditText noteEditText;
    private TextView dateTextView;
    private Button saveButton;
    private Calendar calendar;
    private Task taskToEdit;
    private String startTime = "";
    private String endTime = "";
    private static final String EXTRA_TASK = "TASK_TO_EDIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        titleEditText = findViewById(R.id.titleEditText);
        noteEditText = findViewById(R.id.noteEditText);
        dateTextView = findViewById(R.id.dateTextView);
        saveButton = findViewById(R.id.saveButton);
        calendar = Calendar.getInstance();

        // Check if we're editing an existing task
        if (getIntent().hasExtra(EXTRA_TASK)) {
            taskToEdit = getIntent().getParcelableExtra(EXTRA_TASK, Task.class);
            if (taskToEdit != null) {
                titleEditText.setText(taskToEdit.getTitle());
                noteEditText.setText(taskToEdit.getNote());
                dateTextView.setText(taskToEdit.getDate());
                String[] times = taskToEdit.getTimeRange().split(" to ");
                if (times.length == 2) {
                    startTime = times[0];
                    endTime = times[1];
                }
            }
        } else {
            // Set default date and time for new tasks
            setDefaultDateTime();
        }

        dateTextView.setOnClickListener(v -> showDateTimePicker());
        saveButton.setOnClickListener(v -> saveTask());
    }

    private void setDefaultDateTime() {
        // Set current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());

        // Set default time range (8:00 AM to 12:00 PM)
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        startTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(calendar.getTime());

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        endTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(calendar.getTime());

        // Display the default date and time
        dateTextView.setText(String.format("%s\n%s to %s", currentDate, startTime, endTime));
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showStartTimePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showStartTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    startTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(calendar.getTime());
                    showEndTimePicker();
                },
                8, // Default to 8:00 AM
                0,
                false
        );
        timePickerDialog.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    endTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(calendar.getTime());
                    updateDateTimeDisplay();
                },
                12, // Default to 12:00 PM
                0,
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());
        String timeRange = startTime + " to " + endTime;
        dateTextView.setText(String.format("%s\n%s", date, timeRange));
    }

    private void saveTask() {
        String title = titleEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());
        String timeRange = startTime + " to " + endTime;

        if (title.isEmpty() || note.isEmpty()) {
            Toast.makeText(this, "Please fill title and note", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(title, note, date, timeRange);
        if (taskToEdit != null) {
            task.setId(taskToEdit.getId());
        }

        new Thread(() -> {
            TaskDatabase database = TaskDatabase.getInstance(getApplicationContext());
            if (taskToEdit != null) {
                database.taskDao().update(task);
            } else {
                database.taskDao().insert(task);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
} 