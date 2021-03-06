/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.gtasks;

import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.astrid.core.PluginServices;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.StoreObject;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.test.DatabaseTestCase;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
public class GtasksIndentActionTest extends DatabaseTestCase {

    @Autowired private GtasksMetadataService gtasksMetadataService;
    @Autowired private GtasksListService gtasksListService;
    @Autowired private GtasksTaskListUpdater gtasksTaskListUpdater;

    private Task task;
    private StoreObject storeList;

    public void testIndentWithoutMetadata() {
        givenTask(taskWithoutMetadata());

        whenIncreaseIndent();

        // should not crash
    }

    public void disabled_testIndentWithMetadataButNoOtherTasks() {
        givenTask(taskWithMetadata(0, 0));

        whenIncreaseIndent();

        thenExpectIndentationLevel(0);
    }

    public void testIndentWithMetadata() {
        taskWithMetadata(0, 0);
        givenTask(taskWithMetadata(1, 0));

        whenIncreaseIndent();

        thenExpectIndentationLevel(1);
    }

    public void testDeindentWithMetadata() {
        givenTask(taskWithMetadata(0, 1));

        whenDecreaseIndent();

        thenExpectIndentationLevel(0);
    }

    public void testDeindentWithoutMetadata() {
        givenTask(taskWithoutMetadata());

        whenDecreaseIndent();

        // should not crash
    }

    public void testDeindentWhenAlreadyZero() {
        givenTask(taskWithMetadata(0, 0));

        whenDecreaseIndent();

        thenExpectIndentationLevel(0);
    }

    public void disabled_testIndentWithChildren() {
        taskWithMetadata(0, 0);
        givenTask(taskWithMetadata(1, 0));
        Task child = taskWithMetadata(2, 1);

        whenIncreaseIndent();

        thenExpectIndentationLevel(1);
        thenExpectIndentationLevel(child, 2);
    }

    public void testDeindentWithChildren() {
        taskWithMetadata(0, 0);
        givenTask(taskWithMetadata(1, 1));
        Task child = taskWithMetadata(2, 2);

        whenDecreaseIndent();

        thenExpectIndentationLevel(0);
        thenExpectIndentationLevel(child, 1);
    }

    public void testIndentWithSiblings() {
        taskWithMetadata(0, 0);
        givenTask(taskWithMetadata(1, 0));
        Task sibling = taskWithMetadata(2, 0);

        whenIncreaseIndent();

        thenExpectIndentationLevel(1);
        thenExpectIndentationLevel(sibling, 0);
    }

    public void disabled_testIndentWithChildrensChildren() {
        taskWithMetadata(0, 0);
        givenTask(taskWithMetadata(1, 0));
        Task child = taskWithMetadata(2, 1);
        Task grandchild = taskWithMetadata(3, 2);

        whenIncreaseIndent();

        thenExpectIndentationLevel(1);
        thenExpectIndentationLevel(child, 2);
        thenExpectIndentationLevel(grandchild, 3);
    }

    // --- helpers

    private void whenIncreaseIndent() {
        gtasksTaskListUpdater.indent(storeList, task.getId(), 1);
    }

    private void whenDecreaseIndent() {
        gtasksTaskListUpdater.indent(storeList, task.getId(), -1);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        TaskLists lists = new TaskLists();
        List<TaskList> items = new ArrayList<>();
        TaskList list = new TaskList();
        list.setId("list");
        list.setTitle("Test Tasks");
        items.add(list);
        lists.setItems(items);
        gtasksListService.updateLists(lists);

        storeList = gtasksListService.getLists()[0];
    }

    private Task taskWithMetadata(long order, int indentation) {
        Task newTask = new Task();
        PluginServices.getTaskService().save(newTask);
        Metadata metadata = GtasksMetadata.createEmptyMetadata(newTask.getId());
        metadata.setValue(GtasksMetadata.INDENT, indentation);
        metadata.setValue(GtasksMetadata.ORDER, order);
        metadata.setValue(GtasksMetadata.LIST_ID, "list");
        metadata.setTask(newTask.getId());
        PluginServices.getMetadataService().save(metadata);
        return newTask;
    }

    private void thenExpectIndentationLevel(int expected) {
        thenExpectIndentationLevel(task, expected);
    }

    private void thenExpectIndentationLevel(Task targetTask, int expected) {
        Metadata metadata = gtasksMetadataService.getTaskMetadata(targetTask.getId());
        assertNotNull("task has metadata", metadata);
        int indentation = metadata.getValue(GtasksMetadata.INDENT);
        assertTrue("indentation: " + indentation,
                indentation == expected);
    }

    private void givenTask(Task taskToTest) {
        task = taskToTest;
    }

    private Task taskWithoutMetadata() {
        Task task = new Task();
        PluginServices.getTaskService().save(task);
        return task;
    }

}//*/
