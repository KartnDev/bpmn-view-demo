package com.company.bpmnviewdemo.view.iframe;


import com.company.bpmnviewdemo.api.CustomViewForm;
import com.company.bpmnviewdemo.view.main.MainView;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.bpmflowui.processform.ProcessFormContext;
import io.jmix.bpmflowui.processform.annotation.Outcome;
import io.jmix.bpmflowui.processform.annotation.ProcessForm;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
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