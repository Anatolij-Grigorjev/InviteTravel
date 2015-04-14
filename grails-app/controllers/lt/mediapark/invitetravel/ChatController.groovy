package lt.mediapark.invitetravel

import grails.converters.JSON

class ChatController {


    def chatService

    static allowedMethods = [
            index: 'GET',
            send: 'POST'
    ]

    def index = {

        List<ChatMessage> correspondence = chatService.getCorrespondence(params.id1, params.id2, params.requestor, new Date(params.time?:0L))
        def userMaps = correspondence.collect {
            def map = [
                    'text' : it.text,
                    'fromId' : it.from.id,
                    'fromPicId' : it.from.defaultPictureId,
                    'toPicId' : it.to.defaultPictureId,
                    'toId' : it.to.id,
                    'sent' : it.sent.time,
                    'read' : it.read,
            ]
            if (it.received) {
                map << ['received' : it.received.time]
            }
            map
        }
        render userMaps as JSON
    }


    def send = {
        chatService.sendMessage(params.requestor, params.id, request.JSON.text)
        render(status: 200)
    }






}
