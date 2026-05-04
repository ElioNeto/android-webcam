package dev.elioneto.androidwebcam.ui.home

import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.elioneto.androidwebcam.signaling.ConnectionState
import dev.elioneto.androidwebcam.signaling.SignalingClient
import dev.elioneto.androidwebcam.webrtc.WebRTCManager
import dev.elioneto.androidwebcam.webrtc.WebRTCState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

data class HomeUiState(
    val statusMessage: String = "Connect PC and phone to the same Wi-Fi or use USB",
    val localIpAddress: String = "",
    val isIpAvailable: Boolean = false,
    val phonePort: Int = 8888,
    val connectionStatus: String = "Disconnected",
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wifiManager: WifiManager,
    private val signalingClient: SignalingClient,
    private val webRTCManager: WebRTCManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        // Carregar IP local
        viewModelScope.launch {
            val ipAddress = getLocalIpAddress()
            _uiState.value = _uiState.value.copy(
                localIpAddress = ipAddress,
                isIpAvailable = ipAddress.isNotEmpty(),
                statusMessage = if (ipAddress.isNotEmpty()) {
                    "Your phone IP: $ipAddress (port $DEFAULT_PORT)"
                } else {
                    "Connect PC and phone to the same Wi-Fi or use USB"
                }
            )
        }

        // Observar estado da conexão de sinalização
        viewModelScope.launch {
            signalingClient.connectionState.collect { state ->
                val connectionInfo = when (state) {
                    ConnectionState.DISCONNECTED -> Triple("Disconnected", false, false)
                    ConnectionState.CONNECTING -> Triple("Connecting...", true, false)
                    ConnectionState.CONNECTED -> Triple("Connected", false, true)
                    ConnectionState.RECONNECTING -> Triple("Reconnecting...", true, false)
                }
                _uiState.value = _uiState.value.copy(
                    connectionStatus = connectionInfo.first,
                    isConnecting = connectionInfo.second,
                    isConnected = connectionInfo.third
                )
            }
        }

        // Observar estado do WebRTC
        viewModelScope.launch {
            webRTCManager.webrtcState.collect { state ->
                when (state) {
                    WebRTCState.DISCONNECTED -> {
                        _uiState.value = _uiState.value.copy(
                            connectionStatus = "Disconnected",
                            isConnecting = false,
                            isConnected = false
                        )
                    }
                    WebRTCState.CONNECTING -> {
                        _uiState.value = _uiState.value.copy(
                            connectionStatus = "WebRTC Connecting...",
                            isConnecting = true,
                            isConnected = false
                        )
                    }
                    WebRTCState.CONNECTED -> {
                        _uiState.value = _uiState.value.copy(
                            connectionStatus = "Connected",
                            isConnecting = false,
                            isConnected = true,
                            errorMessage = null
                        )
                    }
                    WebRTCState.FAILED -> {
                        _uiState.value = _uiState.value.copy(
                            connectionStatus = "Connection Failed",
                            isConnecting = false,
                            isConnected = false,
                            errorMessage = signalingClient.errorMessage.value
                        )
                    }
                }
            }
        }

        // Observar erros do signaling
        viewModelScope.launch {
            signalingClient.errorMessage.collect { error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(errorMessage = error)
                }
            }
        }
    }

    /**
     * Conecta ao servidor de sinalização.
     */
    fun connectToServer(host: String = DEFAULT_HOST, port: Int = DEFAULT_PORT) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
                webRTCManager.connect(host, port)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = "Connection failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Desconecta do servidor de sinalização.
     */
    fun disconnectFromServer() {
        viewModelScope.launch {
            webRTCManager.disconnect()
        }
    }

    /**
     * Obtém o IP local do dispositivo (WiFi)
     */
    private fun getLocalIpAddress(): String {
        try {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                val ipInt = wifiInfo.ipAddress
                if (ipInt != 0) {
                    return intToIp(ipInt)
                }
            }
        } catch (_: Exception) {
            // Fallback para busca por interface de rede
        }

        // Fallback: buscar por interface de rede
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            networkInterfaces?.asSequence()?.forEach { networkInterface ->
                networkInterface.inetAddresses?.asSequence()?.forEach { inetAddress ->
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        val ip = inetAddress.hostAddress ?: return@forEach
                        // Preferir IPs da faixa 192.168.x.x ou 10.x.x.x
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignorar erro
        }

        return ""
    }

    /**
     * Converte inteiro IP para string
     */
    private fun intToIp(ip: Int): String {
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    companion object {
        const val DEFAULT_HOST = "192.168.1.100"
        const val DEFAULT_PORT = 8888
    }
}
