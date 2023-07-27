package com.lambda.net.packet

const val SPacketHandshake = 0

const val CPacketDisconnect = 0

const val CPacketKeyRequest = 0
const val CPacketLoginSuccess = 1
const val CPacketPlayerInfo = 2
const val CPacketPlayerJoin = 3
const val CPacketPlayerLeave = 4
const val CPacketPlayerMessage = 5
const val CPacketPlayerMessageBackoff = 6
const val CPacketSystemMessage = 7

const val SPacketKeyResponse = 0
const val SPacketPlayerMessage = 1