package org.woo.orchestrator.workflow.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.support.JacksonUtils
import org.springframework.stereotype.Component
import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.kafka.KafkaProducer
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.workflow.SagaWorkflowTemplate
import org.woo.orchestrator.workflow.WorkflowCommand
import org.woo.orchestrator.workflow.command.UpdateUserNameCommand

@Component
class UserNameChangeWorkflow(
    @Value("\${topic.name-update}")
    val topic: String,
    val kafkaProducer: KafkaProducer,
    val mapper: ObjectMapper,
) : SagaWorkflowTemplate() {
    override val eventType: EventType = EventType.UPDATE_USER_NAME

    override suspend fun execute(command: WorkflowCommand) {
//        val data = command as UpdateUserNameCommand
        val message = mapper.writeValueAsString(command)
        kafkaProducer.send(topic, message)
    }

    override suspend fun rollback(command: WorkflowCommand) {
    }

    override suspend fun generateCommand(
        outbox: Outbox,
        recordOperation: RecordOperation,
    ): WorkflowCommand {
        val mapper = JacksonUtils.enhancedObjectMapper()
        val map: MutableMap<String, Any> = mapper.readValue(outbox.payload)
        map[RecordOperation.CONSTANT] = recordOperation
        val newPayload = mapper.writeValueAsString(map)
        return mapper.readValue(newPayload, UpdateUserNameCommand::class.java)
    }
}
