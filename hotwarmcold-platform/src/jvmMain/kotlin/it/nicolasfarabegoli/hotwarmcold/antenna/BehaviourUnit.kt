package it.nicolasfarabegoli.hotwarmcold.antenna

import it.nicolasfarabegoli.hotwarmcold.components.antenna.*
import it.nicolasfarabegoli.hotwarmcold.components.smartphone.SmartphoneCommunication
import it.nicolasfarabegoli.hotwarmcold.components.smartphone.smartphoneCommunicationLogic
import it.nicolasfarabegoli.hotwarmcold.config.config
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.actuatorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.communicationLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val platform = pulverizationPlatform(config.getDeviceConfiguration("antenna")!!) {
        behaviourLogic(LedBehaviour(), ::ledBehaviourLogic)
        actuatorsLogic(LedActuatorContainer(), ::ledActuatorLogic)
        communicationLogic(SmartphoneCommunication(), ::smartphoneCommunicationLogic)
        withPlatform { RabbitmqCommunicator() }
        withRemotePlace { defaultRabbitMQRemotePlace() }
    }
    platform.start().joinAll()
    platform.stop()
}
