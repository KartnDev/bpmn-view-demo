package com.company.bpmnviewdemo.impl;

import com.company.bpmnviewdemo.api.CustomViewForm;
import com.google.common.base.Strings;
import com.vaadin.flow.router.NavigationEvent;
import io.jmix.bpm.data.form.FormData;
import io.jmix.bpm.data.form.FormParam;
import io.jmix.bpm.data.form.FormParamValueSource;
import io.jmix.bpm.processform.ProcessFormDataExtractor;
import io.jmix.bpmflowui.injector.BpmBeforeNavigationInitializer;
import io.jmix.bpmflowui.injector.BpmControllerProcessor;
import io.jmix.bpmflowui.processform.ProcessFormContext;
import io.jmix.bpmflowui.processform.ProcessFormContextObjectFactory;
import io.jmix.bpmflowui.processform.ProcessFormInitData;
import io.jmix.bpmflowui.processform.viewcreator.ProcessFormViewCreator;
import io.jmix.core.Messages;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.View;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("projName_DefaultBpmBeforeNavigationInitializer")
public class DefaultBpmBeforeNavigationInitializer extends BpmBeforeNavigationInitializer {

    protected final ProcessFormContextObjectFactory processFormContextObjectFactory;
    protected final ProcessFormDataExtractor processFormDataExtractor;
    protected final List<ProcessFormViewCreator> processFormViewCreators;
    protected final TaskService taskService;
    protected final RuntimeService runtimeService;
    public DefaultBpmBeforeNavigationInitializer(ObjectProvider<SessionData> sessionDataProvider,
                                                 BpmControllerProcessor controllerProcessor,
                                                 Notifications notifications,
                                                 Messages messages,
                                                 ProcessFormContextObjectFactory processFormContextObjectFactory,
                                                 ProcessFormDataExtractor processFormDataExtractor,
                                                 List<ProcessFormViewCreator> processFormViewCreators,
                                                 TaskService taskService,
                                                 RuntimeService runtimeService) {
        super(sessionDataProvider, controllerProcessor, notifications, messages);
        this.processFormContextObjectFactory = processFormContextObjectFactory;
        this.processFormDataExtractor = processFormDataExtractor;
        this.processFormViewCreators = processFormViewCreators;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
    }

    @Override
    public void initialize(View<?> origin, NavigationEvent event) {
        if (origin.getClass().getAnnotation(CustomViewForm.class) != null) {
            String taskId = event.getLocation().getSegments().get(3);

            FormData formData = processFormDataExtractor.getTaskFormData(taskId);
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();

            ProcessFormContext processFormContext = processFormContextObjectFactory.getPrototype();

            processFormContext.setTask(task);
            fillFormParamValuesForTaskForm(formData, task.getExecutionId());
            processFormContext.setFormData(formData);
            ProcessFormInitData processFormInitData = new ProcessFormInitData(
                    processFormContext,
                    runtimeService.getVariables(task.getExecutionId()));


            bpmControllerProcessor.process(origin, processFormInitData);
        } else {
            super.initialize(origin, event);
        }
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
