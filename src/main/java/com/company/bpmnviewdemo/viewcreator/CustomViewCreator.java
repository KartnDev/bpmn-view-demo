package com.company.bpmnviewdemo.viewcreator;

import com.company.bpmnviewdemo.api.CustomViewForm;
import com.google.common.base.Strings;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.bpm.data.form.FormData;
import io.jmix.bpm.data.form.FormParam;
import io.jmix.bpm.data.form.FormParamValueSource;
import io.jmix.bpmflowui.injector.BpmControllerProcessor;
import io.jmix.bpmflowui.processform.ProcessFormContext;
import io.jmix.bpmflowui.processform.ProcessFormContextObjectFactory;
import io.jmix.bpmflowui.processform.ProcessFormInitData;
import io.jmix.bpmflowui.processform.viewcreator.impl.JmixViewProcessFormViewCreator;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.flowable.engine.RuntimeService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;


@SuppressWarnings("JmixInternalElementUsage")
@Component("bpm_CustomViewCreator")
@Primary
public class CustomViewCreator extends JmixViewProcessFormViewCreator {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private ProcessFormContextObjectFactory processFormContextObjectFactory;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private BpmControllerProcessor bpmControllerProcessor;
    @Autowired
    protected ViewRegistry viewRegistry;

    @Override
    public DialogWindow createUserTaskView(CreationContext creationContext) {
        FormData formData = creationContext.getFormData();

        if (formData == null) {
            throw new IllegalStateException("Unable to create start process view cause formData is null");
        }
        ViewInfo viewInfo = viewRegistry.getViewInfo(formData.getScreenId());
        Class<? extends View> viewControllerClass = viewInfo.getControllerClass();

        if(viewControllerClass.getAnnotation(CustomViewForm.class) != null) {
            ProcessFormContext processFormContext = processFormContextObjectFactory.getPrototype();
            Task task = creationContext.getTask();
            processFormContext.setTask(task);
            fillFormParamValuesForTaskForm(formData, task.getExecutionId());
            processFormContext.setFormData(formData);
            ProcessFormInitData processFormInitData = new ProcessFormInitData(
                    processFormContext,
                    runtimeService.getVariables(task.getExecutionId()));

            var routeParam = new RouteParameters(
                    new RouteParam("processInstanceBusinessKey", creationContext.getTask().getProcessInstanceId()),
                    new RouteParam("taskId", creationContext.getTask().getId()));

            viewNavigators.view(viewControllerClass)
                    .withRouteParameters(routeParam)
                    .withBackwardNavigation(true)
                    .withAfterNavigationHandler(event -> {
                        bpmControllerProcessor.process(event.getView(), processFormInitData);
                    })
                    .navigate();
            return null;
        } else {
            return super.createStartProcessView(creationContext);
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
