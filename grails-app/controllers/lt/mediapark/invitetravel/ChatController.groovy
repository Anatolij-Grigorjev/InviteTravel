package lt.mediapark.invitetravel

import grails.converters.JSON

class ChatController {


    def chatService
    def JSONConversionService

    static allowedMethods = [
            index: 'GET',
            send: 'POST',
            list: 'GET'
    ]

    def index = {

        Set<ChatMessage> correspondence = chatService.getCorrespondence(params.id1, params.id2, params.currUser.id, (params.time? new Date(Long.parseLong(params.time)):new Date()))
        def userMaps = correspondence.collect {
            JSONConversionService.messageToMap(it)
        }
        def finMap = ['messages' : userMaps]
        render finMap as JSON
    }


    def send = {
        def message = chatService.sendMessage((User)params.currUser, params.id, request.JSON.text)
        def map = JSONConversionService.messageToMap(message)
        render map as JSON
    }


    def list = {
        List<ChatMessage> listMessages = chatService.getChatsList(params.currUser.id)
        def map = listMessages.collect {
            JSONConversionService.messageToMap(it)
        }
        def finMap = ['messages' : map]
        render finMap as JSON
    }
}
