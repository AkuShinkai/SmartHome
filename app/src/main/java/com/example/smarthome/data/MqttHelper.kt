package com.example.smarthome.data

import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

object MqttManager {
    private const val TAG = "MQTT-Manager"
    private const val brokerUri = "ssl://fcea052744354405b694acff9b476116.s1.eu.hivemq.cloud:8883"
    private const val username = "AkuShinkai"
    private val password = "@Fuckrise69".toCharArray()

    private var client: MqttClient? = null

    fun connect() {
        try {
            if (client == null || !client!!.isConnected) {
                val clientId = MqttClient.generateClientId()
                client = MqttClient(brokerUri, clientId, null)

                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    userName = username
                    this.password = com.example.smarthome.data.MqttManager.password
                    connectionTimeout = 10
                    keepAliveInterval = 60
                }

                Log.d(TAG, "Connecting to HiveMQ broker...")
                client!!.connect(options)
                Log.d(TAG, "Connected to HiveMQ!")
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            e.printStackTrace()
        }
    }

    fun publish(topic: String, message: String) {
        try {
            if (client == null || !client!!.isConnected) {
                Log.w(TAG, "Client not connected. Reconnecting...")
                connect()
            }

            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                qos = 1
            }

            client?.publish(topic, mqttMessage)
            Log.d(TAG, "Published message: $message to topic: $topic")
        } catch (e: MqttException) {
            Log.e(TAG, "Publish failed: ${e.message}")
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            client?.disconnect()
            client = null
            Log.d(TAG, "Disconnected from HiveMQ")
        } catch (e: MqttException) {
            Log.e(TAG, "Disconnection failed: ${e.message}")
        }
    }
}
