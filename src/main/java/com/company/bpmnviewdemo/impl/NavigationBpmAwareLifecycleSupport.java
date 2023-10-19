package com.company.bpmnviewdemo.impl;

import com.google.common.base.Strings;
import io.jmix.bpm.data.form.FormData;
import io.jmix.bpm.data.form.FormParam;
import io.jmix.bpm.data.form.FormParamValueSource;
import io.jmix.bpm.processform.ProcessFormDataExtractor;
import io.jmix.bpmflowui.injector.BpmControllerProcessor;
import io.jmix.bpmflowui.processform.ProcessFormContext;
import io.jmix.bpmflowui.processform.ProcessFormContextObjectFactory;
import io.jmix.bpmflowui.processform.ProcessFormInitData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NavigationBpmAwareLifecycleSupport {

    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessFormContextObjectFactory processFormContextObjectFactory;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessFormDataExtractor processFormDataExtractor;
    @Autowired
    private BpmControllerProcessor bpmControllerProcessor;
    @Autowired
    private RepositoryService repositoryService;

    public void doSupportLifecycle(String taskId, View origin) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
        FormData formData = processFormDataExtractor.getTaskFormData(taskId);

        ProcessFormContext processFormContext = processFormContextObjectFactory.getPrototype();
        processFormContext.setTask(task);
        processFormContext.setProcessDefinition(processDefinition);

        //noinspection DataFlowIssue
        fillFormParamValuesForTaskForm(formData, task.getExecutionId());
        processFormContext.setFormData(formData);

        bpmControllerProcessor.process(origin,
                new ProcessFormInitData(
                        processFormContext,
                        runtimeService.getVariables(task.getExecutionId()))
        );
    }

    private void fillFormParamValuesForTaskForm(FormData formData, String executionId) {
        for (FormParam formParam : formData.getFormParams()) {
            if (formParam.getValueSource() == FormParamValueSource.PROCESS_VARIABLE && !Strings.isNullOrEmpty(formParam.getValue())) {
                formParam.setEvaluatedValue(runtimeService.getVariable(executionId, formParam.getValue()));
            } else if (formParam.getValueSource() == FormParamValueSource.DIRECT_VALUE) {
                formParam.setEvaluatedValue(formParam.getValue());
            }
        }
    }
}
