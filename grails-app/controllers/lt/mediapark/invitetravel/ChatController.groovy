package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.utils.ConversionsHelper

class ChatController {


    def chatService

    static allowedMethods = [
            index: 'GET',
            send: 'POST',
            list: 'GET'
    ]

    def index = {

        List<ChatMessage> correspondence = chatService.getCorrespondence(params.id1, params.id2, params.requestor, (params.time? new Date(Long.parseLong(params.time)):new Date()))
        def userMaps = correspondence.collect {
            ConversionsHelper.messageToMap(it)
        }
        def finMap = ['messages' : userMaps]
        render finMap as JSON
    }


    def send = {
        def message = chatService.sendMessage(params.requestor, params.id, request.JSON.text)
        def map = ConversionsHelper.messageToMap(message)
        render map as JSON
    }


    def list = {
        List<ChatMessage> listMessages = chatService.getChatsList(params.requestor)
        def map = listMessages.collect {
            ConversionsHelper.messageToMap(it)
        }
        def finMap = ['messages' : map]
        render finMap as JSON
    }
}
