package lt.mediapark.invitetravel

class ChatController {


    def chatService

    static allowedMethods = [
            index: 'GET',
            send: 'POST'
    ]

    def index = {

        List<ChatMessage> correspondence = chatService.getCorrespondence(param.id1, param.id2, new Date(param.time?:0L))
        def userMaps = correspondence.collect { it ->
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
        chatService.sendMessage(param.id1, param.id2, request.JSON.text)
        render(status: 200)
    }






}
