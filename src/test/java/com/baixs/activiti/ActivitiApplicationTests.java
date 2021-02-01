package com.baixs.activiti;

import com.baixs.activiti.utils.SecurityUtil;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.CompleteTaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ActivitiApplicationTests {
    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    // 流程变量，附件均在该对象中
    private TaskService taskService;

    // 部署流程
    @Test
    void deployProcess() {
        securityUtil.logInAs("salaboy");
        Deployment deployment = repositoryService.createDeployment().name("测试流程3")
                .addClasspathResource("test3.bpmn")
                .addClasspathResource("test3.png")
                .deploy();
        System.out.println("部署的流程信息：" + deployment);
    }

    // 查看流程定义
    @Test
    void processDefinition() {
        securityUtil.logInAs("salaboy");
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));
        System.out.println("查询到流程数量：" + processDefinitionPage.getTotalItems());
        List<ProcessDefinition> content = processDefinitionPage.getContent();
        for (ProcessDefinition processDefinition : content) {
            System.out.println("流程定义信息：" + processDefinition);
        }
    }

    // 启动流程实例
    @Test
    void startProcessInstance() {
        securityUtil.logInAs("salaboy");
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionId("myProcess:5:fbf5b2b1-6470-11eb-b5be-d89c6748a88e")
                .build());
        System.out.println("流程实例信息：" + processInstance);
    }

    // 查询待办任务，并完成
    @Test
    void selectUncompletedTaskAndComplete() {
        securityUtil.logInAs("salaboy");
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 100));
        System.out.println("待办任务数量：" + tasks.getTotalItems());
        for (Task task : tasks.getContent()) {
            // 多人任务时需要先拾取任务
            if (task.getAssignee() == null) {
                taskRuntime.claim(new ClaimTaskPayloadBuilder().withTaskId(task.getId()).build());
            }
            // 完成任务
            taskRuntime.complete(new CompleteTaskPayloadBuilder().withTaskId(task.getId()).build());
            System.out.println("已完成任务：" + task);
        }
    }

}
