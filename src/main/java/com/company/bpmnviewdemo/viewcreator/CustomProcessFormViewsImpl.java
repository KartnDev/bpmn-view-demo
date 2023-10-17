package com.company.bpmnviewdemo.viewcreator;

import io.jmix.bpm.data.form.FormData;
import io.jmix.bpmflowui.processform.ProcessFormViewsImpl;
import io.jmix.bpmflowui.processform.viewcreator.ProcessFormViewCreator;
import io.jmix.bpmflowui.processform.viewcreator.impl.JmixViewProcessFormViewCreator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component("bpm_CustomProcessFormViewsImpl")
@Primary
public class CustomProcessFormViewsImpl extends ProcessFormViewsImpl {

    @Nullable
    @Override
    protected ProcessFormViewCreator getViewCreator(String formType) {
        return processFormViewCreators.stream()
                .filter(viewCreator -> formType.equals(viewCreator.isApplicableFor()))
                .filter(processFormViewCreator -> isNotInterceptedWithCustomCreator(processFormViewCreator.getClass()))
                .findFirst()
                .orElse(null);
    }

    protected boolean isNotInterceptedWithCustomCreator(Class<? extends ProcessFormViewCreator> clz) {
        return !clz.equals(JmixViewProcessFormViewCreator.class);
    }
}
