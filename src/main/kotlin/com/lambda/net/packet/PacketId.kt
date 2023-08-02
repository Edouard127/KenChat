package com.lambda.net.packet


const val CPacketDisconnect = 0
const val CPacketKeyRequest = 1
const val CPacketUptime = 2
const val CPacketPlayerInfo = 3
const val CPacketPlayerMessage = 4
const val CPacketPlayerMessageBackoff = 5
const val CPacketSystemMessage = 6
const val CPacketPlayerList = 7
const val CPacketKeepAlive = 8

const val SPacketKeyResponse = 0
const val SPacketStartWriting = 1
const val SPacketPlayerMessage = 2
const val SPacketKeepAlive = 3