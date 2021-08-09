package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.games.exceptions.ActionException

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

const val CLIENT_ID = 1000
const val CLIENT_CHALLENGE = 1010
const val CLIENT_CHALLENGE_ABORT = 1011
const val CLIENT_CHALLENGE_ACCEPT = 1012
const val CLIENT_CHALLENGE_DECLINE = 1013
const val CLIENT_UPDATE = 2000

const val SERVER_ERROR = 0
const val SERVER_ID = 1100
const val SERVER_CHALLENGE = 1110
const val SERVER_CHALLENGE_ABORT = 1111
const val SERVER_CHALLENGE_ACCEPT = 1112
const val SERVER_CHALLENGE_DECLINE = 1113
const val SERVER_GAME_INIT_NEXT = 2100
const val SERVER_GAME_INIT = 2101
const val SERVER_GAME_UPDATE_NEXT = 2110
const val SERVER_GAME_UPDATE = 2111
const val SERVER_GAME_END_VICTORY = 2120
const val SERVER_GAME_END_DEFEAT = 2121
const val SERVER_GAME_END_TIE = 2122

private val mapper = ObjectMapper()

fun parseClientMessage(message: String): Pair<Int, JsonNode> {
    val node = mapper.readTree(message)
    val codeNode = node.get("code")
    val argNode = node.get("args")
    if (codeNode == null || !codeNode.isInt) {
        throw ActionException("Missing event code")
    }
    if (argNode == null || !argNode.isObject) {
        throw ActionException("Missing arguments")
    }
    val messageCode = codeNode.intValue()
    return Pair(messageCode, argNode)
}

fun readClientGameArguments(argsNode: JsonNode, actionClass: Class<*>): Any {
    try {
        return mapper.treeToValue(argsNode, actionClass)
    } catch (e: JsonProcessingException) {
        throw ActionException ("Invalid arguments $argsNode")
    }
}

fun <T> readClientArgument(node: JsonNode, name: String, type: Class<T>): T {
    val argNode = node.get(name)
    if (argNode != null) {
        try {
            return mapper.convertValue(argNode, type)
        } catch (e: IllegalArgumentException) {
            throw ActionException ("Cannot convert argument $name to ${type.simpleName}")
        }
    }
    throw ActionException ("Missing argument $name")
}

fun createErrorMessage(reason: String): String {
    return stringify(
        SERVER_ERROR,
        Pair("reason", reason)
    )
}

fun createIdMessage(sender: Client) : String {
    return stringify(
        SERVER_ID,
        Pair("id", sender.id)
    )
}

fun createChallengeMessage(opponent: Client) : String {
    return stringify(
        SERVER_CHALLENGE,
        Pair("opponent", opponent.id)
    )
}

fun createChallengeAbortMessage(opponent: Client) : String {
    return stringify(
        SERVER_CHALLENGE_ABORT,
        Pair("opponent", opponent.id)
    )
}

fun createChallengeAcceptMessage(opponent: Client) : String {
    return stringify(
        SERVER_CHALLENGE_ACCEPT,
        Pair("opponent", opponent.id)
    )
}

fun createChallengeDeclineMessage(opponent: Client) : String {
    return stringify(
        SERVER_CHALLENGE_DECLINE,
        Pair("opponent", opponent.id)
    )
}

fun createGameInitNextMessage(color: Int, board: IntArray, preview: Collection<*>) : String {
    return stringify(
        SERVER_GAME_INIT_NEXT,
        Pair("color", color),
        Pair("board", board),
        Pair("preview", preview)
    )
}

fun createGameInitMessage(color: Int, board: IntArray) : String {
    return stringify(
        SERVER_GAME_INIT,
        Pair("color", color),
        Pair("board", board)
    )
}

fun createGameUpdateNextMessage(source: Any, board: IntArray, preview: Collection<*>): String {
    return stringify(
        SERVER_GAME_UPDATE_NEXT,
        Pair("source", source),
        Pair("board", board),
        Pair("preview", preview)
    );
}

fun createGameUpdateMessage(source: Any, board: IntArray): String {
    return stringify(
        SERVER_GAME_UPDATE,
        Pair("source", source),
        Pair("board", board)
    )
}

fun createGameEndVictoryMessage(source: Any, board: IntArray): String {
    return stringify(
        SERVER_GAME_END_VICTORY,
        Pair("source", source),
        Pair("board", board)
    )
}

fun createGameEndDefeatMessage(source: Any, board: IntArray): String {
    return stringify(
        SERVER_GAME_END_DEFEAT,
        Pair("source", source),
        Pair("board", board)
    )
}

fun createGameEndTieMessage(source: Any, board: IntArray): String {
    return stringify(
        SERVER_GAME_END_TIE,
        Pair("source", source),
        Pair("board", board)
    )
}

fun stringify(eventCode: Int, vararg args: Pair<String, *>): String {
    val node = mapper.createObjectNode()
    node.put("code", eventCode)
    val argsNode = node.putObject("args")
    for(arg in args) {
        argsNode.set<JsonNode>(arg.first, mapper.convertValue(arg.second, JsonNode::class.java))
    }
    return mapper.writeValueAsString(node);
}
