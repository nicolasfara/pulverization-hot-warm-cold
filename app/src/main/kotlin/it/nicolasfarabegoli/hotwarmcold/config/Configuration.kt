package it.nicolasfarabegoli.hotwarmcold.config

import it.nicolasfarabegoli.pulverization.core.ActuatorsComponent
import it.nicolasfarabegoli.pulverization.core.BehaviourComponent
import it.nicolasfarabegoli.pulverization.core.CommunicationComponent
import it.nicolasfarabegoli.pulverization.core.SensorsComponent
import it.nicolasfarabegoli.pulverization.core.StateComponent
import it.nicolasfarabegoli.pulverization.dsl.Cloud
import it.nicolasfarabegoli.pulverization.dsl.Device
import it.nicolasfarabegoli.pulverization.dsl.Edge
import it.nicolasfarabegoli.pulverization.dsl.pulverizationConfig

const val BT_NAME = "ESP32"

val config = pulverizationConfig {
    logicalDevice("smartphone") {
        BehaviourComponent and StateComponent deployableOn Cloud
        SensorsComponent and ActuatorsComponent deployableOn Device
        CommunicationComponent deployableOn Edge
    }
    logicalDevice("antenna") {
        CommunicationComponent deployableOn Edge
        SensorsComponent and ActuatorsComponent deployableOn Device
        BehaviourComponent deployableOn Cloud
    }
}
