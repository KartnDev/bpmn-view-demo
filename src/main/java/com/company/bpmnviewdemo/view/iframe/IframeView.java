package com.company.bpmnviewdemo.view.iframe;


import com.company.bpmnviewdemo.api.CustomViewForm;
import com.company.bpmnviewdemo.entity.User;
import com.company.bpmnviewdemo.impl.NavigationBpmAwareLifecycleSupport;
import com.company.bpmnviewdemo.repository.UserRepository;
import com.company.bpmnviewdemo.view.main.MainView;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.bpmflowui.processform.ProcessFormContext;
import io.jmix.bpmflowui.processform.annotation.Outcome;
import io.jmix.bpmflowui.processform.annotation.ProcessForm;
import io.jmix.bpmflowui.processform.annotation.ProcessVariable;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;

@CustomViewForm
@ProcessForm(
        outcomes = {
                @Outcome(id = "approve"),
                @Outcome(id = "reject")
        }
)
@Route(value = "processInstance/:processInstanceBusinessKey/task/:taskId", layout = MainView.class)
@ViewController("IframeView")
@ViewDescriptor("iframe-view.xml")
public class IframeView extends StandardView {

    @Autowired
    private ProcessFormContext processFormContext;
    @ViewComponent
    private JmixButton testBtn;

    @ProcessVariable(name = "username")
    @ViewComponent
    private TypedTextField<String> usernameField;
    @ViewComponent
    private InstanceContainer<User> userDc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NavigationBpmAwareLifecycleSupport navigationBpmAwareLifecycleSupport;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // happens before BeforeShowEvent
        String taskId = event.getRouteParameters().get("taskId").orElseThrow();

        // signal that we are not in bpmn context view build, just url open context
        navigationBpmAwareLifecycleSupport.doSupportLifecycle(taskId, this);
        super.beforeEnter(event);
    }

    @Subscribe("usernameField")
    public void onUsernameFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        User user= userRepository.findByUsername(usernameField.getValue());
        userDc.setItem(user);
        Task task = processFormContext.getTask();
        if(task.getName()!=null && task.getName().equals("User Task")){
            testBtn.setVisible(false);
        }
    }



    @Subscribe("approveBtn")
    public void onApproveBtnClick(final ClickEvent<JmixButton> event) {
        processFormContext.taskCompletion()
                .withOutcome("approve")
                .saveInjectedProcessVariables()
                .complete();
        closeWithDefaultAction();
    }

    @Subscribe("rejectBtn")
    public void onRejectBtnClick(final ClickEvent<JmixButton> event) {
        processFormContext.taskCompletion()
                .withOutcome("reject")
                .saveInjectedProcessVariables()
                .complete();
        closeWithDefaultAction();
    }


}