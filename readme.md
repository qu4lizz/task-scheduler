# Task Scheduler

A library that implements logic for task scheduler and GUI implementation for library.

<img src="TaskSchedulerImplementation/src/main/resources/qu4lizz/taskscheduler_implementation/gui/icons/main_icon.png" alt="Task scheduler icon" width="50"/>

## Description

<p align="justify"> A task scheduler library implements basic functionality for task scheduling. It provides a simple API for users to schedule tasks and allows specification of the maximum number of tasks that can be executed concurrently. The scheduler runs each task as a separate process. The API supports adding tasks to the scheduler, scheduling tasks while others are executing, starting, pausing, resuming, and stopping tasks, waiting for tasks to complete, and specifying a scheduling algorithm, such as first-in, first-out (FIFO), priority-based, round robin priority based or preemptive scheduling. Additionally, tasks may optionally specify a start time, total allowed execution time, and a deadline for completion or interruption. </p>

<p align="justify"> As for implementation, the application can easily expand with new task types. Application user to specifies the method of scheduling and all properties of the created task (allowed execution time, deadlines, etc.). The GUI application provides support for reviewing tasks during execution, reporting task progress through a progress bar, as well as creating, starting, pausing, resuming and stopping task. </p>

As example task, chosen algorithm is edge detection of images.

## Credits

Main icon: <a href="https://www.flaticon.com/free-icons/plan" title="plan icons">Plan icons created by Freepik - Flaticon</a>

Start icon: <a href="https://www.flaticon.com/free-icons/play-button" title="play button icons">Play button icons created by Pixel perfect - Flaticon</a>

Stop icon: <a href="https://www.flaticon.com/free-icons/forbidden" title="forbidden icons">Forbidden icons created by Those Icons - Flaticon</a>

Pause icon: <a href="https://www.flaticon.com/free-icons/pause" title="pause icons">Pause icons created by inkubators - Flaticon</a>